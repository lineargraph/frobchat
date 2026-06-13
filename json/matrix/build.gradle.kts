plugins {
	`java-library`
}
dependencies {
	api(projects.json.library)
}

val generateJsonSources by tasks.registering(JavaExec::class) {
	this.classpath(configurations.detachedConfiguration(projects.json.generator))
	this.mainClass = "moe.nea.frobjson.generator.GenerateSingleSchema"
	val schemaFile = file("testschema.json")
	this.inputs.file(schemaFile)
	var destinationFolder = layout.buildDirectory.dir("generated/sources/jsonschema")
	this.outputs.dir(destinationFolder)
	this.args(
		schemaFile.absolutePath,
		"moe.nea.frobchat.model",
		"WellKnownProperties",
		destinationFolder.get().asFile.absolutePath
	)
}
val generatedJsonSource = files(generateJsonSources)
sourceSets.main {
	java.srcDir(generateJsonSources)
}


