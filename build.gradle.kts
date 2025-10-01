@file:OptIn(ExperimentalWasmDsl::class)

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.bcv)
    alias(libs.plugins.maven)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
}

kotlin {
    applyDefaultHierarchyTemplate()

    explicitApi()

    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }

    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    wasmJs {
        browser()
        nodejs()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // Coroutines for async operations
            implementation(libs.kotlinx.coroutines.core)

            // DateTime for timestamps
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
//            implementation(compose.ui.test.junit4)
        }

        // Desktop JVM dependencies
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.jna)
            implementation(libs.jna.platform)
        }

        // JavaScript dependencies
        jsMain.dependencies {
            implementation(libs.kotlinx.coroutines.core.js)
        }

        androidMain.dependencies {
            implementation("androidx.startup:startup-runtime:1.2.0")
            implementation("androidx.annotation:annotation:1.9.1")
        }
    }

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}

android {
    namespace = "com.niyajali.clipboard.manager"
    compileSdk = 36

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        abortOnError = false
        warningsAsErrors = false
    }
}

apiValidation {
    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib {
        enabled = true
    }
    nonPublicMarkers.add("kotlin.PublishedApi")
}

// Maven publishing configuration
val artifactId = "cliboard-manager"
val mavenGroup: String by project
val defaultVersion: String by project
val currentVersion = System.getenv("PACKAGE_VERSION") ?: defaultVersion
val desc: String by project
val license: String by project
val creationYear: String by project
val githubRepo: String by project

group = mavenGroup
version = currentVersion

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()
    coordinates(mavenGroup, artifactId, currentVersion)

    // sources publishing is always enabled by the Kotlin Multiplatform plugin
    configure(
        KotlinMultiplatform(
            // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka,
            // where `dokkaHtml` is the name of the Dokka task that should be used as input
            javadocJar = JavadocJar.Dokka("dokkaGenerate")
        )
    )

    pom {
        name = project.name
        description = desc
        inceptionYear = creationYear
        url = "https://github.com/$githubRepo"
        licenses {
            license {
                name = license
                url = "https://github.com/$githubRepo/blob/main/LICENSE"
            }
        }
        developers {
            developer {
                id = "niyajali"
                name = "Sk Niyaj Ali"
                url = "https://github.com/niyajali/"
            }
        }
        scm {
            url = "https://github.com/$githubRepo.git"
            connection = "scm:git:git://github.com/$githubRepo.git"
            developerConnection = "scm:git:git://github.com/$githubRepo.git"
        }
        issueManagement {
            url = "https://github.com/$githubRepo/issues"
        }
    }
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("$rootDir/docs/kdoc"))
    }
}

// Spotless configuration
//val ktlintVersion = "1.4.0"
//apply<SpotlessPlugin>()
//extensions.configure<SpotlessExtension> {
//    kotlin {
//        target("src/**/*.kt")
//        targetExclude("**/build/**/*.kt")
//        ktlint(ktlintVersion).editorConfigOverride(
//            mapOf(
//                "android" to "true",
//                "ktlint_standard_no-wildcard-imports" to "disabled",
//            ),
//        )
//        licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
//    }
//    format("kts") {
//        target("src/**/*.kts")
//        targetExclude("**/build/**/*.kts")
//        // Look for the first line that doesn't have a block comment (assumed to be the license)
//        licenseHeaderFile(rootProject.file("spotless/copyright.kts"), "(^(?![\\/ ]\\*).*$)")
//    }
//    format("xml") {
//        target("src/**/*.xml")
//        targetExclude("**/build/**/*.xml")
//        // Look for the first XML tag that isn't a comment (<!--) or the xml declaration (<?xml)
//        licenseHeaderFile(rootProject.file("spotless/copyright.xml"), "(<[^!?])")
//    }
//}