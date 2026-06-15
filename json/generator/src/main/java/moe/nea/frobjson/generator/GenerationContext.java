package moe.nea.frobjson.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.JavaFile;
import moe.nea.frobjson.internal.JsonUtil;
import moe.nea.frobjson.internal.StreamUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerationContext {
	public GenerationContext(String packageName) {
		this.packageName = packageName;
	}

	String packageName;
	NameCollection typeNames = new NameCollection(true);
	List<SchemaType> allTypes = new ArrayList<>();
	List<SchemaType> todos = new ArrayList<>();
	List<JavaFile> files = new ArrayList<>();

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
			var myTodos = todos;
			todos = new ArrayList<>();
			for (var todo : myTodos) {
				files.addAll(todo.emitFiles());
			}
		}
		return files;
	}

	private SchemaType constructSchema(String propertyName, JsonElement value, @Nullable SchemaType parent) {
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
			return new SchemaOneOfType(
				this,
				typeNames.allocateName(guessName(propertyName, parent, obj)),
				JsonUtil.stream(oneOf)
					.map(StreamUtil.withIndex())
					.map(it -> getSchemaForProperty(propertyName + it.index(), it.value(), parent))
					.toList()
			);
		}
		if (obj.entrySet()
			.stream()
			.filter(it -> !it.getKey().startsWith("x-"))
			.findAny()
			.isEmpty()) {
			return new SchemaJsonElement();
		}

		if (obj.get("type") instanceof JsonArray array) {
			return new SchemaJsonElement(); // TODO actually match on stuff..
		}

		var type = obj.get("type").getAsString();
		return switch (type) {
			case "object" -> new SchemaObjectType(this, obj, propertyName, parent);
			case "string" -> new SchemaStringType();
			case "integer" -> new SchemaIntegerType();
			case "boolean" -> new SchemaBooleanType();
			case "number" -> new SchemaNumberType();
			case "array" -> new SchemaArrayType(getSchemaForProperty(propertyName, obj.get("items"), parent));
			default -> throw new RuntimeException("Unknown type " + type);
		};
	}

	Map<SchemaKey, LazySchema> schemaCache = new HashMap<>();

	public record SchemaKey(
		JsonElement element,
		String propertyName
	) {
	}

	public JsonElement cleanValue(JsonElement element) {
		if (element instanceof JsonObject obj) {
			var ignoredKeys = List.of("description", "example", "deprecated");
			var obj2 = new JsonObject();
			obj.entrySet()
				.stream()
				.filter(it -> !it.getKey().startsWith("x-"))
				.filter(it -> !ignoredKeys.contains(it.getKey()))
				.forEach(it -> obj2.add(it.getKey(), cleanValue(it.getValue())));

			return obj2;
		}
		if (element instanceof JsonArray arr) {
			return JsonUtil.unstream(JsonUtil.stream(arr)
				.map(this::cleanValue));
		}
		return element;
	}

	public SchemaType getSchemaForProperty(String propertyName, JsonElement value, @Nullable SchemaType parent) {
		var schema =
			schemaCache.computeIfAbsent(
				new SchemaKey(cleanValue(value), guessName(propertyName, parent, value)),
				key -> new LazySchema(() -> {
					System.err.println("DEBUG: " + key);
					try {
						return constructSchema(propertyName, value, parent);
					} catch (Throwable e) {
						if (e instanceof TypeMakingException) throw e;
						throw new TypeMakingException("While trying to schematize " + value, e);
					}
				})
			);
		allTypes.add(schema);
		todos.add(schema);
		return schema;
	}

	static class TypeMakingException extends RuntimeException {
		public TypeMakingException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
