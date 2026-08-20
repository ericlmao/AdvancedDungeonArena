pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "AdvancedDungeonArena"

include("Lib")
include("API")
include("Core")
include("NMS")
include("MC_1_21_11")
