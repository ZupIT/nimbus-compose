plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("kotlin")
}


java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.12.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.10-1.0.4")
    implementation(project(":compose", "default"))
}

apply("$rootDir/maven-publish.gradle")
