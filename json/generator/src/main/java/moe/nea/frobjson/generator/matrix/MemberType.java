package moe.nea.frobjson.generator.matrix;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.Deserializers;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.ser.jdk.ObjectArraySerializer;
import tools.jackson.databind.type.TypeFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed interface MemberType {

	record TypeResolutionContext(
		YAMLUtils.FolderContext context,
		@Nullable String title,
		JsonNode node
	) {
	}

	private static MemberType deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
		ObjectNode tree = p.readValueAsTree();
		var context = Objects.requireNonNull(YAMLUtils.getContext());
		var typeResolutionContext = new TypeResolutionContext(
			context,
			tree.has("title") ? tree.get("title").asString() : null,
			tree
		);
		var override = context.library().resolveOverride(typeResolutionContext);
		if (override != null) return override;
		if (tree.has("$ref")) {
			assert tree.size() == 1;
			return new Reference(
				context.library(),
				context.folder(),
				tree.get("$ref").asString()
			);
		}
		if (tree.has("allOf")) {
			// Specifically in the context of this matrix spec this typically is used for extending an object in some way.
			assert tree.size() == 1;
			return new MemberType.MergeDefinition(ctxt.readTreeAsValue(tree.get("allOf"),
				TypeFactory.createDefaultInstance()
					.constructCollectionLikeType(List.class, MemberType.class)));
		}
		if (!tree.has("type"))
			tree.put("type", "unknown");
		return new MemberType.InlineDefinition(ctxt.readTreeAsValue(tree, RawJsonType.class));
	}

	Deserializers DESERIALIZERS = new SimpleDeserializers(
		Map.of(MemberType.class, new ValueDeserializer<MemberType>() {
			@Override
			public MemberType deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
				var memberType = MemberType.deserialize(p, ctxt);
				if (memberType instanceof Unlazyable unlazy)
					YAMLUtils.getContext().unlazyables().add(unlazy);
				return memberType;
			}
		})
	);

	sealed interface DirectDeclaration extends MemberType {
		RawJsonType type();
	}

	record InlineDefinition(RawJsonType type) implements DirectDeclaration {
		@Override
		public @NonNull String toString() {
			return type.toString();
		}
	}

	@RequiredArgsConstructor
	final class TypeOverride implements DirectDeclaration, Unlazyable {
		final Path path;
		final SchemaLibrary schemaLibrary;
		RawJsonType type;

		@Override
		public RawJsonType type() {
			return type;
		}

		@Override
		public void unlazy() {
			type = YAMLUtils.readFile(schemaLibrary, path, RawJsonType.class);
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

	@RequiredArgsConstructor
	final class MergeDefinition implements DirectDeclaration, Unlazyable {
		private final List<MemberType> definitions;
		private RawJsonType actual;

		public RawJsonType type() {
			assert actual != null;
			return actual;
		}

		private RawJsonType getRaw(MemberType type) {
			return switch (type) {
				case DirectDeclaration inlineDefinition -> inlineDefinition.type();
				case Reference reference -> reference.schemaReference().getRawJsonType();
			};
		}

		private <T> T mergeNulls(T base, T spec) {
			if (spec == null) return base;
			return spec;
		}

		private MemberType mergeProp(MemberType base, MemberType spec) {
			var mergeDef = new MergeDefinition(List.of(base, spec));
			mergeDef.unlazy();
			return mergeDef;
		}

		private <T extends RawJsonType> T mergeCommon(T merged, T base, T spec) {
			merged.setTitle(mergeNulls(base.getTitle(), spec.getTitle()));
			merged.setDescription(mergeNulls(base.getDescription(), spec.getDescription()));
			return merged;
		}

		private RawJsonType.ObjectType mergeObjectType(
			RawJsonType.ObjectType base,
			RawJsonType.ObjectType spec
		) {
			var mergedProperties = new HashMap<>(base.getProperties());
			for (var entry : spec.getProperties().entrySet()) {
				var key = entry.getKey();
				var specProp = entry.getValue();
				mergedProperties.merge(key, specProp, this::mergeProp);
			}
			var merged = new RawJsonType.ObjectType(
				mergedProperties,
				mergeNulls(base.getAdditionalProperties(), spec.getAdditionalProperties()),
				Stream.of(base, spec)
					.flatMap(it -> it.getRequired().stream())
					.collect(Collectors.toSet())
			);
			mergeCommon(merged, base, spec);
			return merged;
		}

		private <T extends RawJsonType> void mergeAll(Class<T> tClass, BiFunction<T, T, T> merge) {
			T p = null;
			for (MemberType definition : definitions) {
				var t = tClass.cast(getRaw(definition));
				if (p == null) p = t;
				else p = merge.apply(p, t);
			}
			this.actual = p;
		}

		@Override
		public void unlazy() {
			var p = getRaw(definitions.getFirst());
			if (p instanceof RawJsonType.ObjectType)
				mergeAll(RawJsonType.ObjectType.class, this::mergeObjectType);
			else if (p instanceof RawJsonType.StringType)
				mergeAll(RawJsonType.StringType.class, (base, spec) ->
					mergeCommon(new RawJsonType.StringType(
						mergeNulls(base.getFormat(), spec.getFormat()),
						mergeNulls(base.getPattern(), spec.getPattern()),
						mergeNulls(base.getValidValues(), spec.getValidValues()) // TODO: technically this would be an intersection?
					), base, spec));

			else throw new RuntimeException("Cannot merge " + p);
		}
	}
}
