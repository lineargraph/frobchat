package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import moe.nea.frobjson.internal.JsonUtil;
import moe.nea.frobjson.internal.StreamUtil;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Objects;

public class ExtendedType implements Generatable {
	private final ClassName className;
	private final List<SchemaExtensionObjectType> types;

	public ExtendedType(
		ClassName className,
		List<SchemaExtensionObjectType> types) {
		this.className = className;
		this.types = types;
	}

	public static void generate(GenerationContext ctx, JsonElement extendedType) {
		var object = extendedType.getAsJsonObject();
		var title = object.get("extendsType").getAsString();
		var extensionType = ClassName.get(ctx.modelPackageName, ctx.typeNames.allocateName(title + " ext"));
		var typeKey = object.get("typeKey").getAsString();
		var baseSchema = (SchemaObjectType) Objects.requireNonNull(ctx.typeByTitle(title), "Could not find extensible type: " + title).unlazy();
		var names = new NameCollection(true);
		var types = JsonUtil.streamEntries(object.get("types").getAsJsonObject())
			.map(entry -> {
					var variantName = extensionType.nestedClass(names.allocateName(entry.getKey()));
					var variantType = new SchemaExtensionObjectType(ctx, (JsonObject) entry.getValue(), variantName, baseSchema, typeKey, entry.getKey());
					ctx.addSchema(variantType);
					return variantType;
				}
			).toList();
		ctx.enqueue(new ExtendedType(extensionType, types));
	}

	@Override
	public List<? extends JavaFile> emitFiles() {
		var cls = TypeSpec.classBuilder(className)
			.addModifiers(Modifier.PUBLIC)
			.addTypes(StreamUtil.iterable(types.stream().map(SchemaObjectType::buildClass).map(TypeSpec.Builder::build)));
		return List.of(JavaFile.builder(className.packageName(), cls.build()).build());
	}
}
