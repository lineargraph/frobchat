package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.*;
import moe.nea.frobjson.generator.NameCollection;
import moe.nea.frobjson.generator.TypeUtils;
import moe.nea.frobjson.internal.IterUtil;
import moe.nea.frobmatrix.event.EventType;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventGenerator implements ClassGenerator {
	private final MatrixGenerator generator;
	public ClassNameSpace messageType;

	public EventGenerator(MatrixGenerator generator) {
		this.generator = generator;
		var eventTypes = generator.getPackageNameSpace(generator.basePackage() + ".event")
			.createChild("EventTypesData");
		eventTypes.setGenerator(this);
	}

	public ClassNameSpace generateEventType(String fileName) {
		return generateEventType(new SchemaLibrary.SchemaReferencePath(
				Path.of("event-schemas/schema/" + fileName),
				SchemaLibrary.DataRoot.SPEC
			)
		);
	}

	public ClassNameSpace generateEventType(SchemaLibrary.SchemaReferencePath referencePath) {
		var rawEventSchema = YAMLUtils.readFile(
			generator.schemaLibrary(),
			referencePath,
			RawEventSchema.class
		);
		var eventTypeClass = generator.getPackageNameSpace(generator.basePackage() + ".event.types")
			.createChild(NameCollection.foldName(
				rawEventSchema.getTitle() != null
					? rawEventSchema.getTitle()
					: referencePath.relativeToDataRoot().getFileName().toString().replace(".yaml", "").replace("m.", ""),
				NameCollection.NameType.CLASS_NAME));
		eventTypeClass.setGenerator(new EventTypeGenerator(rawEventSchema));
		allTypes.add(eventTypeClass.getClassName());
		return eventTypeClass;
	}

	public void generateMessageType(String fileName) {
		var referencePath = new SchemaLibrary.SchemaReferencePath(
			Path.of("event-schemas/schema/" + fileName),
			SchemaLibrary.DataRoot.SPEC
		);
		var rawEventSchema = YAMLUtils.readFile(
			generator.schemaLibrary(),
			referencePath,
			RawEventSchema.class
		);
		assert rawEventSchema.getProperties().getType().getType().equals("m.room.message");
		var content = rawEventSchema.getProperties().getContent();
		var msgType = (RawJsonType.StringType) ((MemberType.DirectDeclaration) content.getProperties().remove("msgtype")).type();
		assert msgType.getValidValues() != null;
		var msgTypeString = IterUtil.single(msgType.getValidValues());
		var msgTypeClass = generator.getPackageNameSpace(generator.basePackage() + ".event.types.message")
			.createChild(NameCollection.foldName(rawEventSchema.getTitle(), NameCollection.NameType.CLASS_NAME));
		rawEventSchema.getProperties().getContent().getProperties().remove("body");
		msgTypeClass.setGenerator(new MessageEventTypeGenerator(this.messageType, msgTypeString, rawEventSchema));
		var messageTypeGen = (EventTypeGenerator) messageType.getGenerator();
		if (messageTypeGen.messageTypes == null)
			messageTypeGen.messageTypes = new ArrayList<>();
		messageTypeGen.messageTypes.add(msgTypeClass);
	}

	public List<ClassName> allTypes = new ArrayList<>();

	@Override
	public void emitDependencies(ClassNameSpace classNameSpace) {
	}

	@Override
	public void writeType(TypeSpec.Builder spec, ClassNameSpace classNameSpace) {
		spec.addField(FieldSpec.builder(TypeUtils.list(WildcardTypeName.subtypeOf(ParameterizedTypeName.get(ClassName.get(EventType.class), WildcardTypeName.subtypeOf(ClassName.OBJECT)))), "TYPES")
			.addModifiers(Modifier.STATIC)
			.initializer("$T.asList(\n$L\n)",
				Arrays.class,
				allTypes.stream()
					.map(typ -> CodeBlock.of("$T.TYPE", typ))
					.collect(CodeBlock.joining(",\n"))
			)
			.build());
	}
}
