package me.lilac.camellia;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.config.SettingBuilder;
import me.lilac.camellia.config.SettingHolder;
import me.lilac.camellia.input.CheckBoxInput;
import me.lilac.camellia.input.ComboBoxInput;
import me.lilac.camellia.input.NumberInput;
import me.lilac.camellia.input.SliderInput;
import me.lilac.camellia.input.TextFieldInput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;

public final class SettingsExample implements SettingHolder {

    public static final SettingHolder INSTANCE = new SettingsExample();
    private static final List<Setting<?>> SETTINGS = new ArrayList<>();

    public static final Setting<Boolean> BOOLEAN_EXAMPLE =
            create(SettingBuilder.of("boolean_example", Codec.BOOL, false)
                    .tab("primitives")
                    .input(new CheckBoxInput<>()));

    public static final Setting<Integer> INTEGER_EXAMPLE =
            create(SettingBuilder.of("integer_example", Codec.INT, 5)
                    .tab("primitives")
                    .section("numbers")
                    .input(new NumberInput<>(-10, 10, 1)));

    public static final Setting<Long> LONG_EXAMPLE =
            create(SettingBuilder.of("long_example", Codec.LONG, 25L)
                    .tab("primitives")
                    .section("numbers")
                    .input(new NumberInput<>(0L, 50L, 5L)));

    public static final Setting<Short> SHORT_EXAMPLE =
            create(SettingBuilder.of("short_example", Codec.SHORT, (short) 1)
                    .tab("primitives")
                    .section("numbers")
                    .input(new NumberInput<>((short) 0, (short) 10, (short) 1)));

    public static final Setting<Byte> BYTE_EXAMPLE =
            create(SettingBuilder.of("byte_example", Codec.BYTE, (byte) 4)
                    .tab("primitives")
                    .section("numbers")
                    .input(new NumberInput<>((byte) 0, (byte) 8, (byte) 1)));

    public static final Setting<Double> DOUBLE_EXAMPLE =
            create(SettingBuilder.of("double_example", Codec.DOUBLE, 0.5)
                    .tab("primitives")
                    .section("numbers")
                    .input(new NumberInput<>(-10.0, 10.0, 0.5)));

    public static final Setting<Float> FLOAT_EXAMPLE =
            create(SettingBuilder.of("float_example", Codec.FLOAT, 1.23F)
                    .tab("primitives")
                    .section("numbers")
                    .input(new NumberInput<>(1.0F, 2.0F, 0.01F)));

    public static final Setting<Integer> INT_SLIDER =
            create(SettingBuilder.of("int_slider", Codec.INT, 50)
                    .tab("primitives")
                    .section("sliders")
                    .input(new SliderInput<>(0, 100, 10)));

    public static final Setting<Float> FLOAT_SLIDER =
            create(SettingBuilder.of("float_slider", Codec.FLOAT, 0.5F)
                    .tab("primitives")
                    .section("sliders")
                    .input(new SliderInput<>(-1.0F, 1.0F, 0.1F)));

    public static final Setting<List<Boolean>> BOOLEAN_LIST_EXAMPLE =
            create(SettingBuilder.of("boolean_list_example", Codec.list(Codec.BOOL), List.of(true, false, false))
                    .tab("lists")
                    .input(new CheckBoxInput.CheckBoxList<>(5)));

    public static final Setting<List<Integer>> INT_LIST_EXAMPLE =
            create(SettingBuilder.of("integer_list_example", Codec.list(Codec.INT), List.of(1, 2, 3))
                    .tab("lists")
                    .section("numbers")
                    .input(new NumberInput.NumberList<>(0, 10, 1, 3)));

    public static final Setting<List<Double>> DOUBLE_LIST_EXAMPLE =
            create(SettingBuilder.of("double_list_example", Codec.list(Codec.DOUBLE), List.of(1.11, 2.22, 3.33))
                    .tab("lists")
                    .section("numbers")
                    .input(new NumberInput.NumberList<>(0.0, 5.0, 0.5, 5)));

