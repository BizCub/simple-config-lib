plugins {
    id("me.modmuss50.mod-publish-plugin")
    id("dev.kikugie.fletching-table")
    id("io.github.bizcub.multiloader")
}

multiloader {
    sc.swaps["render_method"] = when {
        scp >= "26.1" -> "protected void extractWidgetRenderState(GuiGraphicsExtractor"
        scp >= "1.20" -> "protected void renderWidget(GuiGraphics"
        else -> "public void render(PoseStack"
    }

    sc.replacements {
        string(scp >= "26.2") {
            replace("TabNavigationBar", "MenuTabBar")
            replace(".setScreen(", ".gui.setScreen(")
        }
        string(scp >= "26.1") {
            replace("render(", "extractRenderState(")
            replace("renderWidget(", "extractWidgetRenderState(")
            replace(".drawString(", ".text(")
            replace("renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float partialTick)",
                "extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick)")
            replace("gui.render.state", "renderer.state.gui")
            replace("gui/render/state", "renderer/state/gui")
            replace("submitBlitToCurrentLayer", "addBlitToCurrentLayer")
            replace("KeyBindingHelper", "KeyMappingHelper")
            replace("registerKeyBinding", "registerKeyMapping")
            replace("fabric.api.client.keybinding", "fabric.api.client.keymapping")
            replace("SpecialGuiElementRegistry", "PictureInPictureRendererRegistry")
            replace(".vertexConsumers()", ".bufferSource()")
        }
        string(scp >= "26.1", "render_widget") {
            replace("renderWidget(", "extractWidgetRenderState(")
        }
        string(scp >= "26.1", "!graphics") {
            replace("GuiGraphics", "GuiGraphicsExtractor")
        }
        string(scp >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
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
        string(scp >= "1.21.5", "hover_event") {
            replace("(HoverEvent.Action.SHOW_TEXT, ", ".ShowText(")
            replace("(HoverEvent.Action.SHOW_ENTITY, ", ".ShowEntity(")
        }
        string(scp >= "1.21.5", "click_event") {
            replace("(ClickEvent.Action.OPEN_URL, ", ".OpenUrl(")
            replace("(ClickEvent.Action.OPEN_FILE, ", ".OpenFile(")
            replace("(ClickEvent.Action.RUN_COMMAND, ", ".RunCommand(")
            replace("(ClickEvent.Action.SUGGEST_COMMAND, ", ".SuggestCommand(")
            replace("(ClickEvent.Action.CHANGE_PAGE, ", ".ChangePage(")
            replace("(ClickEvent.Action.COPY_TO_CLIPBOARD, ", ".CopyToClipboard(")
        }
        string(scp >= "1.21.4") {
            replace("protected int getScrollbarPosition()", "protected int scrollBarX()")
        }
        string(scp >= "1.21.2") {
            replace("getDisplayName()", "getItemName()")
        }
        string(scp >= "1.20.3", "render_widget") {
            replace("render(", "renderWidget(")
        }
        string(scp >= "1.20") {
            replace("public void updateNarration", "protected void updateWidgetNarration")
        }
    }

    addSourceSet("testmod")

    setMREnvironment(mrEnvs.clientOnly)
    setCFEnvironment(cfEnvs.client)

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
