package me.lilac.camellia.screen.widget;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.input.ComboBoxInput;
import me.lilac.camellia.screen.widget.combo.ComboBoxWidget;
import me.lilac.camellia.screen.widget.list.ListInputWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;

@Environment(EnvType.CLIENT)
public class ComboBoxInputWidget extends InputWidget<ComboBoxInput<?>> {

    private ComboBoxWidget comboBox;

    public ComboBoxInputWidget(ComboBoxInput<?> parameters, Setting<Object> setting) {
        super(parameters, setting);
    }

    public void init() {
        this.children.clear();

        Either<List<String>, Identifier> entriesOrKey = this.parameters.getEntriesOrRegistryKey();
        List<String> entries = new ArrayList<>();
        if (entriesOrKey.left().isPresent()) {
            entries = entriesOrKey.left().get();
        } else if (entriesOrKey.right().isPresent()) {
            RegistryAccess registryAccess = this.getRegistryAccess();
            Registry<?> registry = registryAccess.lookupOrThrow(ResourceKey.createRegistryKey(entriesOrKey.right().get()));
            for (Identifier id : registry.keySet())
                entries.add(id.toString());
        }

        this.comboBox = new ComboBoxWidget(this.x - 174, 0, 174, 20, (String) this.getCurrentValue(), entries);

        this.addChild(this.comboBox);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (this.comboBox != null)
            return this.comboBox.mouseClicked(event, doubled);

        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.comboBox != null)
            return this.comboBox.mouseDragged(event, deltaX, deltaY);

        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return (this.comboBox != null && this.comboBox.isMouseOver(mouseX, mouseY)) || super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public int getCurrentHeight() {
        return this.comboBox != null && this.comboBox.isShowingEntries() ? this.comboBox.getCurrentHeight() + 20 : super.getCurrentHeight();
    }

    @Override
    public Component getDefaultValueText() {
        return Component.literal((String) this.getDefaultValue());
    }

    @Override
    public void save() {
        this.setting.set(this.deserialize(this.comboBox.getValue()));
    }

    @Override
    public void reset() {
        this.setting.set(this.setting.getDefaultValue());
        this.comboBox.setY(this.getY());
        this.comboBox.updateValue((String) this.getCurrentValue());
    }

    @Environment(EnvType.CLIENT)
    public static class ComboBoxListInputWidget extends ListInputWidget<ComboBoxInput.ComboBoxList<?>> {

        private final Either<List<String>, Identifier> entriesOrKey;

        public ComboBoxListInputWidget(ComboBoxInput.ComboBoxList<?> parameters, Setting<Object> setting) {
            super(parameters, setting);

            this.entriesOrKey = parameters.getEntriesOrRegistryKey();
        }

        @Override
        public AbstractWidget createElement(Object value) {
            List<String> entries = new ArrayList<>();
            if (this.entriesOrKey.left().isPresent()) {
                entries = this.entriesOrKey.left().get();
            } else if (this.entriesOrKey.right().isPresent()) {
                RegistryAccess registryAccess = this.getRegistryAccess();
                Registry<?> registry = registryAccess.lookupOrThrow(ResourceKey.createRegistryKey(this.entriesOrKey.right().get()));
                for (Identifier id : registry.keySet())
                    entries.add(id.toString());
            }

            String currentValue = "";
            if (value instanceof StringRepresentable s) {
                currentValue = s.getSerializedName();
            } else if (value instanceof String s) {
                currentValue = s;
            }

            return new ComboBoxWidget(this.x - 210, 0, 174, 20, value == null ? "" : currentValue, entries);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            for (Entry element : this.elements) {
                ComboBoxWidget comboBox = (ComboBoxWidget) element.child().widget();
                if (comboBox.isMouseOver(mouseX, mouseY) && comboBox.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
                    return true;
            }

            return  false;
        }

        @Override
        public int getCurrentHeight() {
            int height = 28;
            for (Entry element : this.elements) {
                ComboBoxWidget comboBox = (ComboBoxWidget) element.child().widget();
                height += comboBox.isShowingEntries() ? comboBox.getCurrentHeight() + 25 : comboBox.getHeight() + 8;
            }

            return this.expanded ? height + this.getHeight() : super.getCurrentHeight();
        }

        @Override
        public void save() {
            List<Object> objectList = (List<Object>) this.getDefaultValue();
            if (objectList.isEmpty())
                return;

            List<Object> value = new ArrayList<>();
            for (Entry element : this.elements) {
                ComboBoxWidget comboBox = (ComboBoxWidget) element.child().widget();
                if (comboBox.getValue().isEmpty())
                    continue;

                value.add(this.deserialize(comboBox.getValue()));
            }

            this.setting.set(value);
        }

        @Override
        public Component getDefaultValueText() {
            List<Object> objectList = (List<Object>) this.getDefaultValue();
            if (objectList.isEmpty())
                return Component.empty();

            MutableComponent component = Component.empty();
            if (objectList.getFirst() instanceof StringRepresentable) {
                List<StringRepresentable> elements = (List<StringRepresentable>) this.getDefaultValue();

                for (StringRepresentable element : elements)
                    component.append("\n").append(element.getSerializedName());
            } else if (objectList.getFirst() instanceof String) {
                List<String> elements = (List<String>) this.getDefaultValue();
                for (String element : elements)
                    component.append("\n").append(element);
            }

            return component;
        }

    }

}
