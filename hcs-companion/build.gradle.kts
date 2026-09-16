plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "org.hcs.companion"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.hcs.companion"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":hcs-emui"))
    implementation(project(":hcs-diagnostics"))
    implementation(project(":hcs-api-compat"))
    implementation(project(":hcs-tasks"))
    implementation(project(":hcs-push"))
    implementation(project(":hcs-maps"))
    implementation(project(":hcs-compat-db"))
    implementation(project(":hcs-update"))
    implementation(project(":hcs-telemetry"))
    implementation(project(":hcs-privileged"))
    implementation(project(":hcs-shizuku"))
    implementation(project(":hcs-proxy"))
    implementation(project(":hcs-benchmark"))
    implementation(project(":hcs-distributor-installer"))
    implementation(project(":hcs-fido-biometrics"))
    implementation(project(":hcs-offline-profiles"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
