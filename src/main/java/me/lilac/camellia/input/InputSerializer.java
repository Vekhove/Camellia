package me.lilac.camellia.input;

@FunctionalInterface
public interface InputSerializer<T, U> {

    U serialize(T t);

}
