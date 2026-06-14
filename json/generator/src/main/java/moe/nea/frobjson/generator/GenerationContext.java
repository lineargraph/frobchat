package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import com.palantir.javapoet.JavaFile;
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

	// TODO: make this _lazy_
	public SchemaType getSchemaForProperty(String propertyName, JsonElement value, @Nullable SchemaType parent) {
		var type = value.getAsJsonObject().get("type").getAsString();
		var schema = switch (type) {
			case "object" -> new SchemaObjectType(this, value.getAsJsonObject(), propertyName, parent);
			case "string" -> new SchemaStringType();
			case "integer" -> new SchemaIntegerType();
			default -> throw new RuntimeException("Unknown type " + type);
		};
		allTypes.add(schema);
		todos.add(schema);
		return schema;
	}
}
