package me.lilac.camellia.screen.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.input.InputDeserializer;
import me.lilac.camellia.input.InputSerializer;
import me.lilac.camellia.input.InputType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public abstract class InputWidget<T extends InputType<?, ?>> implements Renderable, GuiEventListener, NarratableEntry {

    protected final T parameters;
    protected final WrappedSetting setting;
    protected final List<PositionedChild> children;
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    public InputWidget(T parameters, Setting<Object> setting) {
        this.parameters = parameters;
        this.setting = new WrappedSetting(setting);
        this.children = new ArrayList<>();
    }

    public void reset() {
        this.setting.set(this.setting.getDefaultValue());
        this.init();
    }

    public int getCurrentHeight() {
        return 20;
    }

    public abstract void init();

    public abstract void save();

    public abstract Component getDefaultValueText();

    protected Object deserialize(Object object) {
        return ((InputDeserializer) this.setting.getInput().getDeserializer()).deserialize(this.getRegistryAccess(), object);
    }

    protected Object serialize(Object object) {
        return ((InputSerializer) this.setting.getInput().getSerializer()).serialize(object);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        for (PositionedChild child : this.children) {
            child.widget.extractRenderState(graphics, mouseX, mouseY, a);
        }

        this.extractWidgetRenderState(graphics, mouseX, mouseY, a);
    }

    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {

    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        for (PositionedChild child : this.children) {
            if (child.widget.isMouseOver(event.x(), event.y()) && child.widget.mouseClicked(event, doubled)) {
                child.widget.setFocused(true);
                return true;
            }
        }

        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (PositionedChild child : this.children)
            child.widget.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for (PositionedChild child : this.children) {
            if (child.widget.mouseReleased(event))
                return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        for (PositionedChild child : this.children) {
            if (child.widget.isFocused() && child.widget.mouseDragged(event, deltaX, deltaY))
                return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (PositionedChild child : this.children) {
            if (child.widget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
                return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        for (PositionedChild child : this.children) {
            if (child.widget.keyPressed(event))
                return true;
        }

        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        for (PositionedChild child : this.children) {
            if (child.widget.keyReleased(event))
                return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        for (PositionedChild child : this.children) {
            if (child.widget.charTyped(event))
                return true;
        }

        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (PositionedChild child : this.children) {
            if (child.widget.isMouseOver(mouseX, mouseY))
                return true;
        }

        return false;
    }

    public void addChild(AbstractWidget child) {
        this.children.add(new PositionedChild(child, child.getX(), child.getY()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object getCurrentValue() {
        return ((InputSerializer) this.setting.getInput().getSerializer()).serialize(this.setting.value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object getDefaultValue() {
        return ((InputSerializer) this.setting.getInput().getSerializer()).serialize(this.setting.getDefaultValue());
    }

    public RegistryAccess getRegistryAccess() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY) : player.registryAccess();
    }

    public void setX(int x) {
        this.x = x;

        for (PositionedChild child : this.children)
            child.widget.setX(child.x + x);
    }

    public void setY(int y) {
        this.y = y;

        for (PositionedChild child : this.children)
            child.widget.setY(child.y + y);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    public void setFocused(boolean focused) {
        for (PositionedChild child : this.children)
            child.widget.setFocused(focused);
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public ScreenRectangle getRectangle() {
        return GuiEventListener.super.getRectangle();
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    public WrappedSetting getSetting() {
        return this.setting;
    }

    public T getParameters() {
        return this.parameters;
    }

    @Environment(EnvType.CLIENT)
    public record PositionedChild(AbstractWidget widget, int x, int y) {

    }

    @Environment(EnvType.CLIENT)
    public static final class WrappedSetting {

        private final Setting<Object> setting;
        private final Object defaultValue;
        private final InputType<?, ?> input;
        private final Consumer<Object> onSet;
        private Object value;

        public WrappedSetting(Setting<Object> setting, Object value, Object defaultValue, InputType<?, ?> input, Consumer<Object> onSet) {
            this.setting = setting;
            this.value = value;
            this.defaultValue = defaultValue;
            this.input = input;
            this.onSet = onSet;
        }

        public WrappedSetting(Setting<Object> setting) {
            this(setting, setting.get(), setting.getDefaultValue().get(), setting.getInput(), setting::set);
        }

        public WrappedSetting(Object value, Object defaultValue, InputType<?, ?> input, Consumer<Object> onSet) {
            this(null, value, defaultValue, input, onSet);
        }

        public Setting<Object> getSetting() {
            return setting;
        }

        public Object getValue() {
            return value;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public InputType<?, ?> getInput() {
            return input;
        }

        public void set(Object value) {
            this.value = value;
            this.onSet.accept(value);
        }

    }

}
