package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.*;
import moe.nea.frobjson.generator.NameCollection;
import moe.nea.frobjson.generator.TypeUtils;
import moe.nea.frobmatrix.event.EventType;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventGenerator implements ClassGenerator {
	private final MatrixGenerator generator;

	public EventGenerator(MatrixGenerator generator) {
		this.generator = generator;
		var eventTypes = generator.getPackageNameSpace(generator.basePackage() + ".event")
			.createChild("EventTypesData");
		eventTypes.setGenerator(this);
	}

	public void generateEventType(String fileName) {
		var referencePath = new SchemaLibrary.SchemaReferencePath(
			Path.of("event-schemas/schema/" + fileName),
			SchemaLibrary.DataRoot.SPEC
		);
		var rawEventSchema = YAMLUtils.readFile(
			generator.schemaLibrary(),
			referencePath,
			RawEventSchema.class
		);
		var eventTypeClass = generator.getPackageNameSpace(generator.basePackage() + ".event.types")
			.createChild(NameCollection.foldName(
				rawEventSchema.getTitle() != null
					? rawEventSchema.getTitle()
					: fileName.replace(".yaml", "").replace("m.", ""),
				NameCollection.NameType.CLASS_NAME));
		eventTypeClass.setGenerator(new EventTypeGenerator(rawEventSchema));
		allTypes.add(eventTypeClass.getClassName());
	}

	public List<ClassName> allTypes = new ArrayList<>();

	@Override
	public void emitDependencies(ClassNameSpace classNameSpace) {
	}

	@Override
	public void writeType(TypeSpec.Builder spec, ClassNameSpace classNameSpace) {
		spec.addField(FieldSpec.builder(TypeUtils.list(WildcardTypeName.subtypeOf(ParameterizedTypeName.get(ClassName.get(EventType.class), WildcardTypeName.subtypeOf(ClassName.OBJECT)))), "TYPES")
			.addModifiers(Modifier.STATIC)
			.initializer("$T.asList($L)",
				Arrays.class,
				allTypes.stream()
					.map(typ -> CodeBlock.of("$T.TYPE", typ))
					.collect(CodeBlock.joining(", "))
			)
			.build());
	}
}
