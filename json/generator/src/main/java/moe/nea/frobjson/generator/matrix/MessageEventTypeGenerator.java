package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;

public class MessageEventTypeGenerator extends ObjectTypeGenerator {
	private final ClassNameSpace messageNameSpace;
	public final String msgType;
	public ClassNameSpace classNameSpace;

	public MessageEventTypeGenerator(
		ClassNameSpace messageNameSpace,
		String msgType,
		RawEventSchema rawEventSchema) {
		super(rawEventSchema.getProperties().getContent());
		this.messageNameSpace = messageNameSpace;
		this.msgType = msgType;
	}

	@Override
	public void emitDependencies(ClassNameSpace classNameSpace) {
		super.emitDependencies(classNameSpace);
		this.classNameSpace = classNameSpace;
	}

	@Override
	protected void beforeConstructor(MethodSpec.Builder builder) {
		builder.addStatement("setMsgtype($S)", msgType);
	}

	@Override
	public void writeType(TypeSpec.Builder typeSpec, ClassNameSpace classNameSpace) {
		super.writeType(typeSpec, classNameSpace);
		typeSpec.superclass(messageNameSpace.getClassName());
	}
}
