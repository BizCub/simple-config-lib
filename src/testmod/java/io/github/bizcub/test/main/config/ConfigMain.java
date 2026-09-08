package io.github.bizcub.test.main.config;

public interface ConfigMain {
    static ConfigMain get() {
        return Holder.INSTANCE;
    }

    static void set(final ConfigMain config) {
        if (config != null) {
            Holder.INSTANCE = config;
        }
    }

    class Holder {
        private static ConfigMain INSTANCE = new ConfigMain() { };
    }

    default boolean testBoolean() {
        return false;
    }
}
