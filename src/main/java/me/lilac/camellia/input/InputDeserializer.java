package me.lilac.camellia.input;

import net.minecraft.core.RegistryAccess;

@FunctionalInterface
public interface InputDeserializer<T, U> {

    U deserialize(RegistryAccess registryAccess, T t);

}
