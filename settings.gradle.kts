pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HCS"

include(":hcs-companion")
include(":hcs-api-compat")
include(":hcs-tasks")
include(":hcs-location")
include(":hcs-push")
include(":hcs-auth")
include(":hcs-maps")
include(":hcs-webview")
include(":hcs-fido")
include(":hcs-diagnostics")
include(":hcs-compat-db")
include(":hcs-update")
include(":hcs-telemetry")
include(":hcs-emui")
include(":hcs-privileged")
include(":hcs-privileged:hcs-gms-bridge")
include(":hcs-shizuku")
include(":hcs-proxy")
include(":hcs-benchmark")
include(":hcs-distributor-installer")
include(":hcs-fido-biometrics")
include(":hcs-offline-profiles")
include(":hcs-test-suite")
