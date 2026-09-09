package io.github.bizcub.simpleConfigLib.autoconfig;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.AutoConfig;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Side;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;

public class ConfigHolder<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Class<?>, ConfigHolder<?>> REGISTRY = Collections.synchronizedMap(new LinkedHashMap<>());
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
    private final AutoConfig meta;
    private final List<Consumer<T>> saveListeners = new ArrayList<>();
    private T instance;

    ConfigHolder(final Class<T> type) {
        this.type = type;
        this.meta = type.getAnnotation(AutoConfig.class);
        if (this.meta == null) {
            throw new IllegalArgumentException("Class " + type.getName() + " is not marked with @Config");
        }
        load();
    }

    public AutoConfig getMeta() {
        return this.meta;
    }

    public T get() {
        return this.instance;
    }

    public Class<T> type() {
        return this.type;
    }

    private Path path() {
        return SimpleConfigLibMain.gameDir().resolve("config").resolve(this.meta.name() + ".json");
    }

    @SuppressWarnings("unchecked")
    public static <T> ConfigHolder<T> register(final Class<T> type) {
        return (ConfigHolder<T>) REGISTRY.computeIfAbsent(type, ConfigHolder::new);
    }

    public static ConfigHolder<?> byName(final String name) {
        for (ConfigHolder<?> holder : REGISTRY.values()) {
            if (holder.meta.name().equals(name)) return holder;
        }
        return null;
    }

    public static void applyServerSnapshot(String name, String json) {
        ConfigHolder<?> holder = byName(name);
        if (holder != null && holder.getMeta().env() == Side.Env.SERVER) {
            holder.applySnapshot(json);
        }
    }

    public ConfigHolder<T> onSave(final Consumer<T> listener) {
        if (listener != null && !this.saveListeners.contains(listener)) {
            this.saveListeners.add(listener);
        }
        return this;
    }

    public void load() {
        Path file = path();
        T loaded = null;
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                loaded = GSON.fromJson(reader, this.type);
            } catch (Exception e) {
                LOGGER.error("Failed to read config {}", file, e);
            }
        }

        T defaults = newDefault();

        if (loaded == null) {
            if (Files.exists(file)) {
                backup();
            }
            if (this.instance == null) {
                this.instance = defaults;
            } else {
                copyInto(defaults, this.instance);
            }
            save();
            return;
        }

        boolean corrupted = sanitize(loaded, defaults);
        if (corrupted) {
            backup();
        }

        if (this.instance == null) {
            this.instance = loaded;
        } else {
            copyInto(loaded, this.instance);
        }

        if (corrupted) {
            save();
        }
    }

    public void reset() {
        T defaults = newDefault();
        if (this.instance == null) {
            this.instance = defaults;
        } else {
            copyInto(defaults, this.instance);
        }
    }

    public static List<ConfigHolder<?>> registered() {
        synchronized (REGISTRY) {
            return new ArrayList<>(REGISTRY.values());
        }
    }

    public Side.Env envOf(Field field) {
        Side side = field.getAnnotation(Side.class);
        return side != null ? side.value() : this.meta.env();
    }

    private boolean sanitize(final T loaded, final T defaults) {
        boolean corrupted = false;
        for (Field f : this.type.getDeclaredFields()) {
            int mods = f.getModifiers();
            if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
                continue;
            }
            try {
                f.setAccessible(true);
                if (f.get(loaded) == null && f.get(defaults) != null) {
                    f.set(loaded, f.get(defaults));
                    corrupted = true;
                    LOGGER.warn("Config {} field '{}' had an invalid value; reset to default",
                            this.meta.name(), f.getName());
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("Failed to sanitize config field {}", f.getName(), e);
            }
        }
        return corrupted;
    }

    public void applySnapshot(final String json) {
        T loaded;
        try {
            loaded = GSON.fromJson(json, this.type);
        } catch (Exception e) {
            LOGGER.error("Failed to parse config snapshot for {}", this.meta.name(), e);
            return;
        }
        if (loaded == null) return;

        T defaults = newDefault();
        sanitize(loaded, defaults);

        if (this.instance == null) this.instance = loaded;
        else copyInto(loaded, this.instance);
    }

    private void backup() {
        Path file = path();
        if (!Files.exists(file)) {
            return;
        }
        Path bak = nextBackupPath(file);
        try {
            Files.copy(file, bak, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.warn("Config {} contained invalid values; original saved to {}", file, bak);
        } catch (Exception e) {
            LOGGER.error("Failed to back up config {}", file, e);
        }
    }

    private Path nextBackupPath(final Path file) {
        Path dir = file.getParent();
        String base = file.getFileName().toString() + ".bak";
        int n = 1;
        Path candidate = dir.resolve(base + n);
        while (Files.exists(candidate)) {
            n++;
            candidate = dir.resolve(base + n);
        }
        return candidate;
    }

    private void copyInto(final T source, final T target) {
        for (Field f : this.type.getDeclaredFields()) {
            int mods = f.getModifiers();
            if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
                continue;
            }
            try {
                f.setAccessible(true);
                f.set(target, f.get(source));
            } catch (IllegalAccessException e) {
                LOGGER.error("Failed to copy config field {}", f.getName(), e);
            }
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
            return;
        }
        for (Consumer<T> listener : this.saveListeners) {
            try {
                listener.accept(this.instance);
            } catch (Exception e) {
                LOGGER.error("Config save listener failed for {}", this.meta.name(), e);
            }
        }
    }

    public String snapshot() {
        return GSON.toJson(this.instance);
    }

    private T newDefault() {
        try {
            return this.type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("The @Config class " + this.type.getName()
                    + " must have a public constructor without arguments.", e);
        }
    }
}
