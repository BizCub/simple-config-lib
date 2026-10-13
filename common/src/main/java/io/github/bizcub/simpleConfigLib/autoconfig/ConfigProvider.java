package io.github.bizcub.simpleConfigLib.autoconfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigProvider {
    private static final Map<Class<?>, Object> INSTANCES = new ConcurrentHashMap<>();

    private ConfigProvider() {}

    @SuppressWarnings("unchecked")
    public static <T> T get(final Class<T> type) {
        return (T) INSTANCES.computeIfAbsent(type, ConfigProvider::defaultProxy);
    }

    public static <T> void set(final Class<T> type, final T instance) {
        if (instance != null) {
            INSTANCES.put(type, instance);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T defaultProxy(final Class<T> type) {
        InvocationHandler handler = InvocationHandler::invokeDefault;
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }
}
