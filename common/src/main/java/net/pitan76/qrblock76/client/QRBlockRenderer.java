package net.pitan76.qrblock76.client;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import net.minecraft.client.render.VertexConsumer;
import net.pitan76.mcpitanlib.api.client.registry.CompatRegistryClient;
import net.pitan76.mcpitanlib.api.client.render.CompatRenderLayer;
import net.pitan76.mcpitanlib.api.client.render.DrawObjectMV;
import net.pitan76.mcpitanlib.api.client.render.block.entity.event.BlockEntityRenderEvent;
import net.pitan76.mcpitanlib.api.client.render.block.entity.event.CompatBlockEntityRendererConstructArgs;
import net.pitan76.mcpitanlib.api.client.render.block.entity.v2.CompatBlockEntityRenderer;
import net.pitan76.mcpitanlib.api.util.client.ClientUtil;
import net.pitan76.qrblock76.QRBlockEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class QRBlockRenderer extends CompatBlockEntityRenderer<QRBlockEntity> {
    public QRBlockRenderer(CompatRegistryClient.BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    public QRBlockRenderer(CompatBlockEntityRendererConstructArgs args) {
        super(args);
    }

    @Override
    public void render(BlockEntityRenderEvent<QRBlockEntity> e) {
        QRBlockEntity entity = e.getBlockEntity();
        if (entity == null) return;

        String data = entity.getData();
        if (data == null || data.isEmpty()) return;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        int size = 21;
        BitMatrix matrix;
        try {
            matrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size);
        } catch (WriterException ex) {
            return;
        }

        float margin = 0.05f;
        float blockMin = margin;
        float blockMax = 1.0f - margin;
        float scale = (blockMax - blockMin) / size;

        Matrix4f matrix4f = e.matrices.peek().getPositionMatrix();
        Matrix3f matrix3f = e.matrices.peek().getNormalMatrix();

        e.push();
        try {
            DrawObjectMV drawObject = e.getDrawObject(CompatRenderLayer.TRANSLUCENT);
            VertexConsumer vertexConsumer = drawObject.getBuffer();

//            // 試しに描画、点を
//            renderQuad(vertexConsumer, matrix4f, matrix3f,
//                    0, 2, 0, // 座標
//                    1, 2, 1, // 座標
//                    0, 1, 0, 255, 0, 255); // 赤い立方体

            // 上面に描画（上から見える）
            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale, size,
                    0, 1.01f, 0, // Y=上面
                    0, 1, 0);    // 上向き法線

            // 下面に描画（下から見える）
            renderQRFace(vertexConsumer, matrix4f, matrix3f, matrix, blockMin, scale, size,
                    0, -0.01f, 0, // Y=下面
                    0, -1, 0);    // 下向き法線

        } finally {
            e.pop();
        }
    }

    // 水平面（上面・下面）用
    private void renderQRFace(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f,
                              BitMatrix qrMatrix, float blockMin, float scale, int size,
                              float offsetX, float offsetY, float offsetZ,
                              float normalX, float normalY, float normalZ) {

        for (int x = 0; x < qrMatrix.getWidth(); x++) {
            for (int z = 0; z < qrMatrix.getHeight(); z++) {
                boolean isBlack = qrMatrix.get(x, z);

                float minX = blockMin + x * scale + offsetX;
                float maxX = blockMin + (x + 1) * scale + offsetX;
                float minZ = blockMin + z * scale + offsetZ;
                float maxZ = blockMin + (z + 1) * scale + offsetZ;

                float y = offsetY;

                int r, g, b;
                r = g = b = isBlack ? 0 : 255; // 黒なら0、白なら255

                renderQuad(vertexConsumer, matrix4f, matrix3f,
                        minX, y, minZ, maxX, y, maxZ,
                        normalX, normalY, normalZ, r, g, b);
            }
        }
    }

    private void renderQuad(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f,
                            float x1, float y1, float z1, float x2, float y2, float z2,
                            float normalX, float normalY, float normalZ, int r, int g, int b) {

        // 法線ベクトルから面の種類を判定
        if (Math.abs(normalY) > 0.5f) {
            // ★ 水平面（上面・下面）
            // Y座標を統一（y1を使用）
            float y = y1;

            if (normalY > 0) {
                vertexConsumer.vertex(matrix4f, x1, y, z1).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x1, y, z2).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x2, y, z2).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x2, y, z1).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
            } else {
                vertexConsumer.vertex(matrix4f, x1, y, z1).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x2, y, z1).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x2, y, z2).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x1, y, z2).color(r, g, b, 255).texture(0, 0)
                        .light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
            }

        } else if (Math.abs(normalZ) > 0.5f) {
            // ★ 垂直面（北面・南面）
            // Z座標を統一（z1を使用）
            float z = z1;

            if (normalZ > 0) { // 南面
                vertexConsumer.vertex(matrix4f, x1, y1, z).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x1, y2, z).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x2, y1, z).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();

                vertexConsumer.vertex(matrix4f, x2, y1, z).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x1, y2, z).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x2, y2, z).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();

            } else { // 北面
                vertexConsumer.vertex(matrix4f, x1, y1, z).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x2, y1, z).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x1, y2, z).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();

                vertexConsumer.vertex(matrix4f, x2, y1, z).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x2, y2, z).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x1, y2, z).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
            }

        } else if (Math.abs(normalX) > 0.5f) {
            // ★ 垂直面（東面・西面）
            // X座標を統一（x1を使用）
            float x = x1;

            if (normalX > 0) { // 東面
                vertexConsumer.vertex(matrix4f, x, y1, z1).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x, y2, z1).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x, y1, z2).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();

                vertexConsumer.vertex(matrix4f, x, y1, z2).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x, y2, z1).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x, y2, z2).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();

            } else { // 西面
                vertexConsumer.vertex(matrix4f, x, y1, z1).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x, y1, z2).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x, y2, z1).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();

                vertexConsumer.vertex(matrix4f, x, y1, z2).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x, y2, z2).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
                vertexConsumer.vertex(matrix4f, x, y2, z1).color(r, g, b, 255).texture(0, 0).light(15728880).normal(matrix3f, normalX, normalY, normalZ).next();
            }
        }
    }
}