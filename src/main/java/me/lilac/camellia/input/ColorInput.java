package me.lilac.camellia.input;

import java.awt.Color;
import net.minecraft.resources.Identifier;

public class ColorInput<T> extends InputType<Color, T> {

    private final boolean alpha;

    public ColorInput(boolean alpha) {
        this.alpha = alpha;
    }

    public ColorInput() {
        this.alpha = false;
    }

    public boolean hasAlpha() {
        return this.alpha;
    }

    @Override
    public Identifier getId() {
        return Identifier.fromNamespaceAndPath("camellia", "color");
    }

    public static class ColorList<U> extends ColorInput<U> implements ListInput {

        private final int maxEntries;

        public ColorList(boolean alpha, int maxEntries) {
            super(alpha);

            this.maxEntries = maxEntries;
        }

        public ColorList(boolean alpha) {
            super(alpha);

            this.maxEntries = Integer.MAX_VALUE;
        }

        public ColorList() {
            super(false);

            this.maxEntries = Integer.MAX_VALUE;
        }

        @Override
        public int getMaxEntries() {
            return this.maxEntries;
        }

        @Override
        public Identifier getId() {
            return Identifier.fromNamespaceAndPath("camellia", "color_list");
        }

        @Override
        public Identifier getParentId() {
            return Identifier.fromNamespaceAndPath("camellia", "color");
        }

    }

}
