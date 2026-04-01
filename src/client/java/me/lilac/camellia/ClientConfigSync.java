package me.lilac.camellia;

import me.lilac.camellia.config.SettingHolder;
import me.lilac.camellia.sync.ClientboundInitSettingsPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class ClientConfigSync  {

    public static boolean serverSettingsReceived = false;

    public static void init(SettingHolder serverSettings) {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundInitSettingsPayload.TYPE, ((payload, context) -> {
            serverSettingsReceived = true;
        }));

        ClientPlayConnectionEvents.DISCONNECT.register((listener, minecraft) -> {
            serverSettingsReceived = false;
            serverSettings.reload();
        });
    }

}
