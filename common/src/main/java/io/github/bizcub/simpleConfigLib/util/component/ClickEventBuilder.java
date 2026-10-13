package io.github.bizcub.simpleConfigLib.util.component;

import net.minecraft.core.Holder;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.Dialog;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

public final class ClickEventBuilder {

    private ClickEvent event;

    private ClickEventBuilder() {}

    public static ClickEventBuilder create() {
        return new ClickEventBuilder();
    }

    public ClickEventBuilder openUrl(String url) {
        this.event = new ClickEvent.OpenUrl(URI.create(url));
        return this;
    }

    public ClickEventBuilder openFile(String path) {
        this.event = new ClickEvent.OpenFile(path);
        return this;
    }

    public ClickEventBuilder openFile(File file) {
        return openFile(file.getAbsolutePath());
    }

    public ClickEventBuilder openFile(Path path) {
        return openFile(path.toAbsolutePath().toString());
    }

    public ClickEventBuilder runCommand(String command) {
        this.event = new ClickEvent.RunCommand(command);
        return this;
    }

    public ClickEventBuilder suggestCommand(String command) {
        this.event = new ClickEvent.SuggestCommand(command);
        return this;
    }

    public ClickEventBuilder changePage(int page) {
        this.event = new ClickEvent.ChangePage(page);
        return this;
    }

    public ClickEventBuilder copyToClipboard(String value) {
        this.event = new ClickEvent.CopyToClipboard(value);
        return this;
    }

    public ClickEventBuilder showDialog(Holder<Dialog> dialog) {
        this.event = new ClickEvent.ShowDialog(dialog);
        return this;
    }

    public ClickEventBuilder custom(Identifier id, Tag payload) {
        this.event = new ClickEvent.Custom(id, Optional.ofNullable(payload));
        return this;
    }

    public ClickEventBuilder custom(Identifier id) {
        this.event = new ClickEvent.Custom(id, Optional.empty());
        return this;
    }

    public ClickEvent build() {
        if (this.event == null) {
            throw new IllegalStateException("ClickEvent action not set");
        }
        return this.event;
    }
}
