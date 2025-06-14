package net.pitan76.qrblock76.client;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.pitan76.mcpitanlib.api.client.registry.CompatRegistryClient;
import net.pitan76.mcpitanlib.api.client.render.DrawObjectMV;
import net.pitan76.mcpitanlib.api.client.render.block.entity.event.BlockEntityRenderEvent;
import net.pitan76.mcpitanlib.api.client.render.block.entity.v2.CompatBlockEntityRenderer;
import net.pitan76.qrblock76.QRBlockEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class QRBlockRenderer extends CompatBlockEntityRenderer<QRBlockEntity> {
    public QRBlockRenderer(CompatRegistryClient.BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    private static final Map<String, BitMatrix> qrCache = new HashMap<>();

    private int getOptimalQRSize(String data) {
        int dataLength = data.length();

        if (dataLength <= 10) return 21;      // 最小サイズ
        else if (dataLength <= 20) return 25;
        else if (dataLength <= 35) return 29;
        else if (dataLength <= 50) return 33;
        else return 37;
    }

    @Override
    public void render(BlockEntityRenderEvent<QRBlockEntity> e) {
        QRBlockEntity entity = e.getBlockEntity();
        if (entity == null) return;

        String data = entity.getData();
        if (data == null || data.isEmpty()) {
            renderQuad(e.getVertexConsumer(RenderLayer.getDebugQuads()),
                    e.matrices.peek().getPositionMatrix(),
                    e.matrices.peek().getNormalMatrix(),
                    0, 0, 0, 1, 1, 1,
                    0, 1, 0, 255, 255, 255);

            renderQuad(e.getVertexConsumer(RenderLayer.getDebugQuads()),
                    e.matrices.peek().getPositionMatrix(),
                    e.matrices.peek().getNormalMatrix(),
                    0, 1, 0, 1, 1, 1,
                    0, -1, 0, 255, 255, 255);


            renderQuad(e.getVertexConsumer(RenderLayer.getDebugQuads()),
                    e.matrices.peek().getPositionMatrix(),
                    e.matrices.peek().getNormalMatrix(),
                    0, 0, 1, 1, 1, 1,
                    0, 0, 1, 255, 255, 255);

            renderQuad(e.getVertexConsumer(RenderLayer.getDebugQuads()),
                    e.matrices.peek().getPositionMatrix(),
                    e.matrices.peek().getNormalMatrix(),
                    0, 0, 0, 1, 1, 1,
                    0, 0, -1, 255, 255, 255);

            renderQuad(e.getVertexConsumer(RenderLayer.getDebugQuads()),
                    e.matrices.peek().getPositionMatrix(),
                    e.matrices.peek().getNormalMatrix(),
                    1, 0, 0, 1, 1, 1,
                    1, 0, 0, 255, 255, 255);

            renderQuad(e.getVertexConsumer(RenderLayer.getDebugQuads()),
                    e.matrices.peek().getPositionMatrix(),
                    e.matrices.peek().getNormalMatrix(),
                    0, 0, 0, 1, 1, 1,
                    -1, 0, 0, 255, 255, 255);

            return;
        }

        BitMatrix matrix;
        int size = getOptimalQRSize(data);

        if (!qrCache.containsKey(data)) {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            try {
                matrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size);
                qrCache.put(data, matrix);
            } catch (WriterException ex) {
                return;
            }
        } else {
            matrix = qrCache.get(data);
        }

        float blockMin = 0;
        float blockMax = 1.0f;
        float scale = (blockMax - blockMin) / matrix.getWidth();

        Matrix4f matrix4f = e.matrices.peek().getPositionMatrix();
        Matrix3f matrix3f = e.matrices.peek().getNormalMatrix();

        e.push();
        try {
            RenderLayer layer = RenderLayer.getDebugQuads();
            DrawObjectMV drawObject = new DrawObjectMV(e.matrices, e.getVertexConsumer(layer));
            VertexConsumer vertexConsumer = drawObject.getBuffer();
            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale, size,
                    0, 1.0f, 0,
                    0, 1, 0);

            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale, size,
                    0, 0.0f, 0,
                    0, -1, 0);

            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale, size,
                    0, 0, 1.0f,
                    0, 0, 1);

            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale, size,
                    0, 0, 0.0f,
                    0, 0, -1);

            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale, size,
                    1.0f, 0, 0,
                    1, 0, 0);

            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale, size,
                    0.0f, 0, 0,
                    -1, 0, 0);

        } finally {
            e.pop();
        }
    }

    private void renderQRFace(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f,
                              BitMatrix qrMatrix, float blockMin, float scale, int size,
                              float offsetX, float offsetY, float offsetZ,
                              float normalX, float normalY, float normalZ) {

        if (Math.abs(normalY) > 0.5f) {
            for (int x = 0; x < qrMatrix.getWidth(); x++) {
                for (int z = 0; z < qrMatrix.getHeight(); z++) {
                    boolean isBlack = qrMatrix.get(x, z);

                    float minX = blockMin + x * scale + offsetX;
                    float maxX = blockMin + (x + 1) * scale + offsetX;
                    float minZ = blockMin + z * scale + offsetZ;
                    float maxZ = blockMin + (z + 1) * scale + offsetZ;

                    float y = offsetY;

                    int r, g, b;
                    r = g = b = isBlack ? 0 : 255;

                    renderQuad(vertexConsumer, matrix4f, matrix3f,
                            minX, y, minZ, maxX, y, maxZ,
                            normalX, normalY, normalZ, r, g, b);
                }
            }
        }

        if (Math.abs(normalZ) > 0.5f) {
            for (int x = 0; x < qrMatrix.getWidth(); x++) {
                for (int y = 0; y < qrMatrix.getHeight(); y++) {
                    boolean isBlack = qrMatrix.get(x, y);

                    float minX = blockMin + x * scale + offsetX;
                    float maxX = blockMin + (x + 1) * scale + offsetX;
                    float minY = blockMin + y * scale + offsetY;
                    float maxY = blockMin + (y + 1) * scale + offsetY;

                    float z = offsetZ;

                    int r, g, b;
                    r = g = b = isBlack ? 0 : 255;

                    renderQuad(vertexConsumer, matrix4f, matrix3f,
                            minX, minY, z, maxX, maxY, z,
                            normalX, normalY, normalZ, r, g, b);
                }
            }
        }

        if (Math.abs(normalX) > 0.5f) {
            for (int y = 0; y < qrMatrix.getHeight(); y++) {
                for (int z = 0; z < qrMatrix.getWidth(); z++) {
                    boolean isBlack = qrMatrix.get(z, y);

                    float minY = blockMin + y * scale + offsetY;
                    float maxY = blockMin + (y + 1) * scale + offsetY;
                    float minZ = blockMin + z * scale + offsetZ;
                    float maxZ = blockMin + (z + 1) * scale + offsetZ;

                    float x = offsetX;

                    int r, g, b;
                    r = g = b = isBlack ? 0 : 255;

                    renderQuad(vertexConsumer, matrix4f, matrix3f,
                            x, minY, minZ, x, maxY, maxZ,
                            normalX, normalY, normalZ, r, g, b);
                }
            }
        }
    }

    private void renderQuad(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f,
                            float x1, float y1, float z1, float x2, float y2, float z2,
                            float normalX, float normalY, float normalZ, int r, int g, int b) {

        if (Math.abs(normalY) > 0.5f) {
            float y = y1;

            if (normalY > 0) {
                vertexConsumer.vertex(matrix4f, x1, y, z1).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y, z2).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y, z2).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y, z1).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
            } else {
                vertexConsumer.vertex(matrix4f, x1, y, z1).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y, z1).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y, z2).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y, z2).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
            }

        } else if (Math.abs(normalZ) > 0.5f) {

            float z = z1;

            if (normalZ > 0) {
                vertexConsumer.vertex(matrix4f, x1, y1, z).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y2, z).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y2, z).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y1, z).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
            } else {
                vertexConsumer.vertex(matrix4f, x1, y1, z).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y1, z).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x2, y2, z).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x1, y2, z).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
            }

        } else if (Math.abs(normalX) > 0.5f) {
            float x = x1;

            if (normalX > 0) {
                vertexConsumer.vertex(matrix4f, x, y1, z1).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x, y2, z1).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x, y2, z2).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x, y1, z2).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
            } else {
                vertexConsumer.vertex(matrix4f, x, y1, z1).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x, y1, z2).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x, y2, z2).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
                vertexConsumer.vertex(matrix4f, x, y2, z1).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).overlay(OverlayTexture.DEFAULT_UV).next();
            }
        }
    }
}