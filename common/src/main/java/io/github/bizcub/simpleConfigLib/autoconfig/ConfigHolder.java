package io.github.bizcub.simpleConfigLib.autoconfig;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.AutoConfig;
import io.github.bizcub.simpleConfigLib.internal.PlatformHooks;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
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
    private final List<Consumer<T>> changeListeners = new ArrayList<>();
    private String notifiedSnapshot;
    private final T instance;

    ConfigHolder(final Class<T> type) {
        this(type, true);
    }

    private ConfigHolder(final Class<T> type, final boolean load) {
        this.type = type;
        this.meta = type.getAnnotation(AutoConfig.class);
        if (this.meta == null) {
            throw new IllegalArgumentException("Class " + type.getName() + " is not marked with @AutoConfig");
        }
        this.instance = newDefault();
        if (load) load();
        this.notifiedSnapshot = snapshot();
    }

    public ConfigHolder<T> draft() {
        ConfigHolder<T> draft = new ConfigHolder<>(this.type, false);
        draft.applySnapshot(snapshot());
        return draft;
    }

    public T defaults() {
        return newDefault();
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

    public String id() {
        return this.type.getName();
    }

    private Path path() {
        String file = this.meta.fileName().isEmpty()
                ? this.meta.name()
                : this.meta.fileName();
        return PlatformHooks.get().gameDir().resolve("config").resolve(file + ".json");
    }

    @SuppressWarnings("unchecked")
    public static <T> ConfigHolder<T> register(final Class<T> type) {
        return (ConfigHolder<T>) REGISTRY.computeIfAbsent(type, ConfigHolder::new);
    }

    public static ConfigHolder<?> byId(final String id) {
        synchronized (REGISTRY) {
            for (ConfigHolder<?> holder : REGISTRY.values()) {
                if (holder.type.getName().equals(id)) return holder;
            }
        }
        return null;
    }

    public static void applyServerSnapshot(String id, String json) {
        ConfigHolder<?> holder = byId(id);
        if (holder == null) return;
        ConfigSide env = holder.getMeta().side();
        if (env == ConfigSide.SERVER || env == ConfigSide.COMMON) {
            holder.applySnapshot(json);
        }
    }

    public ConfigHolder<T> onSave(final Consumer<T> listener) {
        if (listener != null && !this.saveListeners.contains(listener)) {
            this.saveListeners.add(listener);
        }
        return this;
    }

    public ConfigHolder<T> onChange(final Consumer<T> listener) {
        if (listener != null && !this.changeListeners.contains(listener)) {
            this.changeListeners.add(listener);
        }
        return this;
    }

    public void notifyChanges() {
        String current = snapshot();
        if (!current.equals(this.notifiedSnapshot)) {
            this.notifiedSnapshot = current;
            notifyListeners(this.changeListeners);
        }
    }

    public void load() {
        Path file = path();
        T loaded = null;
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                loaded = GSON.fromJson(reader, this.type);
                GSON.toJson(loaded);
            } catch (Exception e) {
                loaded = null;
                LOGGER.error("Failed to read config {}", file, e);
            }
        }
        T defaults = newDefault();
        boolean corrupted = loaded != null && sanitize(loaded, defaults);
        if ((loaded == null || corrupted) && Files.exists(file)) backup();
        copyInto(loaded != null ? loaded : defaults, this.instance);
        if ((loaded == null || corrupted)
                && !(this.meta.side() == ConfigSide.SERVER && PlatformHooks.get().isPhysicalClient())) {
            write(this.instance);
        }
        notifyChanges();
    }

    public void reset() {
        copyInto(newDefault(), this.instance);
        notifyChanges();
    }

    public static List<ConfigHolder<?>> registered() {
        synchronized (REGISTRY) {
            return new ArrayList<>(REGISTRY.values());
        }
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

    public boolean applySnapshot(final String json) {
        T loaded = parse(json);
        if (loaded == null) return false;
        copyInto(loaded, this.instance);
        notifyChanges();
        return true;
    }

    private T parse(final String json) {
        try {
            T loaded = GSON.fromJson(json, this.type);
            if (loaded == null || sanitize(loaded, newDefault())) return null;
            GSON.toJson(loaded);
            return loaded;
        } catch (Exception e) {
            LOGGER.error("Failed to parse config snapshot for {}", this.meta.name(), e);
            return null;
        }
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

    private static Path nextBackupPath(final Path file) {
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

    public boolean save() {
        if (!write(this.instance)) return false;
        notifyChanges();
        notifyListeners(this.saveListeners);
        return true;
    }

    public boolean saveSnapshot(final String json) {
        T loaded = parse(json);
        if (loaded == null || !write(loaded)) return false;
        copyInto(loaded, this.instance);
        notifyChanges();
        notifyListeners(this.saveListeners);
        return true;
    }

    private boolean write(final T value) {
        Path file = path();
        Path temporary = null;
        try {
            String json = GSON.toJson(value);
            Files.createDirectories(file.getParent());
            temporary = Files.createTempFile(file.getParent(), file.getFileName() + ".", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                writer.write(json);
            }
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to save config {}", file, e);
            return false;
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (Exception e) {
                    LOGGER.warn("Failed to remove temporary config {}", temporary, e);
                }
            }
        }
    }

    private void notifyListeners(final List<Consumer<T>> listeners) {
        for (Consumer<T> listener : List.copyOf(listeners)) {
            try {
                listener.accept(this.instance);
            } catch (Exception e) {
                LOGGER.error("Config update listener failed for {}", this.meta.name(), e);
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
