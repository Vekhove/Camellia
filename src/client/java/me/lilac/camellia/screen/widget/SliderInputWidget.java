package me.lilac.camellia.screen.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.input.SliderInput;
import me.lilac.camellia.screen.widget.list.ListInputWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(EnvType.CLIENT)
public class SliderInputWidget extends InputWidget<SliderInput<?>> {

    private Slider slider;
    private Number value;

    public SliderInputWidget(SliderInput<?> parameters, Setting<Object> setting) {
        super(parameters, setting);
    }

    @Override
    public void init() {
        this.children.clear();

        this.value = (Number) this.getCurrentValue();
        double min = this.parameters.getMinValue().doubleValue();
        double max = this.parameters.getMaxValue().doubleValue();
        double percentage = (value.doubleValue() - min) / (max - min);
        this.slider = new Slider(this.x - 118, 1, 120, 18, CommonComponents.EMPTY,
                min, max, this.parameters.getStep().doubleValue(), value, percentage, (v) -> value = v);
        this.addChild(this.slider);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        if (this.slider != null)
            this.slider.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
    }

    @Override
    public Component getDefaultValueText() {
        return Component.literal(String.valueOf(this.getDefaultValue()));
    }

    @Override
    public void save() {
        this.setting.set(this.deserialize(this.value));
    }

    @Environment(EnvType.CLIENT)
    public static class Slider extends AbstractSliderButton {

        private final Consumer<Number> onApply;
        private final Number min;
        private final Number max;
        private final Number step;
        private final boolean wholeNumber;

        public Slider(int x, int y, int width, int height, Component text, double min, double max, double step, Number initialValue, double initialPercentage, Consumer<Number> onApply) {
            super(x, y, width, height, text, initialPercentage);

            this.min = min;
            this.max = max;
            this.step = step;
            this.wholeNumber = !(initialValue instanceof Float || initialValue instanceof Double);
            this.onApply = onApply;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            String value;
            if (this.wholeNumber) {
                double normalizedValue = this.min.intValue() + (this.value * (this.max.intValue() - this.min.intValue()));
                value = String.valueOf((int) normalizedValue);
            } else {
                double normalizedValue = this.min.doubleValue() + (this.value * (this.max.doubleValue() - this.min.doubleValue()));
                value = String.format("%.2f", normalizedValue);
            }

            this.setMessage(Component.literal(value));
        }

        @Override
        protected void applyValue() {
            if (this.wholeNumber) {
                int min = this.min.intValue();
                int max = this.max.intValue();
                int step = this.step.intValue();
                double value = min + (this.value * (max - min));
                double snapped = Math.round(value / step) * step;
                snapped = Math.max(min, Math.min(max, snapped));
                this.value = (snapped - min) / (max - min);
                this.onApply.accept((int) snapped);
            } else {
                double min = this.min.doubleValue();
                double max = this.max.doubleValue();
                double step = this.step.doubleValue();
                double value = min + (this.value * (max - min));
                double snapped = Math.round(value / step) * step;
                snapped = Math.max(min, Math.min(max, snapped));
                this.value = (snapped - min) / (max - min);
                this.onApply.accept(snapped);
            }
        }

        public int getIntegerValue() {
            int min = this.min.intValue();
            int max = this.max.intValue();
            int step = this.step.intValue();
            double value = min + (this.value * (max - min));
            double snapped = Math.round(value / step) * step;
            return (int) Math.max(min, Math.min(max, snapped));
        }

        public double getDoubleValue() {
            double min = this.min.doubleValue();
            double max = this.max.doubleValue();
            double step = this.step.doubleValue();
            double value = min + (this.value * (max - min));
            double snapped = Math.round(value / step) * step;
            return Math.max(min, Math.min(max, snapped));
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            if (!this.isFocused())
                return false;

            if (this.isValidClickButton(event.buttonInfo())) {
                this.onRelease(event);
                return true;
            } else {
                return false;
            }
        }

    }

    @Environment(EnvType.CLIENT)
    public static class SliderListInputWidget extends ListInputWidget<SliderInput.SliderList<?>> {

        public SliderListInputWidget(SliderInput.SliderList<?> parameters, Setting<Object> setting) {
            super(parameters, setting);
        }

        @Override
        public AbstractWidget createElement(Object value) {
            Number number;
            if (value != null) {
                number = (Number) value;
            }  else {
                String str = String.valueOf(parameters.getStep());
                if (str.contains(".")) {
                    number = 0.0;
                } else {
                    number = 0;
                }
            }

            double min = this.parameters.getMinValue().doubleValue();
            double max = this.parameters.getMaxValue().doubleValue();
            double percentage = (number.doubleValue() - min) / (max - min);
            return new Slider(this.x - 153, 3, 120, 18, CommonComponents.EMPTY,
                    min, max, this.parameters.getStep().doubleValue(), number, percentage, (n) -> {

            });
        }

        @Override
        public void save() {
            List<Object> value = new ArrayList<>();
            for (Entry element : this.elements) {
                Slider slider = (Slider) element.child().widget();
                if (slider.wholeNumber) {
                    value.add(this.deserialize(slider.getIntegerValue()));
                } else {
                    value.add(this.deserialize(slider.getDoubleValue()));
                }
            }

           this.setting.set(value);
        }

        @Override
        public Component getDefaultValueText() {
            List<Number> elements = (List<Number>) this.getDefaultValue();
            MutableComponent component = Component.empty();
            for (Number element : elements)
                component.append("\n").append(String.valueOf(element));
            return component;
        }

    }

}
