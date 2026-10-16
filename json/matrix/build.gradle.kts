plugins {
	`java-library`
}
dependencies {
	api(projects.json.library)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

val matrixSpecRoot = rootProject.file("matrix-spec")

val generateMatrixApi = tasks.register("generateMatrixApi", JavaExec::class) {
	val destinationFolder = layout.buildDirectory.dir("generated/sources/matrixApi")

	this.inputs.dir(matrixSpecRoot)
	this.outputs.dir(destinationFolder)

	this.classpath(configurations.detachedConfiguration(projects.json.generator))
	this.mainClass = "moe.nea.frobjson.generator.matrix.MatrixMain"
	this.systemProperty("matrixApi.matrixSpecRoot", matrixSpecRoot)
	this.systemProperty("matrixApi.destinationFolder", destinationFolder.get().asFile.absolutePath)
}

val generateJsonSources = tasks.register("generateJsonSources", JavaExec::class) {
	this.classpath(configurations.detachedConfiguration(projects.json.generator))
	this.mainClass = "moe.nea.frobjson.generator.GenerateOpenApiSchemas"
	val schemaFile = file("matrix-client-server-api-v1.18.json")
	this.inputs.file(schemaFile)
	var destinationFolder = layout.buildDirectory.dir("generated/sources/jsonschema")
	this.outputs.dir(destinationFolder)
	val instantMessaging = project.file("src/main/schemas/instant-messaging.json")
	this.inputs.file(instantMessaging)
	val extendSchemas = project.file("src/main/schemas/user-identifiers.json")
	this.inputs.file(extendSchemas) // TODO: auto collect all files in there
	this.args(
		destinationFolder.get().asFile.absolutePath,
		"-modelPackage",
		"moe.nea.frobchat.matrixapi.model",
		"-operationPackage",
		"moe.nea.frobchat.matrixapi.operations",
		"-openApi",
		schemaFile.absolutePath,
		"-extraTagged",
		instantMessaging.absolutePath,
		"-extendType",
		extendSchemas.absolutePath,
	)
	this.doFirst {
		destinationFolder.get().asFile.deleteRecursively()
	}
}
val generatedJsonSource = files(generateJsonSources)
sourceSets.main {
	java.srcDir(generateJsonSources)
}

java.withSourcesJar()
java.withJavadocJar()

tasks.test {
	failOnNoDiscoveredTests = false
}
