multiloader {
    setMREnvironment(mrEnvs.clientOrServerPrefersBoth)
    setCFEnvironment(cfEnvs.both)

    configureLoaders {
        addSourceSet("testmod")

        if (isFabric) {
            addDependency(
                dependency = "net.fabricmc:fabric-loader:${getDep("fabric")}",
                configurations = arrayOf("compileOnly", "testmodImplementation")
            )
            addDependency(
                dependency = "net.fabricmc.fabric-api:fabric-api:${getDep("fabric-api")}",
                configurations = arrayOf("compileOnly", "testmodImplementation")
            )
            addDependency(
                dependency = "com.terraformersmc:modmenu:${getDep("modmenu")}",
                repository = "maven.terraformersmc.com/releases",
                configuration = "testmodImplementation",
                excludedModules = listOf("eu.pb4")
            )
        }
    }
}
