package me.lilac.camellia.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public interface Config {

    List<Setting<?>> getSettings();

    <T> T get(Setting<T> setting);

    <T> void set(Setting<T> setting, T value);

    File getFile();

    JsonObject getBaseConfig();

    void reload();

    default void save() {
        for (Setting<?> setting : this.getSettings())
            setting.write(this.getBaseConfig());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(this.getFile())) {
            gson.toJson(this.getBaseConfig(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Builder builder(File file) {
        return new BasicConfig.Builder(file);
    }

    interface Builder {

        Builder settings(List<Setting<?>> settings);

        Builder settings(SettingHolder settingHolder);

        Config build();

    }

}
