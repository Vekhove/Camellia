package me.lilac.camellia.input;

import net.minecraft.resources.Identifier;

public class NumberInput<T> extends InputType<Number, T> {

    private final Number minValue;
    private final Number maxValue;
    private final Number step;

    public NumberInput(Number minValue, Number maxValue, Number step) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
    }

    public NumberInput(Number minValue, Number maxValue) {
        this(minValue, maxValue, 1);
    }

    public Number getMinValue() {
        return this.minValue;
    }

    public Number getMaxValue() {
        return this.maxValue;
    }

    public Number getStep() {
        return this.step;
    }

    @Override
    public InputDeserializer<Number, T> getDeserializer() {
        return this.deserializer != null ? this.deserializer : (r, n) -> {
            if (this.minValue instanceof Integer)
                return (T) (Integer) n.intValue();
            if (this.minValue instanceof Float)
                return (T) (Float) n.floatValue();
            if (this.minValue instanceof Double)
                return (T) (Double) n.doubleValue();
            if (this.minValue instanceof Long)
                return (T) (Long) n.longValue();
            if (this.minValue instanceof Short)
                return (T) (Short) n.shortValue();
            if (this.minValue instanceof Byte)
                return (T) (Byte) n.byteValue();
            return (T) n;
        };
    }

    @Override
    public Identifier getId() {
        return Identifier.fromNamespaceAndPath("camellia", "number");
    }

    public static class NumberList<U> extends NumberInput<U> implements ListInput {

        private final int maxEntries;

        public NumberList(Number minValue, Number maxValue, Number step, int maxEntries) {
            super(minValue, maxValue, step);

            this.maxEntries = maxEntries;
        }

        public NumberList(Number minValue, Number maxValue, Number step) {
            this(minValue, maxValue, step, Integer.MAX_VALUE);
        }

        public NumberList(Number minValue, Number maxValue) {
            this(minValue, maxValue, 1);
        }

        @Override
        public int getMaxEntries() {
            return this.maxEntries;
        }

        @Override
        public Identifier getId() {
            return Identifier.fromNamespaceAndPath("camellia", "number_list");
        }

        @Override
        public Identifier getParentId() {
            return Identifier.fromNamespaceAndPath("camellia", "number");
        }

    }

}
