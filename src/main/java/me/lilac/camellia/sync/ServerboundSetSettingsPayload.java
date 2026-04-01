package me.lilac.camellia.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.config.SettingHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundSetSettingsPayload(UUID player, List<Setting<Object>> settings) implements CustomPacketPayload {

    public static final Type<ServerboundSetSettingsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("camellia", "set_settings"));

    public static StreamCodec<RegistryFriendlyByteBuf, ServerboundSetSettingsPayload> getStreamCodec(SettingHolder settingHolder) {
        return StreamCodec.of(ServerboundSetSettingsPayload::toNetwork, (buf) -> ServerboundSetSettingsPayload.fromNetwork(buf, settingHolder));
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, ServerboundSetSettingsPayload payload) {
        buf.writeUUID(payload.player);
        buf.writeInt(payload.settings.size());
        for (Setting<Object> setting : payload.settings) {
            buf.writeUtf(setting.getKey());
            buf.writeJsonWithCodec(setting.getCodec(), setting.get());
        }
    }

    private static ServerboundSetSettingsPayload fromNetwork(RegistryFriendlyByteBuf buf, SettingHolder serverSettings) {
        UUID playerUuid = buf.readUUID();

        List<Setting<Object>> settings = new ArrayList<>();
        for (Setting<?> setting : serverSettings.get())
            settings.add((Setting<Object>) setting);

        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            String key = buf.readUtf();
            for (Setting<Object> setting : settings) {
                if (setting.getKey().equalsIgnoreCase(key)) {
                    Object x = buf.readLenientJsonWithCodec(setting.getCodec());
                    setting.set(x);
                }
            }
        }

        return new ServerboundSetSettingsPayload(playerUuid, settings);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
