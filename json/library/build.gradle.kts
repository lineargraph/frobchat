import org.gradle.kotlin.dsl.`java-library`

plugins {
	`java-library`
}

dependencies {
	api(libs.jspecify)
	api(libs.jbAnnotations)
	api(libs.gson)
}
