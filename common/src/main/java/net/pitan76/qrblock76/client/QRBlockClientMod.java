package net.pitan76.qrblock76.client;

import net.minecraft.block.entity.BlockEntity;
import net.pitan76.mcpitanlib.api.client.registry.CompatRegistryClient;
import net.pitan76.mcpitanlib.api.network.v2.ClientNetworking;
import net.pitan76.mcpitanlib.api.util.client.ClientUtil;
import net.pitan76.mcpitanlib.api.util.client.widget.TextFieldUtil;
import net.pitan76.mcpitanlib.midohra.util.math.BlockPos;
import net.pitan76.mcpitanlib.midohra.world.World;
import net.pitan76.qrblock76.QRBlockEntity;
import net.pitan76.qrblock76.QRBlockMod;

public class QRBlockClientMod {
    private static String textCache = null;

    public static void init() {
        CompatRegistryClient.registerCompatBlockEntityRenderer(QRBlockMod.QR_BLOCK_ENTITY_TYPE.getOrNull(), QRBlockRenderer::new);

        ClientNetworking.registerReceiver(QRBlockMod._id("qrs2c_screen"), (e) -> {
            String text = e.getBuf().readString();

            if (ClientUtil.getScreen() instanceof QRBlockScreen) {
                QRBlockScreen screen = (QRBlockScreen) ClientUtil.getScreen();
                TextFieldUtil.setText(screen.textField, text);
            } else {
                textCache = text;
            }
        });

        ClientNetworking.registerReceiver(QRBlockMod._id("qrs2c"), (e) -> {
            BlockPos pos = BlockPos.of(e.getBuf().readBlockPos());
            String text = e.getBuf().readString();

            syncQRBEText(pos, text);
        });
    }

    public static void syncQRBEText(BlockPos pos, String text) {
        World world = World.of(ClientUtil.getWorld());
        BlockEntity blockEntity = world.getBlockEntity(pos).get();

        if (blockEntity instanceof QRBlockEntity) {
            QRBlockEntity qrBlockEntity = (QRBlockEntity) blockEntity;

            qrBlockEntity.setData(text);
            qrBlockEntity.isReceivedOnClient = true;
        }
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
