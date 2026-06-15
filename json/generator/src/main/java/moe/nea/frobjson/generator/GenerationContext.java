package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import com.palantir.javapoet.JavaFile;
import moe.nea.frobjson.internal.JsonUtil;
import moe.nea.frobjson.internal.StreamUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GenerationContext {
	public GenerationContext(String packageName) {
		this.packageName = packageName;
	}

	String packageName;
	NameCollection typeNames = new NameCollection(true);
	List<SchemaType> allTypes = new ArrayList<>();
	List<SchemaType> todos = new ArrayList<>();
	List<JavaFile> files = new ArrayList<>();

	public String deriveName(
		@Nullable SchemaType parent,
		@Nullable String title,
		String propertyName) {
		if (title != null) {
			return typeNames.allocateName(title);
		}
		var foldedPropName = typeNames.foldName(propertyName);
		if (parent != null)
			return typeNames.allocateName(typeNames.foldName(parent.name()) + "-" + foldedPropName);
		return typeNames.allocateName(foldedPropName);
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
			return new SchemaAllOfType(
				this,
				deriveName(parent, JsonUtil.getStringOrNull(obj.get("title")), propertyName),
				JsonUtil.stream(allOf)
					.map(StreamUtil.withIndex())
					.map(it -> getSchemaForProperty(propertyName + it.index(), it.value(), parent))
					.toList()
			);
		}
		var type = obj.get("type").getAsString();
		return switch (type) {
			case "object" -> new SchemaObjectType(this, obj, propertyName, parent);
			case "string" -> new SchemaStringType();
			case "integer" -> new SchemaIntegerType();
			case "array" -> new SchemaArrayType(getSchemaForProperty(propertyName, obj.get("items"), parent));
			default -> throw new RuntimeException("Unknown type " + type);
		};
	}

	// TODO: make this _lazy_
	public SchemaType getSchemaForProperty(String propertyName, JsonElement value, @Nullable SchemaType parent) {
		try {
			var schema = constructSchema(propertyName, value, parent);
			allTypes.add(schema);
			todos.add(schema);
			return schema;
		} catch (Throwable e) {
			if (e instanceof TypeMakingException) throw e;
			throw new TypeMakingException("While trying to schematize " + value, e);
		}
	}

	static class TypeMakingException extends RuntimeException {
		public TypeMakingException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
