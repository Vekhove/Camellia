package me.lilac.camellia.input;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

public class ComboBoxInput<T> extends InputType<String, T> {

    private final Either<java.util.List<String>, Identifier> entriesOrRegistryKey;

    public ComboBoxInput(java.util.List<String> entries) {
        this.entriesOrRegistryKey = Either.left(entries);
    }

    public ComboBoxInput(StringRepresentable[] entries) {
        java.util.List<String> values = new ArrayList<>();
        for (StringRepresentable value : entries)
            values.add(value.getSerializedName());

        this.entriesOrRegistryKey = Either.left(values);
    }

    public ComboBoxInput(Identifier registryKey) {
        this.entriesOrRegistryKey = Either.right(registryKey);
    }

    public Either<java.util.List<String>, Identifier> getEntriesOrRegistryKey() {
        return this.entriesOrRegistryKey;
    }

    @Override
    public Identifier getId() {
        return Identifier.fromNamespaceAndPath("camellia", "combo_box");
    }

    public static class ComboBoxList<U> extends ComboBoxInput<U> implements ListInput {

        private final int maxEntries;

        public ComboBoxList(java.util.List<String> comboBoxEntries, int maxEntries) {
            super(comboBoxEntries);

            this.maxEntries = maxEntries;
        }

        public ComboBoxList(StringRepresentable[] comboBoxEntries, int maxEntries) {
            this(Arrays.stream(comboBoxEntries).map(StringRepresentable::getSerializedName).toList(), maxEntries);
        }

        public ComboBoxList(StringRepresentable[] comboBoxEntries) {
            this(comboBoxEntries, Integer.MAX_VALUE);
        }

        public ComboBoxList(java.util.List<String> comboBoxEntries) {
            super(comboBoxEntries);

            this.maxEntries = Integer.MAX_VALUE;
        }

        public ComboBoxList(Identifier registryKey, int maxEntries) {
            super(registryKey);

            this.maxEntries = maxEntries;
        }

        public ComboBoxList(Identifier registryKey) {
            super(registryKey);

            this.maxEntries = Integer.MAX_VALUE;
        }

        @Override
        public int getMaxEntries() {
            return this.maxEntries;
        }

        @Override
        public Identifier getId() {
            return Identifier.fromNamespaceAndPath("camellia", "combo_box_list");
        }

        @Override
        public Identifier getParentId() {
            return Identifier.fromNamespaceAndPath("camellia", "combo_box");
        }

    }

}
