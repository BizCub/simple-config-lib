pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.architectury.dev")
        maven("https://maven.fabricmc.net")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9+"
    id("com.bizcub.multiloader") version "0.7+"
}

multiloader {
    match("26.1.2", fb, fg, nf)
}
