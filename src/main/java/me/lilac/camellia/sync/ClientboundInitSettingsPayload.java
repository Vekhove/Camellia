package me.lilac.camellia.sync;

import java.util.ArrayList;
import java.util.List;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.config.SettingHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundInitSettingsPayload(List<Setting<Object>> settings) implements CustomPacketPayload {

    public static final Type<ClientboundInitSettingsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("camellia", "init_settings"));

    public static StreamCodec<RegistryFriendlyByteBuf, ClientboundInitSettingsPayload> getStreamCodec(SettingHolder settingHolder) {
        return StreamCodec.of(ClientboundInitSettingsPayload::toNetwork, (buf) -> ClientboundInitSettingsPayload.fromNetwork(buf, settingHolder));
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, ClientboundInitSettingsPayload payload) {
        buf.writeInt(payload.settings.size());
        for (Setting<Object> setting : payload.settings) {
            buf.writeUtf(setting.getKey());
            buf.writeJsonWithCodec(setting.getCodec(), setting.get());
        }
    }

    private static ClientboundInitSettingsPayload fromNetwork(RegistryFriendlyByteBuf buf, SettingHolder serverSettings) {
        System.out.println("From Network?");

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

        return new ClientboundInitSettingsPayload(settings);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
