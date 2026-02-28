rootProject.name = "StarterProject"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":composeApp")
include(":composeApp:base")
include(":composeApp:core")
include(":composeApp:data:zenn")
include(":composeApp:data:repository")
include(":composeApp:domain:zenn")
include(":composeApp:domain:service")
include(":composeApp:feature:home")
include(":composeApp:feature:user")
include(":composeApp:ui")
