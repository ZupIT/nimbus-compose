pluginManagement {
    repositories {
        mavenLocal()
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "Nimbus Compose"
include(":compose")
include(":compose-sample")
include(":processor")
// include(":annotation")
