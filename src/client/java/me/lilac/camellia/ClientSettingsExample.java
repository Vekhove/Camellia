package me.lilac.camellia;

import com.mojang.serialization.Codec;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.config.SettingBuilder;
import me.lilac.camellia.config.SettingHolder;
import me.lilac.camellia.config.Util;
import me.lilac.camellia.input.ColorInput;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ClientSettingsExample implements SettingHolder {

    public static final SettingHolder INSTANCE = new ClientSettingsExample();
    private static final List<Setting<?>> SETTINGS = new ArrayList<>();

    public static final Setting<Color> COLOR_EXAMPLE =
            create(SettingBuilder.of("color_example", Util.HEX_COLOR_CODEC, Color.CYAN)
                    .tab("client")
                    .input(new ColorInput<>()));

    public static final Setting<Color> ALPHA_COLOR_EXAMPLE =
            create(SettingBuilder.of("alpha_color_example", Util.HEX_COLOR_CODEC, new Color(135, 50, 170, 128))
                    .tab("client")
                    .input(new ColorInput<>(true)));

    public static final Setting<List<Color>> COLOR_LIST_EXAMPLE =
            create(SettingBuilder.of("color_list_example", Codec.list(Util.HEX_COLOR_CODEC), List.of(Color.RED, Color.GREEN, Color.BLUE))
                    .tab("client")
                    .input(new ColorInput.ColorList<>()));

    @Override
    public List<Setting<?>> get() {
        return Collections.unmodifiableList(SETTINGS);
    }

    private static <T> Setting<T> create(Setting.Builder<T> builder) {
        Setting<T> setting = builder.build(CamelliaClientExample.getInstance());
        SETTINGS.add(setting);
        return setting;
    }

    @Override
    public void save() {
        CamelliaClientExample.getInstance().getOrCreateConfig().save();
    }

    @Override
    public void reload() {
        CamelliaClientExample.getInstance().getOrCreateConfig().reload();
    }

}
