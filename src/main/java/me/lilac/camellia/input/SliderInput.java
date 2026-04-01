package me.lilac.camellia.input;

import net.minecraft.resources.Identifier;

public class SliderInput<T> extends InputType<Number, T> {

    private final Number minValue;
    private final Number maxValue;
    private final Number step;

    public SliderInput(Number minValue, Number maxValue, Number step) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
    }

    public SliderInput(Number minValue, Number maxValue) {
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
        return Identifier.fromNamespaceAndPath("camellia", "slider");
    }

    public static class SliderList<U> extends SliderInput<U> implements ListInput {

        private final int maxEntries;

        public SliderList(Number sliderMinValue, Number sliderMaxValue, Number sliderStep, int maxEntries) {
            super(sliderMinValue, sliderMaxValue, sliderStep);

            this.maxEntries = maxEntries;
        }

        public SliderList(Number sliderMinValue, Number sliderMaxValue, Number sliderStep) {
            this(sliderMinValue, sliderMaxValue, sliderStep, Integer.MAX_VALUE);
        }

        public SliderList(Number sliderMinValue, Number sliderMaxValue) {
            this(sliderMinValue, sliderMaxValue, 1);
        }

        @Override
        public int getMaxEntries() {
            return this.maxEntries;
        }

        @Override
        public Identifier getId() {
            return Identifier.fromNamespaceAndPath("camellia", "slider_list");
        }

    }

}
