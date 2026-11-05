multiloader {
    setMREnvironment(mrEnvs.clientOrServerPrefersBoth)
    setCFEnvironment(cfEnvs.both)

    configureLoaders {
        addSourceSet("testmod")

        if (isFabric) {
            addDependency(
                dependency = "net.fabricmc:fabric-loader:${getDep("fabric")}",
                configuration = "implementation"
            )
            addDependency(
                dependency = "net.fabricmc.fabric-api:fabric-api:${getDep("fabric-api")}",
                configuration = "implementation"
            )
            addDependency(
                dependency = "com.terraformersmc:modmenu:${getDep("modmenu")}",
                repository = "maven.terraformersmc.com/releases",
                configuration = "implementation",
                excludedModules = listOf("eu.pb4")
            )
        }
    }
}
