plugins {
	`java-library`
	id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
	version = "27.0.1"
	modules = listOf("javafx.controls", "javafx.fxml")
}

val processor = sourceSets.register("processor")
val annotations = sourceSets.register("annotations")
dependencies {
	"processorImplementation"(annotations.map { it.output })
	api(annotations.map { it.output })
	annotationProcessor(project(":fxutils")) {
		targetConfiguration = "processorElements"
	}
}

val processorConfig = configurations.consumable("processorElements") {
	extendsFrom(configurations.named("processorRuntimeClasspath").get())
}

val processorJar = tasks.register("processorJar", Jar::class) {
	archiveClassifier.set("processor")
	from(processor.map { it.output })
}
artifacts.add(processorConfig.name, processorJar)
