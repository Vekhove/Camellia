package me.lilac.camellia.screen;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.input.CheckBoxInput;
import me.lilac.camellia.input.ColorInput;
import me.lilac.camellia.input.ComboBoxInput;
import me.lilac.camellia.input.InputType;
import me.lilac.camellia.input.NumberInput;
import me.lilac.camellia.input.SliderInput;
import me.lilac.camellia.input.TextFieldInput;
import me.lilac.camellia.screen.widget.CheckBoxInputWidget;
import me.lilac.camellia.screen.widget.ColorInputWidget;
import me.lilac.camellia.screen.widget.ComboBoxInputWidget;
import me.lilac.camellia.screen.widget.InputWidget;
import me.lilac.camellia.screen.widget.NumberInputWidget;
import me.lilac.camellia.screen.widget.SliderInputWidget;
import me.lilac.camellia.screen.widget.TextFieldInputWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@SuppressWarnings("all")
@Environment(EnvType.CLIENT)
public final class InputWidgets {

    private static final Map<Identifier, InputWidgetType<?, ?>> REGISTRY = new HashMap<>();

    public static final InputWidgetType<CheckBoxInputWidget, CheckBoxInput<?>> CHECK_BOX = register(Identifier.fromNamespaceAndPath("camellia", "checkbox"), CheckBoxInputWidget::new);
    public static final InputWidgetType<ColorInputWidget, ColorInput<?>> COLOR = register(Identifier.fromNamespaceAndPath("camellia", "color"), ColorInputWidget::new);
    public static final InputWidgetType<ComboBoxInputWidget, ComboBoxInput<?>> COMBO_BOX = register(Identifier.fromNamespaceAndPath("camellia", "combo_box"), ComboBoxInputWidget::new);
    public static final InputWidgetType<NumberInputWidget, NumberInput<?>> NUMBER = register(Identifier.fromNamespaceAndPath("camellia", "number"), NumberInputWidget::new);
    public static final InputWidgetType<SliderInputWidget, SliderInput<?>> SLIDER = register(Identifier.fromNamespaceAndPath("camellia", "slider"), SliderInputWidget::new);
    public static final InputWidgetType<TextFieldInputWidget, TextFieldInput<?>> TEXT_FIELD = register(Identifier.fromNamespaceAndPath("camellia", "text_field"), TextFieldInputWidget::new);
    public static final InputWidgetType<CheckBoxInputWidget.CheckBoxListInputWidget, CheckBoxInput.CheckBoxList<?>> CHECK_BOX_LIST = register(Identifier.fromNamespaceAndPath("camellia", "checkbox_list"), CheckBoxInputWidget.CheckBoxListInputWidget::new);
    public static final InputWidgetType<ColorInputWidget.ColorListInputWidget, ColorInput.ColorList<?>> COLOR_LIST = register(Identifier.fromNamespaceAndPath("camellia", "color_list"), ColorInputWidget.ColorListInputWidget::new);
    public static final InputWidgetType<ComboBoxInputWidget.ComboBoxListInputWidget, ComboBoxInput.ComboBoxList<?>> COMBO_BOX_LIST = register(Identifier.fromNamespaceAndPath("camellia", "combo_box_list"), ComboBoxInputWidget.ComboBoxListInputWidget::new);
    public static final InputWidgetType<NumberInputWidget.NumberListInputWidget, NumberInput.NumberList<?>> NUMBER_LIST = register(Identifier.fromNamespaceAndPath("camellia", "number_list"), NumberInputWidget.NumberListInputWidget::new);
    public static final InputWidgetType<SliderInputWidget.SliderListInputWidget, SliderInput.SliderList<?>> SLIDER_LIST = register(Identifier.fromNamespaceAndPath("camellia", "slider_list"), SliderInputWidget.SliderListInputWidget::new);
    public static final InputWidgetType<TextFieldInputWidget.TextFieldListInputWidget, TextFieldInput.TextFieldList<?>> TEXT_FIELD_LIST = register(Identifier.fromNamespaceAndPath("camellia", "text_field_list"), TextFieldInputWidget.TextFieldListInputWidget::new);

    public static <T extends InputWidget<U>, U extends InputType<?, ?>> InputWidgetType<T, U> register(Identifier id, BiFunction<U, Setting<Object>, T> function) {
        return (InputWidgetType<T, U>) REGISTRY.put(id, new InputWidgetType<>(function));
    }

    public record InputWidgetType<T extends InputWidget<U>, U extends InputType<?, ?>>(BiFunction<U, Setting<Object>, T> function) {

    }

    public static InputWidget<?> create(Identifier id, InputType<?, ?> data, Setting<?> setting) {
        InputWidgetType<?, ?> entry = REGISTRY.get(id);
        if (entry == null)
            return null;

        return (InputWidget<?>) ((InputWidgetType) entry).function().apply(data, setting);
    }

    public static boolean has(Identifier id) {
        return REGISTRY.containsKey(id);
    }

    public static Map<Identifier, InputWidgetType<?, ?>> getRegistry() {
        return REGISTRY;
    }

}