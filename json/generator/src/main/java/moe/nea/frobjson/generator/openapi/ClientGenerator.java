package moe.nea.frobjson.generator.openapi;

import com.palantir.javapoet.*;
import moe.nea.frobjson.generator.Generatable;
import moe.nea.frobjson.generator.TypeUtils;
import moe.nea.frobjson.internal.StreamUtil;
import moe.nea.frobjson.openapi.Operation;
import moe.nea.frobjson.openapi.client.ApiClient;
import moe.nea.frobjson.openapi.client.ApiExecutor;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public record ClientGenerator(ClassName clientName, List<OpenApiOperation> operations) implements Generatable {
	@Override
	public List<? extends JavaFile> emitFiles() {
		var typ = TypeSpec.classBuilder(clientName)
			.addModifiers(Modifier.PUBLIC)
			.superclass(ApiClient.class)
			.addAnnotation(TypeUtils.buildSuppressWarnings(Stream.of("unused")))
			.addMethod(MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ApiExecutor.class, "executor")
				.addStatement("super(executor)")
				.build())
			.addMethods(StreamUtil.iterable(operations.stream()
				.map(operation -> MethodSpec.methodBuilder(operation.operationId())
					.addModifiers(Modifier.PUBLIC)
					.returns(ParameterizedTypeName.get(ClassName.get(CompletableFuture.class), operation.successTyp()))
					.addParameters(StreamUtil.iterable(operation.parameters()
						.stream()
						.map(par -> ParameterSpec.builder(par.type(), par.fieldName()).build())))
					.addParameter(operation.bodyType(), "body")
					.addStatement(CodeBlock.of("return this.executor().executeOperation($T.INSTANCE,\n$L,\n$L)$L",
						operation.clsName(),
						CodeBlock.of("new $T.Parameters($L)",
							operation.clsName(),
							operation.parameters()
								.stream()
								.map(it -> CodeBlock.of("$L", it.fieldName()))
								.collect(CodeBlock.joining(",\n"))),
						"body",
						operation.successResponse() != null
							? CodeBlock.of(".thenApply($T::asSuccess)", operation.responsesTyp())
							: ""
					))
					.build())))
			.addMethods(StreamUtil.iterable(operations.stream()
				.filter(it -> it.requestBody() == null)
				.map(operation -> MethodSpec.methodBuilder(operation.operationId())
					.addModifiers(Modifier.PUBLIC)
					.returns(ParameterizedTypeName.get(ClassName.get(CompletableFuture.class), operation.successTyp()))
					.addParameters(StreamUtil.iterable(operation.parameters()
						.stream()
						.map(par -> ParameterSpec.builder(par.type(), par.fieldName()).build())))
					.addStatement(CodeBlock.of("return this.$L($L)",
						operation.operationId(),
						Stream.concat(operation.parameters()
								.stream()
								.map(it -> CodeBlock.of("$L", it.fieldName())),
							Stream.of(CodeBlock.of("$T.EMPTY_BODY", Operation.class))
						).collect(CodeBlock.joining(",\n"))))
					.build())));
		
		return List.of(JavaFile.builder(clientName.packageName(), typ.build()).build());
	}
}
