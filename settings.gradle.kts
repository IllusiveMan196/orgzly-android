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
        google()
        mavenCentral()

        // For org-java
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }

        // For sardine-android
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

include("app")
