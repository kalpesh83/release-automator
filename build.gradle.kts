import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

plugins {
    application
    id("org.jetbrains.kotlin.jvm")
    `maven-publish`
}

version = "1.2.3"

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


dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.lordcodes.turtle:turtle:0.10.0")
}

tasks.test {
    useJUnitPlatform()
}
//kotlin {
//    jvmToolchain(17)
//}
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs.add("-Xcontext-receivers")
        freeCompilerArgs.add("-Xjdk-release=17")
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

fun getLocalProperty(key: String): Any {
    val props = Properties().apply {
        load(rootProject.file("local.properties").reader())
    }
    return props[key] ?: throw Exception("No such property exists: $key")
}