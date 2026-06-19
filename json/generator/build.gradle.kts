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
	implementation(libs.jspecify)
	implementation(libs.guava)
	implementation(projects.json.library)
}
