package net.pitan76.qrblock76.client;

import net.pitan76.mcpitanlib.api.client.registry.CompatRegistryClient;
import net.pitan76.mcpitanlib.api.network.v2.ClientNetworking;
import net.pitan76.mcpitanlib.api.util.client.ClientUtil;
import net.pitan76.mcpitanlib.api.util.client.widget.TextFieldUtil;
import net.pitan76.qrblock76.QRBlockMod;

public class QRBlockClientMod {
    private static String textCache = null;

    public static void init() {
        CompatRegistryClient.registerCompatBlockEntityRenderer(QRBlockMod.QR_BLOCK_ENTITY_TYPE.getOrNull(), QRBlockRenderer::new);

        ClientNetworking.registerReceiver(QRBlockMod._id("get_qr"), (e) -> {
            String text = e.getBuf().readString();
            if (ClientUtil.getScreen() instanceof QRBlockScreen) {
                QRBlockScreen screen = (QRBlockScreen) ClientUtil.getScreen();
                TextFieldUtil.setText(screen.textField, text);
            } else {
                textCache = text;
            }
        });
    }

    public static String useTextCache() {
        if (textCache != null) {
            String text = textCache;
            textCache = null;
            return text;
        } else {
            throw new RuntimeException("Text cache is null, this should not happen!");
        }
    }

    public static boolean hasTextCache() {
        return textCache != null;
    }
}
