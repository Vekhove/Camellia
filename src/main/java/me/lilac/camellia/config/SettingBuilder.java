package me.lilac.camellia.config;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import me.lilac.camellia.input.InputType;

public class SettingBuilder<T> implements Setting.Builder<T> {

    private final Codec<T> codec;
    private final String key;
    private final Supplier<T> defaultValueSupplier;
    private boolean hidden;
    private String commentKey;
    private String section;
    private String tab;
    private InputType<?, ?> input;

    public SettingBuilder(String key, Codec<T> codec, T defaultValue) {
        this.codec = codec;
        this.key = key;
        this.defaultValueSupplier = () -> defaultValue;
    }

    @Override
    public Setting.Builder<T> hidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    @Override
    public Setting.Builder<T> comment(String comment) {
        this.commentKey = comment;
        return this;
    }

    @Override
    public Setting.Builder<T> section(String section) {
        this.section = section;
        return this;
    }

    @Override
    public Setting.Builder<T> tab(String tab) {
        this.tab = tab;
        return this;
    }

    @Override
    public Setting.Builder<T> input(InputType<?, ?> input) {
        this.input = input;
        return this;
    }

    @Override
    public Setting<T> build(ConfigHolder holder) {
        BasicSetting<T> setting = new BasicSetting<>(this.key, this.codec, holder);
        setting.defaultValueSupplier = this.defaultValueSupplier;
        setting.hidden = this.hidden;
        setting.commentKey = this.commentKey;
        setting.section = this.section;
        setting.tab = this.tab;
        setting.input = this.input;

        return setting;
    }

    public static <T> SettingBuilder<T> of(String key, Codec<T> codec, T defaultValue) {
        return new SettingBuilder<>(key, codec, defaultValue);
    }

}
