plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.maven.publish)
    signing
}

val buildRoot = "$projectDir/../buildscripts"
val prefixRoot = "$buildRoot/prefix"


val archMappings = mapOf(
    "armv7l" to "armeabi-v7a",
    "arm64" to "arm64-v8a",
    "x86" to "x86",
    "x86_64" to "x86_64"
)


tasks.register("organizeNativeLibraries") {
    doLast {
        val nativeLibsDir = file("${project.buildDir}/intermediates/native_libs")
        nativeLibsDir.mkdirs()

        archMappings.forEach { (sourceArch, targetArch) ->
            val sourceDir = file("$prefixRoot/$sourceArch/lib")
            val targetDir = file("$nativeLibsDir/$targetArch")
            targetDir.mkdirs()

            if (sourceDir.exists()) {
                sourceDir.listFiles()?.filter { it.extension == "so" }?.forEach { sourceLib ->
                    sourceLib.copyTo(File(targetDir, sourceLib.name), overwrite = true)
                }
            } else {
                logger.warn("Source directory for $sourceArch does not exist: $sourceDir")
            }
        }
    }
}

android {
    namespace = "dev.marcelsoftware.mpvcompose"
    compileSdk = 36

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {

                cppFlags += ""
                arguments(
                    "-DANDROID_STL=c++_shared",
                    "-DPREFIX32=$prefixRoot/armv7l",
                    "-DPREFIX64=$prefixRoot/arm64",
                    "-DPREFIX_X64=$prefixRoot/x86_64",
                    "-DPREFIX_X86=$prefixRoot/x86"
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("${project.buildDir}/intermediates/native_libs")
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    ktlint(libs.kotlinter.compose.rules)
}

tasks.named("preBuild") {
    dependsOn("organizeNativeLibraries")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }

            groupId = "dev.marcelsoftware.mpvcompose"
            artifactId = "mpv-compose"
            version = "1.0.0"

            pom {
                name.set("MPV Compose")
                description.set("MPV player for Jetpack Compose")
                url.set("https://github.com/nitanmacel/mpv-compose")

                developers {
                    developer {
                        id.set("nitanmarcel")
                        name.set("Marcel Alexandru")
                        email.set("contact@mail.marcelsoftware.dev")
                    }
                }


                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://raw.githubusercontent.com/mpv-android/mpv-android/refs/heads/master/LICENSE")
                    }

                    license {
                        name.set("MIT")
                        url.set("https://raw.githubusercontent.com/nitanmarcel/mpv-compose/refs/heads/master/LICENSE")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/nitanmarcel/mpv-compose.git")
                    developerConnection.set("scm:git:ssh://github.com:nitanmarcel/mpv-compose.git")
                    url.set("https://github.com/nitanmarcel/mpv-compose")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["release"])
}
