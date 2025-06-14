package net.pitan76.qrblock76.client;

import com.google.zxing.common.BitMatrix;
import net.minecraft.client.render.OverlayTexture;
import net.pitan76.mcpitanlib.api.client.registry.CompatRegistryClient;
import net.pitan76.mcpitanlib.api.client.render.CompatRenderLayer;
import net.pitan76.mcpitanlib.api.client.render.DrawObjectMV;
import net.pitan76.mcpitanlib.api.client.render.block.entity.event.BlockEntityRenderEvent;
import net.pitan76.mcpitanlib.api.client.render.block.entity.v2.CompatBlockEntityRenderer;
import net.pitan76.mcpitanlib.api.network.v2.ClientNetworking;
import net.pitan76.mcpitanlib.api.util.client.render.VertexConsumerUtil;
import net.pitan76.mcpitanlib.api.util.client.render.WorldRendererUtil;
import net.pitan76.mcpitanlib.midohra.network.CompatPacketByteBuf;
import net.pitan76.qrblock76.QRBlockEntity;
import net.pitan76.qrblock76.QRBlockMod;
import net.pitan76.qrblock76.QRData;

public class QRBlockRenderer extends CompatBlockEntityRenderer<QRBlockEntity> {
    public QRBlockRenderer(CompatRegistryClient.BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(BlockEntityRenderEvent<QRBlockEntity> e) {
        QRBlockEntity entity = e.getBlockEntity();
        if (entity == null) return;

        String data = entity.getData();

        if (!entity.isReceivedOnClient) {
            CompatPacketByteBuf buf = CompatPacketByteBuf.create();
            buf.writeBlockPos(entity.callGetPos());
            ClientNetworking.send(QRBlockMod._id("request_qrdata"), buf);
        }

        if (data == null || data.isEmpty()) return;

        QRData qrdata = QRData.getOrCreateQRData(data);
        if (qrdata == null) return;

        BitMatrix matrix = qrdata.matrix;

        float blockMin = 0;
        float blockMax = 1.0f;
        float scale = (blockMax - blockMin) / matrix.getWidth();

        int light = entity.callGetWorld() != null ? WorldRendererUtil.getLightmapCoordinates(entity.callGetWorld(), entity.callGetPos()) : e.getLight();

        e.push();

        try {
            DrawObjectMV drawObject = e.getDrawObject(CompatRenderLayer.SOLID);

            float offset = 0.001f;

            renderQRFace(drawObject, matrix, blockMin, scale,
                    0, 1.0f + offset, 0, 0, 1, 0, light);
            renderQRFace(drawObject, matrix, blockMin, scale,
                    0, 0.0f - offset, 0, 0, -1, 0, light);
            renderQRFace(drawObject, matrix, blockMin, scale,
                    0, 0, 1.0f + offset, 0, 0, 1, light);
            renderQRFace(drawObject, matrix, blockMin, scale,
                    0, 0, 0.0f - offset, 0, 0, -1, light);
            renderQRFace(drawObject, matrix, blockMin, scale,
                    1.0f + offset, 0, 0, 1, 0, 0, light);
            renderQRFace(drawObject, matrix, blockMin, scale,
                    0.0f - offset, 0, 0, -1, 0, 0, light);
        } finally {
            e.pop();
        }
    }

    private void renderQRFace(DrawObjectMV drawObject, BitMatrix qrMatrix, float blockMin, float scale,
                              float offsetX, float offsetY, float offsetZ, float normalX, float normalY, float normalZ, int light) {

        float absNormalX = Math.abs(normalX);
        float absNormalY = Math.abs(normalY);
        float absNormalZ = Math.abs(normalZ);

        if (absNormalX <= 0.5f && absNormalY <= 0.5f && absNormalZ <= 0.5f) return;

        int width = qrMatrix.getWidth();
        int height = qrMatrix.getHeight();

        for (int i = 0; i < width; i++) {
            float coord1Min = blockMin + i * scale;
            float coord1Max = coord1Min + scale;

            for (int j = 0; j < height; j++) {
                boolean isBlack;

                int matrixX, matrixY;

                if (absNormalY > 0.5f) { // XZ plane
                    if (normalY > 0) {
                        matrixX = i;
                        matrixY = height - 1 - j;
                    } else {
                        matrixX = width - 1 - i;
                        matrixY = j;
                    }
                    isBlack = qrMatrix.get(matrixX, matrixY);
                } else if (absNormalZ > 0.5f) { // XY plane
                    if (normalZ > 0) {
                        matrixX = i;
                        matrixY = height - 1 - j;
                    } else {
                        matrixX = width - 1 - i;
                        matrixY = height - 1 - j;
                    }
                    isBlack = qrMatrix.get(matrixX, matrixY);
                } else if (absNormalX > 0.5f) { // YZ plane
                    if (normalX > 0) {
                        matrixX = height - 1 - j;
                        matrixY = width - 1 - i;
                    } else {
                        matrixX = j;
                        matrixY = width - 1 - i;
                    }
                    isBlack = qrMatrix.get(matrixX, matrixY);
                } else {
                    continue;
                }

                if (!isBlack) continue;

                float coord2Min = blockMin + j * scale;
                float coord2Max = coord2Min + scale;

                float x1, y1, z1, x2, y2, z2;
                if (absNormalY > 0.5f) {
                    x1 = coord1Min + offsetX;
                    y1 = offsetY;
                    z1 = coord2Min + offsetZ;
                    x2 = coord1Max + offsetX;
                    y2 = offsetY;
                    z2 = coord2Max + offsetZ;
                } else if (absNormalZ > 0.5f) {
                    x1 = coord1Min + offsetX;
                    y1 = coord2Min + offsetY;
                    z1 = offsetZ;
                    x2 = coord1Max + offsetX;
                    y2 = coord2Max + offsetY;
                    z2 = offsetZ;
                } else {
                    x1 = offsetX;
                    y1 = coord1Min + offsetY;
                    z1 = coord2Min + offsetZ;
                    x2 = offsetX;
                    y2 = coord1Max + offsetY;
                    z2 = coord2Max + offsetZ;
                }

                VertexConsumerUtil.renderQuad(drawObject, x1, y1, z1, x2, y2, z2,
                        normalX, normalY, normalZ, 0, 0, 0, 255, 0, 0, OverlayTexture.DEFAULT_UV, light);
            }
        }
    }
}
