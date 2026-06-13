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
	NameCollection typeNames = new NameCollection();
	List<SchemaType> allTypes = new ArrayList<>();
	List<SchemaType> todos = new ArrayList<>();
	List<JavaFile> files = new ArrayList<>();

	public String deriveName(
		@Nullable SchemaType parent,
		@Nullable String title,
		String propertyName) {
		if (title != null) {
			var candidate = typeNames.foldName(title);
			if (!typeNames.contains(candidate)) {
				return typeNames.allocateName(candidate);
			}
		}
		var foldedPropName = typeNames.foldName(propertyName);
		if (!typeNames.contains(foldedPropName)) return typeNames.allocateName(foldedPropName);
		if (parent != null)
			return typeNames.allocateName(parent.name() + foldedPropName);
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

	public SchemaType getSchemaForProperty(String propertyName, JsonElement value, @Nullable SchemaType parent) {
		var type = value.getAsJsonObject().get("type").getAsString();
		var schema = switch (type) {
			case "object" -> new SchemaObjectType(this, value.getAsJsonObject(), propertyName, parent);
			case "string" -> new SchemaStringType();
			default -> throw new RuntimeException();
		};
		allTypes.add(schema);
		todos.add(schema);
		return schema;
	}
}
