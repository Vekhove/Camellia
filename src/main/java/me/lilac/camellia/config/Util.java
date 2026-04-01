package me.lilac.camellia.config;

import com.mojang.serialization.Codec;
import java.awt.Color;

public class Util {

    public static final Codec<Color> HEX_COLOR_CODEC =
            Codec.STRING.xmap(
                    s -> {
                        String hex = s.replace("#", "");
                        long value = Long.parseLong(hex, 16);

                        if (hex.length() == 6) {
                            return new Color((int) value);
                        } else if (hex.length() == 8) {
                            return new Color((int) value, true);
                        } else {
                            throw new IllegalArgumentException("Invalid color: " + s);
                        }
                    },
                    c -> String.format("#%08X", c.getRGB())
            );

}
