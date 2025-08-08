import org.gradle.accessors.dm.LibrariesForLibs.VersionAccessors
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias { libs.plugins.kotlin.multiplatform }
    alias { libs.plugins.android.library } apply true
    alias { libs.plugins.dokka }
    `maven-publish`
    jacoco
}

private val Project.semver: String by lazy {
    val major = version { project.version.major }
    val minor = version { project.version.minor }
    val patch = version { project.version.patch }
    return@lazy "$major.$minor.$patch"
}

private fun Project.version(selector: VersionAccessors.() -> Provider<String>): String {
    return this@version.libs.versions.selector().get()
}

group = version { project.group.id }
version = semver

tasks.wrapper {
    gradleVersion = "latest"
}

kotlin {
    // Require explicit visibility & return types.
    explicitApi()

    // Enable browsing compiled bytecode in consumer's IDE.
    withSourcesJar(publish = true)

    // JDK version used by compiler & tooling.
    val javaToolchain = version { java.toolchain }
    val javaBytecode = version { java.bytecode }
    jvmToolchain(jdkVersion = javaToolchain.toInt())

    compilerOptions {
        // Free compiler args
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.experimental.ExperimentalTypeInference",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
        )

        // Enable extra K2 warnings.
        extraWarnings = true
    }

    // Define target platforms (and target-specific compiler options, publishing settings, tasks, artefacts, etc).
    jvm {
        // Enable publishing decompiled source code in consumer's IDE.
        isSourcesPublishable = true

        java {
            compilerOptions {
                // Target version of the generated JVM bytecode.
                jvmTarget = JvmTarget.fromTarget(target = javaBytecode)
            }

            sourceCompatibility = JavaVersion.toVersion(javaToolchain)
            targetCompatibility = JavaVersion.toVersion(javaBytecode)
        }
    }
    androidTarget {
        // Enable publishing decompiled source code in consumer's IDE.
        isSourcesPublishable = true

        android {
            compilerOptions {
                jvmTarget = JvmTarget.fromTarget(target = javaBytecode)
            }
            compileOptions {
                sourceCompatibility = JavaVersion.toVersion(javaToolchain)
                targetCompatibility = JavaVersion.toVersion(javaBytecode)
            }
        }
        compilerOptions.jvmTarget = JvmTarget.fromTarget(target = javaBytecode)
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.kotlin.bom))
            implementation(libs.bundles.core)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
        androidMain {
            android {
                namespace = "nz.adjmunro.ast"
                compileSdk = version { project.android.compileSdk }.toInt()
                defaultConfig.minSdk = version { project.android.minSdk }.toInt()
            }
        }
        jvmTest.dependencies {
            implementation(libs.junit5)
        }
    }
}

// The `Test` type is only used for JVM tests.
tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"))
    reports {
        junitXml.required = true
        html.required = true
    }
}

jacoco {
    reportsDirectory = layout.buildDirectory.dir("reports/jacoco")

    tasks.register<JacocoReport>("jacocoTestReport") {
        dependsOn(tasks.named("jvmTest"))
        reports {
            xml.required = true   // XML report for CI summary
            html.required = true  // HTML report for local inspection
            html.outputLocation = layout.buildDirectory.dir("reports/jacoco/html")
        }
    }
}

tasks.register<Jar>("dokkaJar") {
    from(tasks.dokkaGeneratePublicationHtml)
    dependsOn(tasks.dokkaGeneratePublicationHtml)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        register<MavenPublication>(name = "ast-maven-artifact") {
            from(components["kotlin"])
            groupId = version { project.group.id }
            artifactId = version { project.artifact.id }
            version = semver

            // Include Dokka-generated Javadoc in the publication.
            artifact(tasks.named("dokkaJar"))
        }
    }
    repositories {
        // Publishing to local M.2 repository
        mavenLocal()

        // Publishing to GitHub Packages
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/adjmunro/project-ast")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }

        // TODO Publish to Sonatype OSSRH / Maven Central
//        maven {
//            name = "OSSRH"
//            url = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
//            credentials {
//                username = System.getenv("MAVEN_USERNAME")
//                password = System.getenv("MAVEN_PASSWORD")
//            }
//        }
    }
}
