import org.gradle.kotlin.dsl.`java-library`

plugins {
	`java-library`
}
java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

dependencies {
	api(libs.jspecify)
	api(libs.jbAnnotations)
	api(libs.gson)
}
java.withSourcesJar()
