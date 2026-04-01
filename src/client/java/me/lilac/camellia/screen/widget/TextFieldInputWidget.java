package me.lilac.camellia.screen.widget;

import java.util.ArrayList;
import java.util.List;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.input.TextFieldInput;
import me.lilac.camellia.screen.widget.list.ListInputWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(EnvType.CLIENT)
public class TextFieldInputWidget extends InputWidget<TextFieldInput<?>> {

    private EditBox editBox;

    public TextFieldInputWidget(TextFieldInput<?> parameters, Setting<Object> setting) {
        super(parameters, setting);
    }

    public void init() {
        this.children.clear();
        this.editBox = new EditBox(Minecraft.getInstance().font, this.x - 174, 0, 174, 20, CommonComponents.EMPTY);
        this.editBox.setValue((String) this.getCurrentValue());
        this.editBox.setMaxLength(parameters.getMaxValueLength());
        this.addChild(editBox);
    }

    @Override
    public Component getDefaultValueText() {
        return Component.literal((String) this.getDefaultValue());
    }

    @Override
    public void save() {
        this.setting.set(this.deserialize(this.editBox.getValue()));
    }

    @Environment(EnvType.CLIENT)
    public static class TextFieldListInputWidget extends ListInputWidget<TextFieldInput.TextFieldList<?>> {

        public TextFieldListInputWidget(TextFieldInput.TextFieldList<?> parameters, Setting<Object> setting) {
            super(parameters, setting);
        }

        @Override
        public AbstractWidget createElement(Object value) {
            EditBox editBox = new EditBox(Minecraft.getInstance().font, this.x - 210, 0, 174, 20, CommonComponents.EMPTY);
            editBox.setValue(value == null ? "" : (String) value);
            return editBox;
        }

        @Override
        public void save() {
            List<Object> value = new ArrayList<>();
            for (Entry element : this.elements) {
                EditBox editBox = (EditBox) element.child().widget();
                value.add(this.deserialize(editBox.getValue()));
            }

            this.setting.set(value);
        }

        @Override
        public Component getDefaultValueText() {
            List<String> elements = (List<String>) this.getDefaultValue();
            MutableComponent component = Component.empty();
            for (String element : elements)
                component.append("\n").append(element);
            return component;
        }

    }

}
