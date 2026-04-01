package me.lilac.camellia.input;

import net.minecraft.resources.Identifier;

public class CheckBoxInput<T> extends InputType<Boolean, T> {

    public CheckBoxInput() {

    }

    @Override
    public Identifier getId() {
        return Identifier.fromNamespaceAndPath("camellia", "checkbox");
    }

    public static class CheckBoxList<U> extends CheckBoxInput<U> implements ListInput {

        private final int maxEntries;

        public CheckBoxList(int maxEntries) {
            super();

            this.maxEntries = maxEntries;
        }

        @Override
        public int getMaxEntries() {
            return this.maxEntries;
        }

        @Override
        public Identifier getId() {
            return Identifier.fromNamespaceAndPath("camellia", "checkbox_list");
        }

        @Override
        public Identifier getParentId() {
            return Identifier.fromNamespaceAndPath("camellia", "checkbox");
        }

    }

}
