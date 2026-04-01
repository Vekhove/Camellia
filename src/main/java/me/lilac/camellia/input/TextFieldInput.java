package me.lilac.camellia.input;

import net.minecraft.resources.Identifier;

public class TextFieldInput<T> extends InputType<String, T> {

    private final int maxValueLength;

    public TextFieldInput(int maxLength) {
        this.maxValueLength = maxLength;
    }

    public TextFieldInput() {
        this(-1);
    }

    public int getMaxValueLength() {
        return this.maxValueLength;
    }

    @Override
    public Identifier getId() {
        return Identifier.fromNamespaceAndPath("camellia", "text_field");
    }

    public static class TextFieldList<U> extends TextFieldInput<U> implements ListInput {

        private final int maxEntries;

        public TextFieldList(int maxValueLength, int maxEntries) {
            super(maxValueLength);

            this.maxEntries = maxEntries;
        }

        public TextFieldList(int maxValueLength) {
            this(maxValueLength, Integer.MAX_VALUE);
        }

        public TextFieldList() {
            this(Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEntries() {
            return this.maxEntries;
        }

        @Override
        public Identifier getId() {
            return Identifier.fromNamespaceAndPath("camellia", "text_field_list");
        }

    }

}
