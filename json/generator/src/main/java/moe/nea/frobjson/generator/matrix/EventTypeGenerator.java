package moe.nea.frobjson.generator.matrix;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.palantir.javapoet.*;
import moe.nea.frobmatrix.event.EventContent;

import javax.lang.model.element.Modifier;
import java.util.List;

public class EventTypeGenerator extends ObjectTypeGenerator {
	private final RawEventSchema rawEventSchema;
	private ClassNameSpace typeCns;
	public List<ClassNameSpace> messageTypes;

	public EventTypeGenerator(RawEventSchema rawEventSchema) {
		super(rawEventSchema.getProperties().getContent());
		this.rawEventSchema = rawEventSchema;
	}

	@Override
	public void emitDependencies(ClassNameSpace classNameSpace) {
		super.emitDependencies(classNameSpace);
		this.typeCns = classNameSpace.createChild("Type");
		this.typeCns.setGenerator(new EventTypeTypeGenerator(classNameSpace));
	}

	public class EventTypeTypeGenerator implements ClassGenerator {
		private final ClassNameSpace parent;

		public EventTypeTypeGenerator(ClassNameSpace parent) {
			this.parent = parent;
		}

		@Override
		public void emitDependencies(ClassNameSpace classNameSpace) {
		}

		@Override
		public void writeType(TypeSpec.Builder spec, ClassNameSpace classNameSpace) {
			spec.addModifiers(Modifier.STATIC, Modifier.PUBLIC)
				.addSuperinterface(ParameterizedTypeName.get(
					rawEventSchema.stateEventType(),
					parent.getClassName()
				))
				.addMethod(MethodSpec.methodBuilder("type")
					.addModifiers(Modifier.PUBLIC)
					.addAnnotation(Override.class)
					.returns(String.class)
					.addStatement("return $S", rawEventSchema.getProperties().getType().getValue())
					.build())
				.addMethod(MethodSpec.methodBuilder("contentType")
					.addModifiers(Modifier.PUBLIC)
					.addAnnotation(Override.class)
					.returns(ParameterizedTypeName.get(ClassName.get(Class.class), parent.getClassName()))
					.addStatement("return $T.class", parent.getClassName())
					.build())
				.build();
		}
	}

	@Override
	public void writeType(TypeSpec.Builder spec, ClassNameSpace classNameSpace) {
		super.writeType(spec, classNameSpace);
		spec.addSuperinterface(ParameterizedTypeName.get(
			ClassName.get(EventContent.class), classNameSpace.getClassName()
		));
		spec.addMethod(MethodSpec.methodBuilder("type")
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(Override.class)
			.returns(typeCns.getClassName())
			.addStatement("return TYPE")
			.build());
		spec.addField(FieldSpec.builder(typeCns.getClassName(), "TYPE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
			.initializer("new $T()", typeCns.getClassName())
			.build());
		if (messageTypes != null) {
			spec.addAnnotation(AnnotationSpec.builder(JsonTypeInfo.class)
				.addMember("use", "$T.Id.NAME", JsonTypeInfo.class)
				.addMember("include", "$T.As.PROPERTY", JsonTypeInfo.class)
				.addMember("property", "$S", "msgtype")
				.addMember("visible", "true")
				.build());
			spec.addAnnotation(AnnotationSpec.builder(JsonSubTypes.class)
				.addMember("value", "{\n$L\n}",
					messageTypes.stream()
						.map(it -> CodeBlock.of(
							"@$T.Type(value = $T.class, name = $S)",
							JsonSubTypes.class,
							it.getClassName(),
							((MessageEventTypeGenerator) it.getGenerator()).msgType
						))
						.collect(CodeBlock.joining(",\n")))
				.build());
		}
	}
}
