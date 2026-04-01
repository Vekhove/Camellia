package me.lilac.camellia;

import java.io.File;
import java.io.IOException;
import me.lilac.camellia.config.Config;
import me.lilac.camellia.config.ConfigHolder;
import me.lilac.camellia.sync.ConfigSync;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamelliaExample implements ModInitializer, ConfigHolder {

    public static final String MOD_ID = "camellia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static CamelliaExample instance;
    private Config config;

    @Override
    public void onInitialize() {
        instance = this;

        this.config = this.getOrCreateConfig();
        ConfigSync.init(SettingsExample.INSTANCE);
    }

    /**
     * @return An instance of the server-side config.
     *      <br>Use {@linkplain SettingsExample} to grab a specific setting.
     */
    @Override
    public Config getOrCreateConfig() {
        if (this.config != null)
            return this.config;

        File folder = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).toFile();
        folder.mkdirs();

        File file = new File(folder, "server.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }

        return Config.builder(file).settings(SettingsExample.INSTANCE).build();
    }

    public static CamelliaExample getInstance() {
        return instance;
    }

}
