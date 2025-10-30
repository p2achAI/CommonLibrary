pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("com.android.library") version "8.2.2"
        id("org.jetbrains.kotlin.android") version "1.9.24"
        id("org.jetbrains.kotlin.kapt") version "1.9.24"

    }

}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CommonLibrary"
