import org.jetbrains.kotlin.konan.properties.hasProperty
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*

plugins {
    application
    kotlin("jvm") version "1.9.21"
    `maven-publish`
}

version = "1.1.0"

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.21")
    }
}

tasks.withType(Jar::class.java) {
    manifest {
        attributes(mapOf(Pair("Main-Class", "ApplicationKt")))
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

extensions.configure<PublishingExtension> {
    val version = project.property("version") as String
    publications {
        create("mavenJava", MavenPublication::class.java) {
            this.groupId = "sharechat.library"
            this.artifactId = "automator"
            this.version = version
            from(project.components["kotlin"])
        }
    }
    repositories {
        maven {
            name = "ShareChatReleasePackages"
            url = project.uri("https://maven.pkg.github.com/kalpesh83/release-automator")
            credentials {
                username = (System.getenv("GITHUB_USERNAME") ?: getLocalProperty("GITHUB_USERNAME").toString())
                password = (System.getenv("GITHUB_PACKAGE_KEY") ?: getLocalProperty("GITHUB_PACKAGE_KEY").toString())
            }
        }
        mavenLocal()
    }
}

application {
    mainClass.set("ApplicationKt")
}

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}


dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}

fun getLocalProperty(key: String): Any {
    val props = Properties().apply {
        load(rootProject.file("local.properties").reader())
    }
    return props[key] ?: throw Exception("No such property exists: $key")
}