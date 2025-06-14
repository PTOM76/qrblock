package net.pitan76.qrblock76.client;

import com.google.zxing.common.BitMatrix;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.network.PacketByteBuf;
import net.pitan76.mcpitanlib.api.client.registry.CompatRegistryClient;
import net.pitan76.mcpitanlib.api.client.render.CompatRenderLayer;
import net.pitan76.mcpitanlib.api.client.render.DrawObjectMV;
import net.pitan76.mcpitanlib.api.client.render.block.entity.event.BlockEntityRenderEvent;
import net.pitan76.mcpitanlib.api.client.render.block.entity.v2.CompatBlockEntityRenderer;
import net.pitan76.mcpitanlib.api.network.PacketByteUtil;
import net.pitan76.mcpitanlib.api.network.v2.ClientNetworking;
import net.pitan76.qrblock76.QRBlockEntity;
import net.pitan76.qrblock76.QRBlockMod;
import net.pitan76.qrblock76.QRData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class QRBlockRenderer extends CompatBlockEntityRenderer<QRBlockEntity> {
    public QRBlockRenderer(CompatRegistryClient.BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(BlockEntityRenderEvent<QRBlockEntity> e) {
        QRBlockEntity entity = e.getBlockEntity();
        if (entity == null) return;

        String data = entity.getData();
        if (data == null || data.isEmpty()) {
            if (!entity.isReceivedOnClient) {
                PacketByteBuf buf = PacketByteUtil.create();
                buf.writeBlockPos(entity.callGetPos());
                ClientNetworking.send(QRBlockMod._id("request_qrdata"), buf);
            }

            return;
        }

        QRData qrdata = QRData.getOrCreateQRData(data);
        if (qrdata == null) return;

        BitMatrix matrix = qrdata.matrix;

        float blockMin = 0;
        float blockMax = 1.0f;
        float scale = (blockMax - blockMin) / matrix.getWidth();

        Matrix4f matrix4f = e.matrices.peek().getPositionMatrix();
        Matrix3f matrix3f = e.matrices.peek().getNormalMatrix();

        int light = entity.callGetWorld() != null ? WorldRenderer.getLightmapCoordinates(entity.callGetWorld(), entity.callGetPos()) : e.getLight();

        e.push();

        try {
            DrawObjectMV drawObject = e.getDrawObject(CompatRenderLayer.SOLID);
            VertexConsumer vertexConsumer = drawObject.getBuffer();

            float offset = 0.001f;

            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale,
                    0, 1.0f + offset, 0, 0, 1, 0, light);
            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale,
                    0, 0.0f - offset, 0, 0, -1, 0, light);
            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale,
                    0, 0, 1.0f + offset, 0, 0, 1, light);
            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale,
                    0, 0, 0.0f - offset, 0, 0, -1, light);
            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale,
                    1.0f + offset, 0, 0, 1, 0, 0, light);
            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale,
                    0.0f - offset, 0, 0, -1, 0, 0, light);
        } finally {
            e.pop();
        }
    }

    private void renderQRFace(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f,
                              BitMatrix qrMatrix, float blockMin, float scale,
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
                boolean isBlack = (absNormalX > 0.5f) ? qrMatrix.get(j, i) : qrMatrix.get(i, j);
                if (!isBlack) continue;

                float coord2Min = blockMin + j * scale;
                float coord2Max = coord2Min + scale;

                float x1, y1, z1, x2, y2, z2;
                if (absNormalY > 0.5f) {
                    x1 = coord1Min + offsetX; y1 = offsetY; z1 = coord2Min + offsetZ;
                    x2 = coord1Max + offsetX; y2 = offsetY; z2 = coord2Max + offsetZ;
                } else if (absNormalZ > 0.5f) {
                    x1 = coord1Min + offsetX; y1 = coord2Min + offsetY; z1 = offsetZ;
                    x2 = coord1Max + offsetX; y2 = coord2Max + offsetY; z2 = offsetZ;
                } else {
                    x1 = offsetX; y1 = coord1Min + offsetY; z1 = coord2Min + offsetZ;
                    x2 = offsetX; y2 = coord1Max + offsetY; z2 = coord2Max + offsetZ;
                }

                renderQuad(vertexConsumer, matrix4f, matrix3f,
                        x1, y1, z1, x2, y2, z2,
                        normalX, normalY, normalZ, 0, 0, 0, light);
            }
        }
    }

    private void renderQuad(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f,
                            float x1, float y1, float z1, float x2, float y2, float z2,
                            float normalX, float normalY, float normalZ, int r, int g, int b, int light) {

        if (Math.abs(normalY) > 0.5f) {
            if (normalY > 0) {
                vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y1, z2).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y1, z2).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y1, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
            } else {
                vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y1, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y1, z2).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y1, z2).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
            }

        } else if (Math.abs(normalZ) > 0.5f) {
            if (normalZ > 0) {
                vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y2, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y2, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y1, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
            } else {
                vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y1, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y2, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y2, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
            }

        } else if (Math.abs(normalX) > 0.5f) {
            if (normalX > 0) {
                vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y2, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y2, z2).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y1, z2).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
            } else {
                vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y1, z2).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y2, z2).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y2, z1).color(r, g, b, 255).texture(0, 0)
                        .light(light).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
            }
        }
    }
}