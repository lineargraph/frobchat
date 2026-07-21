package moe.nea.frobjson.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import moe.nea.frobjson.internal.JsonUtil;
import moe.nea.frobjson.internal.SchemaObject;
import moe.nea.frobjson.internal.StreamUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class GenerationContext {

	public String modelPackageName;
	public String operationPackageName;
	NameCollection typeNames = new NameCollection(true);
	public NameCollection operationTypeNames = new NameCollection(true);
	Map<String, SchemaType> typeByTitle = new HashMap<>();

	public @Nullable SchemaType typeByTitle(String name) {
		return typeByTitle.get(name);
	}

	List<SchemaType> allTypes = new ArrayList<>();
	PriorityQueue<Generatable> todos = new PriorityQueue<>(Generatable.COMPARATOR);
	List<JavaFile> files = new ArrayList<>();

	public void addSchema(SchemaType schema) {
		allTypes.add(schema);
	}

	public String guessName(
		String propertyName,
		@Nullable SchemaType parent,
		JsonElement element
	) {

		if (element instanceof JsonObject obj) {
			var title = JsonUtil.getStringOrNull(obj.get("title"));
			if (title != null) return typeNames.foldName(title);
			var allOf = obj.getAsJsonArray("allOf");
			if (allOf != null) {
				return guessName(propertyName, parent, JsonUtil.stream(allOf).reduce(JsonUtil::mergeVariantOverriding).get());
			}
		}
		if (parent != null)
			return typeNames.foldName(parent.name() + "-" + propertyName);
		return typeNames.foldName(propertyName);
	}

	public void writeClosure(Path directory) throws IOException {
		for (JavaFile javaFile : generateClosure()) {
			javaFile.writeTo(directory);
		}
	}

	public List<JavaFile> generateClosure() {
		while (!todos.isEmpty()) {
			var todo = todos.poll();
			files.addAll(todo.emitFiles());
		}
		return files;
	}

	public void enqueue(Generatable generatable) {
		todos.add(generatable);
	}


	private SchemaType constructSchema(String propertyName, JsonElement value, @Nullable SchemaType parent) {
		if (value instanceof JsonPrimitive prim && prim.isBoolean() && prim.getAsBoolean())
			return new SchemaJsonElement();
		var obj = value.getAsJsonObject();
		var allOf = obj.getAsJsonArray("allOf");
		if (allOf != null) {
			// Perhaps allOf could be merged as json objects instead?
			// This needs to be smarter, in terms of naming things. if we merge ClientEventWithoutRoomId a bunch, then this is just really not sustainable
			return getSchemaForProperty(propertyName, JsonUtil.stream(allOf).reduce(JsonUtil::mergeVariantOverriding).get(), parent);
//			return new SchemaAllOfType(
//				this,
//				deriveName(parent, JsonUtil.getStringOrNull(obj.get("title")), propertyName),
//				JsonUtil.stream(allOf)
//					.map(StreamUtil.withIndex())
//					.map(it -> getSchemaForProperty(propertyName + it.index(), it.value(), parent))
//					.toList()
//			);
		}
		var oneOf = obj.getAsJsonArray("oneOf");
		if (oneOf != null) {
			var types = JsonUtil.stream(oneOf)
				.map(StreamUtil.withIndex())
				.map(it -> getSchemaForProperty(propertyName + it.index(), it.value(), parent))
				.map(SchemaType::unlazy)
				.collect(Collectors.toCollection(LinkedHashSet::new));
			if (types.size() == 1)
				return types.getFirst();
//			System.out.println("Could not merge type " + types);
			return new SchemaOneOfType(
				this,
				typeNames.allocateName(guessName(propertyName, parent, obj)),
				new ArrayList<>(types));
		}
		if (obj.entrySet()
			.stream()
			.filter(it -> !it.getKey().startsWith("x-"))
			.findAny()
			.isEmpty()) {
			return new SchemaJsonElement();
		}

		if (obj.get("type") == null || obj.get("type") instanceof JsonArray array) {
			return new SchemaJsonElement(); // TODO actually match on stuff..
		}

		var type = obj.get("type").getAsString();
		return switch (type) {
			case "object" ->
				new SchemaObjectType(this, obj, ClassName.get(modelPackageName, typeNames.allocateName(guessName(propertyName, parent, obj))));
			case "string" -> new SchemaStringType();
			case "integer" -> new SchemaIntegerType();
			case "boolean" -> new SchemaBooleanType();
			case "number" -> new SchemaNumberType();
			case "array" -> new SchemaArrayType(getSchemaForProperty(propertyName, obj.get("items"), parent));
			default -> throw new RuntimeException("Unknown schema " + type);
		};
	}

	Map<SchemaKey, LazySchema> schemaCache = new HashMap<>();


	public record SchemaKey(
		JsonElement element,
		String propertyName
	) {
	}

	public JsonElement cleanValue(JsonElement element, boolean deep) {
		if (element instanceof JsonObject obj) {
			var ignoredKeys = new HashSet<>(List.of("description", "example", "deprecated"));
			if (deep) {
				ignoredKeys.remove("description");
				ignoredKeys.remove("deprecated");
			}

			var obj2 = new JsonObject();
			obj.entrySet()
				.stream()
				.filter(it -> !it.getKey().startsWith("x-"))
				.filter(it -> !ignoredKeys.contains(it.getKey()))
				.forEach(it -> obj2.add(it.getKey(), cleanValue(it.getValue(), true)));

			return obj2;
		}
		if (element instanceof JsonArray arr) {
			return JsonUtil.unstream(JsonUtil.stream(arr)
				.map(it -> cleanValue(it, true)));
		}
		return element;
	}

	public SchemaType getSchemaForProperty(String propertyName, JsonElement value, @Nullable SchemaType parent) {
		var schema =
			schemaCache.computeIfAbsent(
				new SchemaKey(cleanValue(value, false), guessName(propertyName, parent, value)),
				key -> new LazySchema(() -> {
					try {
						return constructSchema(propertyName, value, parent);
					} catch (Throwable e) {
						if (e instanceof TypeMakingException) throw e;
						throw new TypeMakingException("While trying to schematize " + value, e);
					}
				})
			);
		var title = JsonUtil.getStringOrNull(value, "title");
		if (title != null)
			typeByTitle.put(title, schema);
		allTypes.add(schema);
		todos.add(schema);
		schema.get(); // Force instantiation, we only need lazy to deal with potential cycles
		return schema;
	}

	static class TypeMakingException extends RuntimeException {
		public TypeMakingException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
