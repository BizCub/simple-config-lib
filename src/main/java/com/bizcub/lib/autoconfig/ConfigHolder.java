package com.bizcub.lib.autoconfig;

import com.bizcub.lib.autoconfig.annotation.Config;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHolder<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(final FieldAttributes f) {
                    if (Component.class.isAssignableFrom(f.getDeclaredClass())) {
                        return true;
                    }
                    if (f.getDeclaredType() instanceof ParameterizedType pt) {
                        for (Type arg : pt.getActualTypeArguments()) {
                            if (arg instanceof Class<?> c && Component.class.isAssignableFrom(c)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }

                @Override
                public boolean shouldSkipClass(final Class<?> clazz) {
                    return false;
                }
            })
            .create();

    private final Class<T> type;
    private final String name;
    private final boolean snakeCaseKeys;
    private T instance;

    ConfigHolder(final Class<T> type) {
        this.type = type;
        Config meta = type.getAnnotation(Config.class);
        if (meta == null) {
            throw new IllegalArgumentException("Class " + type.getName() + " is not marked with @Config");
        }
        this.name = meta.name();
        this.snakeCaseKeys = meta.snakeCaseKeys();
        load();
    }

    public String name() {
        return this.name;
    }

    public boolean snakeCaseKeys() {
        return this.snakeCaseKeys;
    }

    public T get() {
        return this.instance;
    }

    public Class<T> type() {
        return this.type;
    }

    private Path path() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(this.name + ".json");
    }

    public static <T> ConfigHolder<T> register(final Class<T> type) {
        return new ConfigHolder<>(type);
    }

    public void load() {
        Path file = path();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                this.instance = GSON.fromJson(reader, this.type);
            } catch (Exception e) {
                LOGGER.error("Failed to read config {}", file, e);
                this.instance = null;
            }
        }
        if (this.instance == null) {
            this.instance = newDefault();
            save();
        }
    }

    public void save() {
        Path file = path();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(this.instance, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save config {}", file, e);
        }
    }

    private T newDefault() {
        try {
            return this.type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("The @Config class " + this.type.getName()
                    + " must have a public constructor without arguments.", e);
        }
    }

    public void reset() {
        this.instance = newDefault();
    }
}
