package moe.nea.frobjson.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.palantir.javapoet.JavaFile;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GenerationContext {
	String packageName = "moe.nea.frobjson.model";
	NameCollection typeNames = new NameCollection();
	List<SchemaType> allTypes = new ArrayList<>();
	List<SchemaType> todos = new ArrayList<>();
	List<JavaFile> files = new ArrayList<>();

	static void main(String[] args) throws IOException {
		var ctx = new GenerationContext();
		var schemaJson = new Gson().fromJson("""
			{
				"additionalProperties": {
					"description": "Application-dependent keys using Java package naming convention."
				},
				"description": "Used by clients to determine the homeserver, identity server, and other\\noptional components they should be interacting with.",
				"example": {
					"m.homeserver": {
						"base_url": "https://matrix.example.com"
					},
					"m.identity_server": {
						"base_url": "https://identity.example.com"
					},
					"org.example.custom.property": {
						"app_url": "https://custom.app.example.org"
					}
				},
				"properties": {
					"m.homeserver": {
						"description": "Used by clients to discover homeserver information.",
						"properties": {
							"base_url": {
								"description": "The base URL for the homeserver for client-server connections.",
								"example": "https://matrix.example.com",
								"format": "uri",
								"type": "string"
							}
						},
						"required": [
							"base_url"
						],
						"title": "Homeserver Information",
						"type": "object"
					},
					"m.identity_server": {
						"description": "Used by clients to discover identity server information.",
						"properties": {
							"base_url": {
								"description": "The base URL for the identity server for client-server connections.",
								"example": "https://identity.example.com",
								"format": "uri",
								"type": "string"
							}
						},
						"required": [
							"base_url"
						],
						"title": "Identity Server Information",
						"type": "object"
					}
				},
				"required": [
					"m.homeserver"
				],
				"title": "Discovery Information",
				"type": "object"
			}
			""", JsonElement.class);
		ctx.getSchemaForProperty("WellKnownClient", schemaJson, null);
		ctx.writeClosure(Path.of("build/testOutput"));
	}

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
