package me.lilac.camellia.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.function.Supplier;
import me.lilac.camellia.input.InputType;

public class BasicSetting<T> implements Setting<T> {

    private final ConfigHolder holder;
    private final Codec<T> codec;
    private final String key;
    private T value;
    protected Supplier<T> defaultValueSupplier;
    protected boolean hidden;
    protected String commentKey;
    protected String section;
    protected String tab;
    protected InputType<?, ?> input;

    public BasicSetting(String key, Codec<T> codec, ConfigHolder holder) {
        this.holder = holder;
        this.codec = codec;
        this.key = key;
    }

    @Override
    public T get() {
        if (this.value == null)
            this.value = this.holder.getOrCreateConfig().get(this);

        return this.value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }

    public void reload() {
        this.value = null;
    }

    @Override
    public void write(JsonObject config) {
        T value = this.value == null ? this.defaultValueSupplier.get() : this.value;
        DataResult<JsonElement> result = this.codec.encode(value, JsonOps.INSTANCE, JsonOps.INSTANCE.empty());
        config.add(key, result.getOrThrow());
    }

    @Override
    public T read(JsonObject config) {
        if (!config.has(this.key))
            return null;

        DataResult<Pair<T, JsonElement>> result = this.codec.decode(JsonOps.INSTANCE, config.get(this.key));
        return result.getOrThrow().getFirst();
    }

    @Override
    public Codec<T> getCodec() {
        return this.codec;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public Supplier<T> getDefaultValue() {
        return this.defaultValueSupplier;
    }

    @Override
    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public String getCommentKey() {
        return this.commentKey;
    }

    @Override
    public String getSection() {
        return this.section;
    }

    @Override
    public String getTab() {
        return this.tab;
    }

    @Override
    public InputType<?, ?> getInput() {
        return this.input;
    }

}
