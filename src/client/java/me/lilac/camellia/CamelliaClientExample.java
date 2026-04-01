package me.lilac.camellia;

import java.io.File;
import java.io.IOException;
import me.lilac.camellia.config.Config;
import me.lilac.camellia.config.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class CamelliaClientExample implements ClientModInitializer, ConfigHolder {

    private static CamelliaClientExample instance;
    private Config config;

    @Override
    public void onInitializeClient() {
        instance = this;

        this.config = this.getOrCreateConfig();
        ClientConfigSync.init(SettingsExample.INSTANCE);
    }

    @Override
    public Config getOrCreateConfig() {
        if (this.config != null)
            return this.config;

        File folder = FabricLoader.getInstance().getConfigDir().resolve(CamelliaExample.MOD_ID).toFile();
        folder.mkdirs();

        File file = new File(folder, "client.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                CamelliaExample.LOGGER.error(e.getMessage());
            }
        }

        return Config.builder(file).settings(ClientSettingsExample.INSTANCE).build();
    }

    public static CamelliaClientExample getInstance() {
        return instance;
    }

}
