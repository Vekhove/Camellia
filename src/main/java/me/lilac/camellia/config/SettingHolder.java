package me.lilac.camellia.config;

import java.util.List;

public interface SettingHolder {

    List<Setting<?>> get();

    void save();

    void reload();

}
