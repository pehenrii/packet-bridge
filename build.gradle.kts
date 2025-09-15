plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("com.diffplug.spotless") version "7.0.2"
    id("com.gradleup.shadow") version "8.3.6"
}

group = "com.pehenrii"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.lettuce)

    implementation(libs.zstd)
    implementation(libs.guava)

    compileOnly(libs.checker)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.mockito)
    testImplementation(libs.junit)
    testImplementation(libs.test.containers)
}

tasks {
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    spotless {
        java {
            removeUnusedImports()
            formatAnnotations()
        }
    }

    named("build") {
        dependsOn("shadowJar")
    }

    named("shadowJar") {
        dependsOn("spotlessApply")
    }

    withType<Test> {
        useJUnitPlatform()
    }
}