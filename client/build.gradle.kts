plugins {
	application
	id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
	version = "27.0.1"
	modules = listOf("javafx.controls")
}

dependencies {
	implementation(projects.json.matrix)
	implementation(libs.gemsfx)
	implementation(libs.atlantafx.base)
	implementation(libs.slf4j.api)
	implementation(projects.fxutils)
	annotationProcessor(projects.fxutils) {
		targetConfiguration = "processorElements"
	}
	runtimeOnly(libs.slf4j.simple)
	testImplementation(libs.junit)
	testImplementation(libs.hamcrest)
	testRuntimeOnly(libs.junit.platform)
}

tasks.test {
	useJUnitPlatform()
}

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

application.mainClass = "moe.nea.frobmatrix.ui.Launcher"
