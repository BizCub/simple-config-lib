plugins {
    multiloader
    alias(libs.plugins.neoforged)
}

multiloader {
    setBuiltFile(tasks.jar.get().archiveFile)

    neoForge {
        version = getDep("neoforge")

        if (isMainCTFileExist())
            accessTransformers.from(atNeoForgeFile)

        mods {
            register("main") { sourceSet(sourceSets.main.get()) }
            register("testmod") { sourceSet(sourceSets.testmod.get()) }
        }

        runs {
            configureEach {
                disableIdeRun()
            }
            register("client") {
                gameDirectory.set(clientRunFile)
                client()
            }
            register("server") {
                gameDirectory.set(serverRunFile)
                server()
            }
        }
    }
}
