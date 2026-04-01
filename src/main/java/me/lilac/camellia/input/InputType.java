package me.lilac.camellia.input;

import net.minecraft.resources.Identifier;

/**
 * @param <T> The type displayed in the input.
 * @param <U> The type to convert back to when saving.
 */
public abstract class InputType<T, U> {

    protected InputSerializer<U, T> serializer;
    protected InputDeserializer<T, U> deserializer;

    public InputType() {

    }

    public InputType<T, U> serializer(InputSerializer<U, T> serializer) {
        this.serializer = serializer;
        return this;
    }

    public InputType<T, U> deserializer(InputDeserializer<T, U> deserializer) {
        this.deserializer = deserializer;
        return this;
    }

    public InputSerializer<U, T> getSerializer() {
        return this.serializer != null ? this.serializer : u -> (T) u;
    }

    public InputDeserializer<T, U> getDeserializer() {
        return this.deserializer != null ? this.deserializer : (r, t) -> (U) t;
    }

    public abstract Identifier getId();

}
