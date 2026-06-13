import org.gradle.kotlin.dsl.application
import org.gradle.kotlin.dsl.java

plugins {
	java
	application
}

dependencies {
	implementation(libs.javapoet)
	implementation(libs.gson)
	implementation(libs.jspecify)
	implementation(projects.json.library)
}
