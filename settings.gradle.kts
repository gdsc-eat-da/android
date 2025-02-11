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
        maven(url= uri("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/"))
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io")}
        maven(url= uri("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/"))
    }
}

rootProject.name = "eat_da"
include(":app")