    public static final Setting<List<Long>> LONG_LIST_EXAMPLE =
            create(SettingBuilder.of("long_list_example", Codec.list(Codec.LONG), List.of(500L, 1000L, 5000L))
                    .tab("lists")
                    .section("numbers")
                    .input(new SliderInput.SliderList<>(100L, 50000L, 100L, 5)));

    public static final Setting<List<Float>> FLOAT_LIST_EXAMPLE =
            create(SettingBuilder.of("float_list_example", Codec.list(Codec.FLOAT), List.of(0.2F, 0.4F, 0.6F))
                    .tab("lists")
                    .section("numbers")
                    .input(new SliderInput.SliderList<>(0.0F, 1.0F, 0.1F)));

    public static final Setting<String> STRING_EXAMPLE =
            create(SettingBuilder.of("string_example", Codec.STRING, "Example")
                    .tab("other")
                    .input(new TextFieldInput<>(32)));

    public static final Setting<EquipmentSlot> ENUM_EXAMPLE =
            create(SettingBuilder.of("enum_example", EquipmentSlot.CODEC, EquipmentSlot.HEAD)
                    .tab("other")
                    .input(new ComboBoxInput<EquipmentSlot>(EquipmentSlot.values())
                            .serializer(EquipmentSlot::getSerializedName)
                            .deserializer((r, s) -> EquipmentSlot.byName(s))));

    public static final Setting<EntityType<?>> REGISTRY_EXAMPLE =
            create(SettingBuilder.of("registry_example", EntityType.CODEC, EntityType.COD)
                    .tab("other")
                    .input(new ComboBoxInput<EntityType<?>>(Registries.ENTITY_TYPE.identifier())
                            .serializer(e -> e.builtInRegistryHolder().key().identifier().toString())
                            .deserializer((registry, str) -> {
                                Optional<Holder.Reference<EntityType<?>>> ref = registry.lookupOrThrow(Registries.ENTITY_TYPE).get(Identifier.parse(str));
                                return ref.map(Holder.Reference::value).orElse(null);
                            })));

    public static final Setting<List<String>> STRING_LIST_EXAMPLE =
            create(SettingBuilder.of("string_list_example", Codec.list(Codec.STRING), List.of("Example 1", "Example 2"))
                    .tab("other")
                    .section("lists")
                    .input(new TextFieldInput.TextFieldList<>(10, 3)));

    public static final Setting<List<Pose>> ENUM_LIST_EXAMPLE =
            create(SettingBuilder.of("enum_list_example", Codec.list(Pose.CODEC), List.of(Pose.EMERGING, Pose.DIGGING))
                    .tab("other")
                    .section("lists")
                    .input(new ComboBoxInput.ComboBoxList<Pose>(Pose.values(), 2)
                            .serializer(Pose::getSerializedName)
                            .deserializer((r, s) -> Pose.valueOf(s.toUpperCase()))));

    public static final Setting<List<EntityType<?>>> REGISTRY_LIST_EXAMPLE =
            create(SettingBuilder.of("registry_list_example", Codec.list(EntityType.CODEC), List.of(EntityType.COD))
                    .tab("other")
                    .section("lists")
                    .input(new ComboBoxInput.ComboBoxList<EntityType<?>>(Registries.ENTITY_TYPE.identifier())
                            .serializer(e -> e.builtInRegistryHolder().key().identifier().toString())
                            .deserializer((registry, str) -> {
                                Optional<Holder.Reference<EntityType<?>>> ref = registry.lookupOrThrow(Registries.ENTITY_TYPE).get(Identifier.parse(str));
                                return ref.map(Holder.Reference::value).orElse(null);
                            })));

    @Override
    public List<Setting<?>> get() {
        return Collections.unmodifiableList(SETTINGS);
    }

    private static <T> Setting<T> create(Setting.Builder<T> builder) {
        Setting<T> setting = builder.build(CamelliaExample.getInstance());
        SETTINGS.add(setting);
        return setting;
    }

    @Override
    public void save() {
        CamelliaExample.getInstance().getOrCreateConfig().save();
    }

    @Override
    public void reload() {
        CamelliaExample.getInstance().getOrCreateConfig().reload();
    }

}
