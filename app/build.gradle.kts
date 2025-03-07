import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

android {
    compileSdk = 33
    buildToolsVersion = "33.0.2"
    ndkVersion = "25.2.9519653"
    androidResources.generateLocaleConfig = true

    splits {
        abi {
            isEnable = true
            reset()
            if (gradle.startParameter.taskNames.any { it.contains("Release") }) {
                include("arm64-v8a", "x86_64", "armeabi-v7a", "x86")
                isUniversalApk = true
            } else {
                include("arm64-v8a")
            }
        }
    }

    val signConfig = signingConfigs.create("release") {
        storeFile = File(projectDir.path + "/keystore/androidkey.jks")
        storePassword = "000000"
        keyAlias = "key0"
        keyPassword = "000000"

        enableV3Signing = true
        enableV4Signing = true
    }

    val commitSha by lazy {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine = "git rev-parse --short=7 HEAD".split(' ')
            standardOutput = stdout
        }
        stdout.toString().trim()
    }

    val buildTime by lazy {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(ZoneOffset.UTC)
        formatter.format(Instant.now())
    }

    defaultConfig {
        applicationId = "moe.tarsin.ehviewer"
        minSdk = 28
        targetSdk = 33
        versionCode = 180036
        versionName = "1.8.8.0-dev"
        resourceConfigurations.addAll(
            listOf(
                "zh",
                "zh-rCN",
                "zh-rHK",
                "zh-rTW",
                "es",
                "ja",
                "ko",
                "fr",
                "de",
                "th",
                "tr",
                "nb-rNO"
            )
        )
        buildConfigField("String", "COMMIT_SHA", "\"$commitSha\"")
        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")
    }

    externalNativeBuild {
        cmake {
            path = File("src/main/cpp/CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }

    kotlinOptions {
        jvmTarget = "19"
        freeCompilerArgs = listOf(
            // https://kotlinlang.org/docs/compiler-reference.html#progressive
            "-progressive",

            "-opt-in=coil.annotation.ExperimentalCoilApi",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlinx.coroutines.InternalCoroutinesApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
        disable.add("MissingTranslation")
    }

    packaging {
        resources {
            excludes += "/META-INF/**"
            excludes += "/kotlin/**"
            excludes += "**.txt"
            excludes += "**.bin"
        }

    }

    dependenciesInfo.includeInApk = false

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
            signingConfig = signConfig
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }

    namespace = "com.hippo.ehviewer"
}

dependencies {
    // https://developer.android.com/jetpack/androidx/releases/activity
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")
    implementation("androidx.browser:browser:1.5.0")
    implementation("androidx.collection:collection-ktx:1.3.0-alpha02")

    // https://developer.android.com/jetpack/androidx/releases/compose-material3
    // implementation(platform("androidx.compose:compose-bom:2023.01.00"))
    api(platform("dev.chrisbanes.compose:compose-bom:2023.02.00-SNAPSHOT"))
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.core:core-ktx:1.10.0-rc01")

    implementation("androidx.constraintlayout:constraintlayout:2.2.0-alpha08")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    // https://developer.android.com/jetpack/androidx/releases/lifecycle
    implementation("androidx.lifecycle:lifecycle-process:2.6.0")

    // https://developer.android.com/jetpack/androidx/releases/navigation
    val nav_version = "2.5.3"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // https://developer.android.com/jetpack/androidx/releases/paging
    implementation("androidx.paging:paging-runtime-ktx:3.2.0-alpha04")
    implementation("androidx.paging:paging-compose:1.0.0-alpha18")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")

    // https://developer.android.com/jetpack/androidx/releases/room
    val room_version = "2.5.0"
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-paging:$room_version")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("androidx.work:work-runtime-ktx:2.8.0")
    implementation("com.drakeet.drawer:drawer:1.0.3")
    implementation("com.github.chrisbanes:PhotoView:2.3.0") // Dead Dependency
    implementation("com.github.tachiyomiorg:DirectionalViewPager:1.0.0") // Dead Dependency
    // https://github.com/google/accompanist/releases
    implementation("com.google.accompanist:accompanist-themeadapter-material3:0.28.0")
    implementation("com.google.android.material:material:1.8.0")

    // https://square.github.io/okhttp/changelogs/changelog/
    implementation("com.squareup.okhttp3:okhttp-bom:5.0.0-alpha.11")
    implementation("com.squareup.okhttp3:okhttp-coroutines")

    implementation("com.squareup.okio:okio-jvm:3.3.0")

    implementation("dev.chrisbanes.insetter:insetter:0.6.1") // Dead Dependency
    implementation("dev.rikka.rikkax.core:core-ktx:1.4.1")
    implementation("dev.rikka.rikkax.insets:insets:1.3.0")
    implementation("dev.rikka.rikkax.layoutinflater:layoutinflater:1.3.0")
    implementation("dev.rikka.rikkax.preference:simplemenu-preference:1.0.3")
    implementation("dev.rikka.rikkax.material:material-preference:2.0.0")

    implementation(platform("io.arrow-kt:arrow-stack:2.0.0-SNAPSHOT"))
    implementation("io.arrow-kt:arrow-fx-coroutines")

    // https://coil-kt.github.io/coil/changelog/
    implementation("io.coil-kt:coil-compose:2.2.2")

    implementation("io.ktor:ktor-client-okhttp:2.2.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0-Beta")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.5.0")
    implementation("org.jsoup:jsoup:1.15.4")
}

configurations.all {
    exclude("dev.rikka.rikkax.appcompat", "appcompat")
    exclude("dev.rikka.rikkax.material", "material")
    exclude("org.jetbrains.kotlin", "kotlin-android-extensions-runtime")
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
}

ksp {
    arg(RoomSchemaArgProvider(File(projectDir, "schemas")))
}

class RoomSchemaArgProvider(
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemaDir: File
) : CommandLineArgumentProvider {

    override fun asArguments(): Iterable<String> {
        return listOf("room.schemaLocation=${schemaDir.path}")
    }
}
