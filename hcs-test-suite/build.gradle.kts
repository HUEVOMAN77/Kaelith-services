plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "org.hcs.testsuite"
    compileSdk = 34
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":hcs-emui"))
    implementation(project(":hcs-diagnostics"))
    implementation(project(":hcs-tasks"))
    implementation(project(":hcs-location"))
    implementation(project(":hcs-push"))
    implementation(project(":hcs-auth"))
    implementation(project(":hcs-fido"))
    implementation(project(":hcs-maps"))
    implementation(project(":hcs-webview"))
    implementation(project(":hcs-compat-db"))
    implementation(project(":hcs-update"))
    implementation(project(":hcs-telemetry"))
    implementation(project(":hcs-privileged"))

    testImplementation(project(":hcs-emui"))
    testImplementation(project(":hcs-diagnostics"))
    testImplementation(project(":hcs-tasks"))
    testImplementation(project(":hcs-location"))
    testImplementation(project(":hcs-push"))
    testImplementation(project(":hcs-auth"))
    testImplementation(project(":hcs-fido"))
    testImplementation(project(":hcs-maps"))
    testImplementation(project(":hcs-webview"))
    testImplementation(project(":hcs-compat-db"))
    testImplementation(project(":hcs-update"))
    testImplementation(project(":hcs-telemetry"))
    testImplementation(project(":hcs-privileged"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.junit)
}
