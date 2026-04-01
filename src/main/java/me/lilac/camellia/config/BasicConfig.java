package me.lilac.camellia.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BasicConfig implements Config {

    private final File file;
    private final List<Setting<?>> settings;
    private final Map<Setting<?>, Object> settingsValueCache;
    private JsonObject config;

    private BasicConfig(File file, List<Setting<?>> settings) {
        this.file = file;
        this.settings = settings;
        this.settingsValueCache = new HashMap<>((int) Math.round(this.settings.size() / 0.75 + 1));
        this.reload();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Setting<T> setting) {
        if (this.settingsValueCache.containsKey(setting))
            return (T) this.settingsValueCache.get(setting);

        T value = setting.read(this.getBaseConfig());
        this.settingsValueCache.put(setting, value);
        return value;
    }


    @Override
    public <T> void set(Setting<T> setting, T value) {
        setting.write(this.getBaseConfig());
        this.settingsValueCache.put(setting, value);
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public JsonObject getBaseConfig() {
        if (this.config == null) {
            try {
                JsonElement element = JsonParser.parseReader(new FileReader(this.file));
                this.config = element.isJsonNull() ? new JsonObject() : element.getAsJsonObject();
            } catch (Exception e) {
                this.config = new JsonObject();
                e.printStackTrace();
            }
        }

        return this.config;
    }

    @Override
    public void reload() {
        this.config = null;
        this.settingsValueCache.clear();

        if (this.settings.isEmpty())
            return;

        for (Setting<?> setting : this.settings) {
            if (setting instanceof BasicSetting<?> basicSetting)
                basicSetting.reload();
        }

        boolean changed = !this.file.exists();

        JsonObject config = this.getBaseConfig();

        for (Setting<?> setting : this.settings) {
            if (config.has(setting.getKey()))
                continue;

            setting.write(config);
            changed = true;
        }

        if (changed) {
            try {
                this.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(this.settings);
    }

    public static class Builder implements Config.Builder {

        private final File file;
        private List<Setting<?>> settings;

        public Builder(File file) {
            this.file = file;
            this.settings = Collections.emptyList();
        }

        @Override
        public Config.Builder settings(List<Setting<?>> settings) {
            this.settings = new ArrayList<>(settings);
            return this;
        }

        @Override
        public Config.Builder settings(SettingHolder settingHolder) {
            this.settings = new ArrayList<>(settingHolder.get());
            return this;
        }

        @Override
        public Config build() {
            return new BasicConfig(this.file, this.settings);
        }

    }

}
