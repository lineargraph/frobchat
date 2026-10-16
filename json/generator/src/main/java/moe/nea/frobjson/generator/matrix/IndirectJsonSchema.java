package moe.nea.frobjson.generator.matrix;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

public sealed interface IndirectJsonSchema {

	record Direct(RawJsonType type) implements IndirectJsonSchema {
		@Override
		public @NonNull String toString() {
			return type.toString();
		}
	}

	@RequiredArgsConstructor
	final class Reference implements IndirectJsonSchema, Unlazyable {
		final SchemaLibrary schemaLibrary;
		final Path relativeTo;
		final String reference;
		SchemaReference schemaReference = null;

		@Override
		public void unlazy() {
			schemaReference = schemaLibrary.resolveDefinition(relativeTo, reference);
		}

		@Override
		public String toString() {
			return schemaReference.toString();
		}
	}

}
