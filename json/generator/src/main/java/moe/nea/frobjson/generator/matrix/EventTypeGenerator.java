package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.*;
import moe.nea.frobmatrix.event.EventContent;

import javax.lang.model.element.Modifier;

public class EventTypeGenerator extends ObjectTypeGenerator {
	private final RawEventSchema rawEventSchema;
	private ClassNameSpace typeCns;

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
	}
}
