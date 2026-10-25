plugins {
	`java-library`
}
dependencies {
	api(projects.json.library)
	testImplementation(libs.junit)
	testRuntimeOnly(libs.junit.platform)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

val matrixSpecRoot = rootProject.file("matrix-spec")

val generateMatrixApi = tasks.register("generateMatrixApi", JavaExec::class) {
	val destinationFolder = layout.buildDirectory.dir("generated/sources/matrixApi")
	var extraSchemasRoot = layout.projectDirectory.dir("src/main/schemas")

	this.inputs.dir(matrixSpecRoot)
	this.inputs.dir(extraSchemasRoot)
	this.outputs.dir(destinationFolder)

	this.classpath(configurations.detachedConfiguration(projects.json.generator))
	this.mainClass = "moe.nea.frobjson.generator.matrix.MatrixMain"
	this.systemProperty("matrixApi.matrixSpecRoot", matrixSpecRoot)
	this.systemProperty("matrixApi.extraSchemaRoot", extraSchemasRoot.asFile.absolutePath)
	this.systemProperty("matrixApi.destinationFolder", destinationFolder.get().asFile.absolutePath)

	doFirst {
		// TODO: make this nicer in regards to deleting extra files
		destinationFolder.get().asFile.deleteRecursively()
	}
}

sourceSets.main {
	java.srcDir(generateMatrixApi)
}

java.withSourcesJar()
java.withJavadocJar()

tasks.test {
	useJUnitPlatform()
}
