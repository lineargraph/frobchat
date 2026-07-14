plugins {
	`java-library`
}
dependencies {
	api(projects.json.library)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
val generateJsonSources = tasks.register("generateJsonSources", JavaExec::class) {
	this.classpath(configurations.detachedConfiguration(projects.json.generator))
	this.mainClass = "moe.nea.frobjson.generator.GenerateOpenApiSchemas"
	val schemaFile = file("matrix-client-server-api-v1.18.json")
	this.inputs.file(schemaFile)
	var destinationFolder = layout.buildDirectory.dir("generated/sources/jsonschema")
	this.outputs.dir(destinationFolder)
	this.args(
		destinationFolder.get().asFile.absolutePath,
		"-modelPackage",
		"moe.nea.frobchat.matrixapi.model",
		"-operationPackage",
		"moe.nea.frobchat.matrixapi.operations",
		"-openApi",
		schemaFile.absolutePath,
		"-extendType",
		file("src/main/schemas/user-identifiers.json").absolutePath,
	)
	this.doFirst {
		destinationFolder.get().asFile.deleteRecursively()
	}
}
val generatedJsonSource = files(generateJsonSources)
sourceSets.main {
	java.srcDir(generateJsonSources)
}


