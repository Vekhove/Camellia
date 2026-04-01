package me.lilac.camellia;

import me.lilac.camellia.config.SettingHolder;
import me.lilac.camellia.screen.ConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public class CamelliaConfigScreenExample extends ConfigScreen {

    public CamelliaConfigScreenExample(Screen parent) {
        super(parent, CamelliaExample.MOD_ID);
    }

    @Override
    public SettingHolder getClientSettings() {
        return ClientSettingsExample.INSTANCE;
    }

    @Override
    public SettingHolder getServerSettings() {
        return SettingsExample.INSTANCE;
    }

}
