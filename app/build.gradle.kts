import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

/**
 * Load Orgzly-specific properties from app.properties.
 * File is not included, see sample.app.properties file.
 */
fun updateExtraWithAppProperties() {
    Properties().also { properties ->
        val file = File(rootDir, "app.properties")

        if (file.exists()) {
            logger.info("Loading properties from $file")
            file.inputStream().use { stream ->
                properties.load(stream)
            }

            properties.forEach { k, v ->
                extra[k.toString()] = v
            }

        } else {
            logger.warn("Properties file $file does not exist")
        }
    }
}

fun getAppProperty(key: String, defaultValue: String): String {
    return if (extra.has(key)) {
        extra[key].toString()
    } else {
        defaultValue
    }
}

updateExtraWithAppProperties()


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
}

android {
    namespace = "com.orgzly"

    compileSdk = 36

    defaultConfig {
        // Android 6.0 (Marshmallow)
        minSdk = 23

        // Android 16
        targetSdk = 36

        applicationId = "com.orgzly"

        versionCode = 176
        versionName = "1.8.11-beta.2"

        testInstrumentationRunner = "com.orgzly.android.OrgzlyTestRunner"
        // testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        multiDexEnabled = true

        buildConfigField("String", "DROPBOX_APP_KEY", getAppProperty("dropbox.app_key", "\"\""))
        resValue("string", "dropbox_app_key_schema", getAppProperty("dropbox.app_key_schema", ""))
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

//    testOptions {
//        execution 'ANDROIDX_TEST_ORCHESTRATOR'
//    }

    buildTypes {
        release {
            // Enables code-related app optimization
            isMinifyEnabled = true

            // Enables resource shrinking
            isShrinkResources = true

            proguardFiles(
                // Default file with automatically generated optimization rules
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("boolean", "LOG_DEBUG", "false")

            // signingConfig signingConfigs.debug
        }

        debug {
            buildConfigField("boolean", "LOG_DEBUG", "true")

            buildConfigField("String", "DROPBOX_TOKEN", getAppProperty("dropbox.token", "\"\""))
        }
    }

    flavorDimensions.add("store")

    productFlavors {
        create("premium") {
            buildConfigField("boolean", "IS_DROPBOX_ENABLED", "true")

            buildConfigField("String", "VERSION_NAME_SUFFIX", "\"\"")

            dimension = "store"

            isDefault = true
        }

        create("fdroid") {
            /*
             * Disable Dropbox.
             * Properties file which contains the required API key is not included with the code.
             */
            buildConfigField("boolean", "IS_DROPBOX_ENABLED", "false")

            buildConfigField("String", "VERSION_NAME_SUFFIX", "\" (fdroid)\"")

            dimension = "store"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "plugin.properties"
        }
    }

    lint {
        checkDependencies = true

//        disable.add("MissingTranslation")
//        disable.add("MissingQuantity")
//        disable.add("ImpliedQuantity")
//        disable.add("InvalidPackage")
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.annotation)

    implementation(libs.androidx.multidex)

    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.fragment.ktx)

    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.localbroadcastmanager)
    implementation(libs.androidx.work.runtime.ktx)

    // UI
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.material)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.androidx.room.testing)
    ksp(libs.androidx.room.compiler)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // AndroidX Test
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.contrib)
    androidTestImplementation(libs.androidx.test.espresso.intents)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.uiautomator)

    testImplementation(libs.junit)
    androidTestImplementation(libs.loremipsum)

    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    androidTestImplementation(libs.dagger.hilt.android.testing)
    kspAndroidTest(libs.dagger.hilt.android.compiler)

    implementation(libs.orgzly.org.java)

    implementation(libs.gson)

    implementation(libs.dropbox.core.sdk)
    implementation(libs.dropbox.android.sdk)

    implementation(libs.juniversalchardet)

    implementation(libs.joda.time)

    implementation(libs.glide)

    implementation(libs.sardine) {
        exclude(group = "xpp3", module = "xpp3")
    }

    constraints {
        implementation(libs.okhttp) {
            because("https://github.com/orgzly/orgzly-android/issues/880")
        }
    }

    implementation(libs.okhttp.digest)

    implementation(libs.jgit)
    implementation(libs.jgit.ssh.jsch)
}
