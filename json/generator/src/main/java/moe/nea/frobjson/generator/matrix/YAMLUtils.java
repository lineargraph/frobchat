package moe.nea.frobjson.generator.matrix;

import tools.jackson.core.Version;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class YAMLUtils {
	public static FolderContext getContext() {
		return Objects.requireNonNull(CURRENT_FOLDER.get());
	}

	public record FolderContext(
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
				context.addDeserializers(MemberType.DESERIALIZERS);
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
