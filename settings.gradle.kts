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
        google {
            mavenContent {
                includeGroupByRegex(".*google.*")
                includeGroupByRegex(".*android.*")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, "build-cache")
    }
}

rootProject.name = "FossWallet"
include(":app")

val localComposeKitPath = providers.environmentVariable("FOSSWALLET_LOCAL_COMPOSE_KIT").orNull?.takeIf { it.isNotBlank() }
if (localComposeKitPath != null) {
    includeBuild(localComposeKitPath) {
        dependencySubstitution {
            substitute(module("com.github.SeineEloquenz:compose-kit")).using(project(":lib"))
        }
    }
}
