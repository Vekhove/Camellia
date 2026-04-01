package me.lilac.camellia.screen.widget;

import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.input.ColorInput;
import me.lilac.camellia.screen.widget.color.ColorPickerWidget;
import me.lilac.camellia.screen.widget.list.ListInputWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(EnvType.CLIENT)
public class ColorInputWidget extends InputWidget<ColorInput<?>> {

    private Color currentValue;

    public ColorInputWidget(ColorInput<?> parameters, Setting<Object> setting) {
        super(parameters, setting);
    }

    @Override
    public void init() {
        this.children.clear();

        this.currentValue = (Color) this.getCurrentValue();
        this.addChild(new ColorSelectorWidget(this.x - 4, 1, 16, 16, this.currentValue, this.parameters.hasAlpha(), (color) -> this.currentValue = color));
    }

    @Override
    public Component getDefaultValueText() {
        Color color = (Color) this.getDefaultValue();
        return this.parameters.hasAlpha() ?
                Component.literal(String.format("#%02X%02X%02X%02X", color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()))
                        .withColor(color.getRGB()) :
                Component.literal(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()))
                        .withColor(color.getRGB());
    }

    @Override
    public void save() {
        this.setting.set(this.deserialize(this.currentValue));
    }

    @Environment(EnvType.CLIENT)
    public static class ColorSelectorWidget extends AbstractWidget {

        private final Consumer<Color> onSelected;
        private final boolean hasAlpha;
        private Color currentValue;
        private ColorPickerWidget colorPickerWidget;

        public ColorSelectorWidget(int x, int y, int width, int height, Color currentValue, boolean hasAlpha, Consumer<Color> onSelected) {
            super(x, y, width, height, CommonComponents.EMPTY);

            this.currentValue = currentValue;
            this.hasAlpha = hasAlpha;
            this.onSelected = onSelected;
        }

        @Override
        public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
            graphics.outline(this.getX() - 15, this.getY(), 20, 20, 0xFF888888);
            graphics.fill(this.getX() - 14, this.getY() + 1, this.getX() + 4, this.getY() + 19, this.currentValue.getRGB());

            if (this.colorPickerWidget != null)
                this.colorPickerWidget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
            if (this.colorPickerWidget != null) {
                if (this.colorPickerWidget.mouseClicked(event, doubled)) {
                    return true;
                } else {
                   this.colorPickerWidget = null;
                }
            }

            if (this.isMouseOver(event.x(), event.y())) {
                this.colorPickerWidget = new ColorPickerWidget(this.getX() - 200, this.getY(), 180, 194, this.currentValue.getRGB(), this.hasAlpha, (rgb) -> {
                    this.currentValue = new Color(rgb, true);
                    this.onSelected.accept(this.currentValue);
                });

                return true;
            }

            return false;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            if (this.colorPickerWidget != null)
                return this.colorPickerWidget.mouseReleased(event);

            return super.mouseReleased(event);
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
            if (this.colorPickerWidget != null)
                return this.colorPickerWidget.mouseDragged(event, deltaX, deltaY);

            return super.mouseDragged(event, deltaX, deltaY);
        }

        @Override
        public boolean keyPressed(KeyEvent keyEvent) {
            return (this.colorPickerWidget != null && this.colorPickerWidget.keyPressed(keyEvent)) || super.keyPressed(keyEvent);
        }

        @Override
        public boolean keyReleased(KeyEvent keyEvent) {
            return (this.colorPickerWidget != null && this.colorPickerWidget.keyReleased(keyEvent)) || super.keyReleased(keyEvent);
        }

        @Override
        public boolean charTyped(CharacterEvent characterEvent) {
            return (this.colorPickerWidget != null && this.colorPickerWidget.charTyped(characterEvent)) || super.charTyped(characterEvent);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return (this.colorPickerWidget != null && this.colorPickerWidget.isMouseOver(mouseX, mouseY) || mouseX > this.getX() - 15 && mouseX < this.getX() + 5
                    && mouseY > this.getY() && mouseY < this.getY() + 20);
        }

        @Override
        public void setFocused(boolean focused) {
            Window window = Minecraft.getInstance().getWindow();
            MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
            if (!focused && !this.isMouseOver(mouseHandler.getScaledXPos(window), mouseHandler.getScaledYPos(window)))
                this.colorPickerWidget = null;

            super.setFocused(focused);
        }

        @Override
        public void setY(int y) {
            super.setY(y);

            if (this.colorPickerWidget != null)
                this.colorPickerWidget.setY(y);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

    }

    @Environment(EnvType.CLIENT)
    public static class ColorListInputWidget extends ListInputWidget<ColorInput.ColorList<?>> {

        public ColorListInputWidget(ColorInput.ColorList<?> parameters, Setting<Object> setting) {
            super(parameters, setting);
        }

        @Override
        public AbstractWidget createElement(Object value) {
            Color color = value == null ? Color.RED : (Color) value;

            return new ColorSelectorWidget(this.x - 40, 0, 180, 194, color, this.parameters.hasAlpha(), (rgb) -> {

            });
        }

        @Override
        public void save() {
            List<Object> value = new ArrayList<>();
            for (Entry element : this.elements) {
                ColorSelectorWidget colorSelectorWidget = (ColorSelectorWidget) element.child().widget();
                value.add(this.deserialize(colorSelectorWidget.currentValue));
            }

            this.setting.set(value);
        }

        @Override
        public Component getDefaultValueText() {
            List<Color> elements = (List<Color>) this.getDefaultValue();
            MutableComponent component = Component.empty();
            for (Color element : elements) {
                Component color = this.parameters.hasAlpha() ?
                        Component.literal(String.format("#%02X%02X%02X%02X", element.getAlpha(), element.getRed(), element.getGreen(), element.getBlue()))
                                .withColor(element.getRGB()) :
                        Component.literal(String.format("#%02X%02X%02X", element.getRed(), element.getGreen(), element.getBlue()))
                                .withColor(element.getRGB());

                component.append("\n").append(color);
            }
            return component;
        }

    }

}
