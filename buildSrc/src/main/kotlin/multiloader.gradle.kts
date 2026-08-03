plugins {
    id("me.modmuss50.mod-publish-plugin")
    id("dev.kikugie.fletching-table")
    id("io.github.bizcub.multiloader")
}

multiloader {
    sc.replacements {
        string(scp >= "26.2") {
            replace("TabNavigationBar", "MenuTabBar")
            replace(".setScreen(", ".gui.setScreen(")
        }
        string(scp >= "26.1") {
            replace("GuiGraphics", "GuiGraphicsExtractor")
            replace("render(", "extractRenderState(")
            replace("renderWidget(", "extractWidgetRenderState(")
            replace("renderContent(", "extractContent(")
            replace(".drawString(", ".text(")
        }
    }

    addSourceSet("testmod")

    setMREnvironment(mrEnvs.clientOnly)
    setCFEnvironment(cfEnvs.client)

    if (isFabric) {
        addDependency(
            dependency = "net.fabricmc:fabric-loader:${getDep("fabric")}"
        )
        addDependency(
            dependency = "net.fabricmc.fabric-api:fabric-api:${getDep("fabric-api")}",
            configurations = arrayOf("compileOnly", "testmodImplementation")
        )
        addDependency(
            dependency = "com.terraformersmc:modmenu:${getDep("modmenu")}",
            repository = "maven.terraformersmc.com/releases",
            configuration = "testmodImplementation"
        )
    }
}
