package me.lilac.camellia.screen.widget;

import java.util.ArrayList;
import java.util.List;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.input.CheckBoxInput;
import me.lilac.camellia.screen.widget.list.ListInputWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(EnvType.CLIENT)
public class CheckBoxInputWidget extends InputWidget<CheckBoxInput<?>> {

    private Checkbox checkbox;

    public CheckBoxInputWidget(CheckBoxInput<?> parameters, Setting<Object> setting) {
        super(parameters, setting);
    }

    @Override
    public void init() {
        this.children.clear();
        this.checkbox = Checkbox.builder(CommonComponents.EMPTY, Minecraft.getInstance().font)
                .selected((boolean) this.getCurrentValue())
                .pos(this.x - 14, 1)
                .build();
        this.addChild(this.checkbox);
    }

    @Override
    public Component getDefaultValueText() {
        return Component.literal(String.valueOf(((boolean) this.getDefaultValue())));
    }

    @Override
    public void save() {
        this.setting.set(this.deserialize(checkbox.selected()));
    }

    @Environment(EnvType.CLIENT)
    public static class CheckBoxListInputWidget extends ListInputWidget<CheckBoxInput.CheckBoxList<?>> {

        public CheckBoxListInputWidget(CheckBoxInput.CheckBoxList<?> parameters, Setting<Object> setting) {
            super(parameters, setting);
        }

        @Override
        public AbstractWidget createElement(Object value) {
            return Checkbox.builder(CommonComponents.EMPTY, Minecraft.getInstance().font)
                    .selected(value != null && (Boolean) value)
                    .pos(this.x - 50, 1)
                    .build();
        }

        @Override
        public void save() {
            List<Object> value = new ArrayList<>();
            for (Entry element : this.elements) {
                Checkbox checkbox = (Checkbox) element.child().widget();
                value.add(this.deserialize(checkbox.selected()));
            }

            this.setting.set(value);
        }

        @Override
        public Component getDefaultValueText() {
            List<Boolean> elements = (List<Boolean>) this.getDefaultValue();
            MutableComponent component = Component.empty();
            for (Boolean element : elements)
                component.append("\n").append(String.valueOf(element));
            return component;
        }

    }

}
