package moe.nea.frobjson.generator.matrix;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.databind.*;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class YAMLUtils {
	record FolderContext(
		Path folder,
		SchemaLibrary library,
		List<Unlazyable> unlazyables
	) {
	}

	private static ThreadLocal<FolderContext> CURRENT_FOLDER = new ThreadLocal<>();

	public static <T> T withContextFolder(SchemaLibrary library, Path folder, Supplier<T> supplier) {
		assert Files.isDirectory(folder);
		assert CURRENT_FOLDER.get() == null;
		var context = new FolderContext(folder, library, new ArrayList<>());
		CURRENT_FOLDER.set(context);
		try {
			return supplier.get();
		} finally {
			assert CURRENT_FOLDER.get() == context;
			CURRENT_FOLDER.remove();
			context.unlazyables().forEach(Unlazyable::unlazy);
		}
	}

	private final static ObjectMapper MAPPER = new YAMLMapper()
		.rebuild()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
		.addModule(new JacksonModule() {
			@Override
			public String getModuleName() {
				return "OpenApiSchema";
			}

			@Override
			public Version version() {
				return new Version(1, 0, 0, null, null, null);
			}

			@Override
			public void setupModule(SetupContext context) {
				context.addDeserializers(new SimpleDeserializers(
					Map.of(IndirectJsonSchema.class, new ValueDeserializer<IndirectJsonSchema>() {
						@Override
						public IndirectJsonSchema deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
							ObjectNode tree = p.readValueAsTree();
							if (tree.has("$ref")) {
								var context = Objects.requireNonNull(CURRENT_FOLDER.get());
								var reference = new IndirectJsonSchema.Reference(
									context.library(),
									context.folder(),
									tree.get("$ref").asString()
								);
								context.unlazyables().add(reference);
								return reference;
							}
							if (!tree.has("type"))
								tree.put("type", "unknown");
							return new IndirectJsonSchema.Direct(ctxt.readTreeAsValue(tree, RawJsonType.class));
						}
					})
				));
			}
		})
		.build();

	public static <T> T readFile(SchemaLibrary library, Path file, Class<T> type) {
		try {
			return withContextFolder(library, file.getParent(), () -> MAPPER.readValue(file, type));
		} catch (Exception e) {
			throw new RuntimeException("While reading " + file, e);
		}
	}
}
