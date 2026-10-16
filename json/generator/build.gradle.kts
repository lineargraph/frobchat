import org.gradle.kotlin.dsl.application
import org.gradle.kotlin.dsl.java

plugins {
	java
	application
}
java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

dependencies {
	implementation(libs.javapoet)
	implementation(libs.gson)
	implementation(libs.jackson.dataformat.yaml)
	implementation(libs.jspecify)
	implementation(libs.guava)
	implementation(projects.json.library)
	annotationProcessor(libs.lombok)
	implementation(libs.slf4j.api)
	runtimeOnly(libs.slf4j.simple)
	compileOnly(libs.lombok)
}
