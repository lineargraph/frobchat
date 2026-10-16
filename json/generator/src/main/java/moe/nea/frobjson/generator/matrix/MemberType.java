package moe.nea.frobjson.generator.matrix;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

public sealed interface MemberType {

	record InlineDefinition(RawJsonType type) implements MemberType {
		@Override
		public @NonNull String toString() {
			return type.toString();
		}
	}

	@RequiredArgsConstructor
	final class Reference implements MemberType, Unlazyable {
		private final SchemaLibrary schemaLibrary;
		private final Path relativeTo;
		private final String reference;
		private SchemaReference schemaReference = null;

		public SchemaReference schemaReference() {
			return schemaReference;
		}

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
