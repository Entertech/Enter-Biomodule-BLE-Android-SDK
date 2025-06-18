pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/releases/")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/releases/")
        }
    }
}

rootProject.name = "Enter-Biomodule-BLE-Android-SDK"
include(":demo")



