/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins{
    id("io.gitlab.arturbosch.detekt") version "1.20.0"
    id("org.jetbrains.dokka") version "1.5.0"
    id("org.sonarqube") version "3.2.0"
    id("jacoco")
    id("com.android.library") version "7.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.7.10" apply false
    kotlin("jvm") version "1.7.10" apply false
}

buildscript {
    val compose_version by extra("1.3.0")
    val nimbus_core_version by extra("1.0.0-alpha14")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.15.1")
        classpath ("de.mannodermaus.gradle.plugins:android-junit5:1.7.1.1")
    }
}

allprojects {
    apply("$rootDir/detekt.gradle")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "jacoco")

    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }

    afterEvaluate {
        apply("$rootDir/gradle/report/sonarqube.gradle")
        apply("$rootDir/gradle/report/jacoco.gradle")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

apply("$rootDir/gradle/report/jacoco-merge.gradle")