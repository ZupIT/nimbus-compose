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
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.10-1.0.6")
    implementation("br.com.zup.nimbus:nimbus-core-jvm:${rootProject.extra["nimbus_core_version"]}")
    implementation(project(":annotation"))

    //Unit testing
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.7.20")
    testImplementation(kotlin("test"))
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.9")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.1")
}

tasks.test {
    useJUnitPlatform()
    minHeapSize = "512m"
    maxHeapSize = "1024m"
    jvmArgs = listOf("-XX:MaxPermSize=512m")
}

apply("$rootDir/maven-publish.gradle")
