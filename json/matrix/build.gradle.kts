plugins {
	`java-library`
}
dependencies {
	api(projects.json.library)
}

val generateJsonSources by tasks.registering(JavaExec::class) {
	this.classpath(configurations.detachedConfiguration(projects.json.generator))
	this.mainClass = "moe.nea.frobjson.generator.GenerateOpenApiSchemas"
	val schemaFile = file("matrix-client-server-api-v1.18.json")
	this.inputs.file(schemaFile)
	var destinationFolder = layout.buildDirectory.dir("generated/sources/jsonschema")
	this.outputs.dir(destinationFolder)
	this.args(
		schemaFile.absolutePath,
		"moe.nea.frobchat.model",
		destinationFolder.get().asFile.absolutePath
	)
	this.doFirst {
		destinationFolder.get().asFile.deleteRecursively()
	}
}
val generatedJsonSource = files(generateJsonSources)
sourceSets.main {
	java.srcDir(generateJsonSources)
}


