plugins {
    multiloader
    `maven-publish`
    alias(libs.plugins.loom)
}

multiloader {
    setBuiltFile(tasks.named<AbstractArchiveTask>(fabricJarTask).get().archiveFile)

    dependencies {
        minecraft("com.mojang:minecraft:${mod.mcExact}")
        if (isObfuscated) "mappings"(loom.officialMojangMappings())
    }

    loom {
        if (isMainCTFileExist())
            accessWidenerPath.set(ctFabricFile)

        if (isObfuscated)
            createRemapConfigurations(sourceSets["testmod"])

        runConfigs {
            configureEach {
                sourceSet = "testmod"
            }
            getByName("client") {
                runDirectory.set(clientRunFile)
            }
            getByName("server") {
                runDirectory.set(serverRunFile)
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "io.github.bizcub"
            artifactId = "lib"
            version = multiloader.mod.version
            from(components["java"])
        }
    }
}
