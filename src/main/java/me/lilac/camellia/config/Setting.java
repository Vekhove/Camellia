package me.lilac.camellia.config;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import me.lilac.camellia.input.InputType;

public interface Setting<T> {

    /**
     * Writes the setting to the given config.
     *
     * @param config The json object to write to.
     */
    void write(JsonObject config);

    /**
     * Reads the setting from the given config and returns it.
     *
     * @param config The json object to read from.
     * @return The setting value.
     */
    T read(JsonObject config);

    /**
     * @return The serializer for this setting.
     */
    Codec<T> getCodec();

    /**
     * @return The key name of this setting.
     */
    String getKey();

    /**
     * @return A new instance of the default value.
     */
    Supplier<T> getDefaultValue();

    /**
     * @return True if this setting should not be displayed.
     */
    boolean isHidden();

    /**
     * @return The translation key of the comment detailing this setting.
     */
    String getCommentKey();

    /**
     * @return The section that this setting is in.
     */
    String getSection();

    /**
     * @return The tab that this setting is in.
     */
    String getTab();


    /**
     * @return The input type for displaying the setting.
     */
    InputType<?, ?> getInput();

    /**
     * @return The value of this setting from the backing {@link Config}.
     * @throws UnsupportedOperationException if this setting is not backed by a config.
     */
    default T get() {
        throw new UnsupportedOperationException("get() is not supported for this setting, missing backing config");
    }

    /**
     * @return The value of the setting from the given config.
     */
    default T get(Config config) {
        return config.get(this);
    }

    default void set(T value) {
        throw new UnsupportedOperationException("set() is not supported for this setting, missing backing config");
    }

    default void set(T value, Config config) {
        config.set(this, value);
    }

    /**
     * Creates a Setting with a given default value supplier.
     *
     * @param name The name of the setting.
     * @param serializer The serializer for the setting.
     * @param defaultValueSupplier The default value supplier to return a new default value instance.
     * @param comments Comments describing the setting.
     * @return A new Setting.
     * @param <T> The type of value this setting is for.
     */
    static <T> Setting<T> of(String name, Codec<T> codec, ConfigHolder holder) {
        return new BasicSetting<>(name.toLowerCase(), codec, holder);
    }

    static <T> Builder<T> builder(Codec<T> codec, String key, T defaultValue) {
        return new SettingBuilder<>(key, codec, defaultValue);
    }

    interface Builder<T> {

        Builder<T> hidden(boolean hidden);

        Builder<T> comment(String comment);

        Builder<T> section(String section);

        Builder<T> tab(String tab);

        Builder<T> input(InputType<?, ?> input);

        Setting<T> build(ConfigHolder holder);

    }

}
