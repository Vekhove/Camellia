package me.lilac.camellia.screen.widget;

import java.util.ArrayList;
import java.util.List;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.input.NumberInput;
import me.lilac.camellia.screen.widget.list.ListInputWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(EnvType.CLIENT)
public class NumberInputWidget extends InputWidget<NumberInput<?>> {

    private NumberWidget numberWidget;

    public NumberInputWidget(NumberInput<?> parameters, Setting<Object> setting) {
        super(parameters, setting);
    }

    public void init() {
        this.children.clear();
        this.numberWidget = new NumberWidget(this.x - 115, 1, 100, 18,
                (Number) this.getCurrentValue(), parameters.getMinValue(), parameters.getMaxValue(), parameters.getStep());
        this.addChild(this.numberWidget);
    }

    @Override
    public Component getDefaultValueText() {
        return Component.literal(String.valueOf(this.getDefaultValue()));
    }

    @Override
    public void save() {
        this.setting.set(this.deserialize(this.numberWidget.value));
    }

    @Environment(EnvType.CLIENT)
    public static class NumberWidget extends AbstractWidget {

        private final EditBox editBox;
        private final Button upButton;
        private final Button downButton;
        private final Number min;
        private final Number max;
        private final Number step;
        private final boolean wholeNumber;
        private Number value;

        public NumberWidget(int x, int y, int width, int height, Number currentValue, Number min, Number max, Number step) {
            super(x, y, width, height, CommonComponents.EMPTY);

            this.editBox = new EditBox(Minecraft.getInstance().font, x, 1, width, height, CommonComponents.EMPTY);
            this.editBox.setValue(String.valueOf(currentValue));
            this.wholeNumber = !this.editBox.getValue().contains(".");
            this.value = currentValue;
            this.min = min;
            this.max = max;
            this.step = step;

            this.editBox.setResponder(str -> {
                if (!str.isEmpty() && !str.matches(this.wholeNumber ? "^-?(0|[1-9]\\d*)$" : "^-?(0|[1-9]\\d*)?(\\.\\d*)?$"))
                    return;

                try {
                    if (this.wholeNumber) {
                        this.value = Integer.parseInt(str);
                    } else {
                        this.value = Double.parseDouble(str);
                    }
                } catch (NumberFormatException ignored) {
                }
            });

            this.upButton = new Button.Builder(Component.literal("\uD83E\uDC39"), (button) -> {
                String value;
                if (this.wholeNumber) {
                    this.addInt();
                    value = String.valueOf(this.value.intValue());
                } else {
                    this.addDouble();
                    value = String.format("%.2f", this.value.doubleValue());
                }

                this.editBox.setValue(value);
            }).pos(x + width + 1, 0).size(16, 10).build();

            this.downButton = new Button.Builder(Component.literal("\uD83E\uDC3B"), (button) -> {
                String value;
                if (this.wholeNumber) {
                    this.takeInt();
                    value = String.valueOf(this.value.intValue());
                } else {
                    this.takeDouble();
                    value = String.format("%.2f", this.value.doubleValue());
                }

                this.editBox.setValue(value);
            }).pos(x + width + 1, 10).size(16, 10).build();
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
            this.editBox.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
            this.upButton.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
            this.downButton.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
        }

        private void addInt() {
            this.value = this.value.intValue() + this.step.intValue();
            this.clampInt();
        }

        private void takeInt() {
            this.value = this.value.intValue() - this.step.intValue();
            this.clampInt();
        }

        private void clampInt() {
            int min = this.min == null ? Integer.MIN_VALUE : this.min.intValue();
            int max = this.max == null ? Integer.MAX_VALUE : this.max.intValue();
            this.value = Math.clamp(this.value.intValue(), min, max);
        }

        private void addDouble() {
            this.value = this.value.doubleValue() + this.step.doubleValue();
            this.clampDouble();
        }

        private void takeDouble() {
            this.value = this.value.doubleValue() - this.step.doubleValue();
            this.clampDouble();
        }

        private void clampDouble() {
            double min = this.min == null ? Double.MIN_VALUE : this.min.doubleValue();
            double max = this.max == null ? Double.MAX_VALUE : this.max.doubleValue();
            this.value = Math.clamp(this.value.doubleValue(), min, max);
        }

        private void clampText() {
            if (this.wholeNumber) {
                this.clampInt();
                this.editBox.setValue(String.valueOf(this.value.intValue()));
            } else {
                this.clampDouble();
                this.editBox.setValue(String.format("%.2f", this.value.doubleValue()));
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
            if (this.editBox.mouseClicked(event, doubled)) {
                this.editBox.setFocused(true);
                return true;
            } else if (this.upButton.mouseClicked(event, doubled)) {
                this.upButton.setFocused(true);
                return true;
            } else if (this.downButton.mouseClicked(event, doubled)) {
                this.downButton.setFocused(true);
                return true;
            }

            return false;
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            this.editBox.mouseMoved(mouseX, mouseY);
            this.upButton.mouseMoved(mouseX, mouseY);
            this.downButton.mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            return this.editBox.mouseReleased(event) || this.upButton.mouseReleased(event) || this.downButton.mouseReleased(event);
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
            return this.editBox.mouseDragged(event, deltaX, deltaY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (this.isMouseOver(mouseX, mouseY)) {
                String value;
                if (this.wholeNumber) {
                    if (verticalAmount > 0) {
                        this.addInt();
                    } else {
                        this.takeInt();
                    }

                    value = String.valueOf(this.value.intValue());
                } else {
                    if (verticalAmount > 0) {
                        this.addDouble();
                    } else {
                        this.takeDouble();
                    }

                    value = String.format("%.2f", this.value.doubleValue());
                }

                this.editBox.setValue(value);
                return true;
            }

            return false;
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            return this.editBox.keyPressed(event);
        }

        @Override
        public boolean keyReleased(KeyEvent event) {
            return this.editBox.keyReleased(event);
        }

        @Override
        public boolean charTyped(CharacterEvent event) {
            return this.editBox.charTyped(event);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.editBox.isMouseOver(mouseX, mouseY) || this.upButton.isMouseOver(mouseX, mouseY)
                    || this.downButton.isMouseOver(mouseX, mouseY) || super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public void setFocused(boolean focused) {
            if (this.editBox.isFocused() && !focused)
                this.clampText();

            if (!focused) {
                this.editBox.setFocused(focused);
                this.upButton.setFocused(focused);
                this.downButton.setFocused(focused);
            }

            super.setFocused(focused);
        }

        @Override
        public void setY(int y) {
            this.editBox.setY(y);
            this.upButton.setY(y - 1);
            this.downButton.setY(y + 9);
            super.setY(y);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {

        }

    }

    @Environment(EnvType.CLIENT)
    public static class NumberListInputWidget extends ListInputWidget<NumberInput.NumberList<?>> {

        public NumberListInputWidget(NumberInput.NumberList<?> parameters, Setting<Object> setting) {
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

            return new NumberWidget(this.x - 150, 1, 100, 18,
                    number, parameters.getMinValue(), parameters.getMaxValue(), parameters.getStep());
        }

        @Override
        public void save() {
            List<Object> value = new ArrayList<>();
            for (Entry element : this.elements) {
                NumberWidget numberWidget = (NumberWidget) element.child().widget();
                if (numberWidget.wholeNumber) {
                    value.add(this.deserialize(numberWidget.value.intValue()));
                } else {
                    value.add(this.deserialize(numberWidget.value.doubleValue()));
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
