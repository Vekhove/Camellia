package me.lilac.camellia.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.config.SettingHolder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ConfigSync {

    public static void init(SettingHolder serverSettings) {
        PayloadTypeRegistry.clientboundPlay().register(ClientboundInitSettingsPayload.TYPE, ClientboundInitSettingsPayload.getStreamCodec(serverSettings));
        PayloadTypeRegistry.serverboundPlay().register(ServerboundSetSettingsPayload.TYPE, ServerboundSetSettingsPayload.getStreamCodec(serverSettings));
        ServerPlayNetworking.registerGlobalReceiver(ServerboundSetSettingsPayload.TYPE, (payload, context) -> {
            UUID playerUuid = payload.player();
            Level level = context.player().level();
            Player player = level.getPlayerByUUID(playerUuid);
            if (player == null || !context.server().getPlayerList().isOp(player.nameAndId()))
                return;

            serverSettings.save();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (server.getPlayerList().isOp(handler.player.nameAndId())) {
                List<Setting<Object>> settings = new ArrayList<>();
                for (Setting<?> setting : serverSettings.get())
                    settings.add((Setting<Object>) setting);

                ServerPlayNetworking.send(handler.player, new ClientboundInitSettingsPayload(settings));
            }
        });
    }

}
