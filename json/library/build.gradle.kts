import org.gradle.kotlin.dsl.`java-library`

plugins {
	`java-library`
}

dependencies {
	api(libs.jspecify)
	api(libs.gson)
}
