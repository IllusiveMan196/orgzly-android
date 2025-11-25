pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {
        mavenLocal()
        google()
        mavenCentral()

        // For sardine-android
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

include("app")
