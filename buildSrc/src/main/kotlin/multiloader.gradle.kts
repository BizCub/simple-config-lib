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
        string(scp >= "1.21.9") {
            replace("Screen.hasShiftDown()", "Minecraft.getInstance().hasShiftDown()")
            replace("render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick)",
                "renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float partialTick)")
            replace("int y = this.lastTop;", "int y = this.getContentY();")
            replace("charTyped(char codePoint, int modifiers)", "charTyped(CharacterEvent characterEvent)")
            replace(".charTyped(codePoint, modifiers)", ".charTyped(characterEvent)")
            replace("keyPressed(int keyCode, int scanCode, int modifiers)", "keyPressed(KeyEvent keyEvent)")
            replace(".keyPressed(keyCode, scanCode, modifiers)", ".keyPressed(keyEvent)")
            replace("mouseReleased(double mouseX, double mouseY, int button)", "mouseReleased(MouseButtonEvent mouseButtonEvent)")
            replace(".mouseReleased(mouseX, mouseY, button)", ".mouseReleased(mouseButtonEvent)")
            replace("mouseClicked(double mouseX, double mouseY, int button)", "mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick)")
            replace(".mouseClicked(mouseX, mouseY, button)", ".mouseClicked(mouseButtonEvent, doubleClick)")
            replace("mouseDragged(double mouseX, double mouseY, int button, double dx, double dy)", "mouseDragged(MouseButtonEvent mouseButtonEvent, double dx, double dy)")
            replace(".mouseDragged(mouseX, mouseY, button, dx, dy)", ".mouseDragged(mouseButtonEvent, dx, dy)")
        }
        string(scp >= "1.21.9", "mb_event") {
            replace("mouseX", "mouseButtonEvent.x()")
            replace("mouseY", "mouseButtonEvent.y()")
        }
        string(scp >= "1.21.6") {
            replace(".renderTooltip(", ".setTooltipForNextFrame(")
            replace("screen.handleComponentClicked(style);", "Screen.defaultHandleClickEvent(style.getClickEvent(), Minecraft.getInstance(), screen);")
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
