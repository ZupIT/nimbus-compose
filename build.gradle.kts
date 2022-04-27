buildscript {
    val compose_version by extra("1.0.0")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
        classpath("com.android.tools.build:gradle:7.0.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs = setOf(file("lib"))
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}