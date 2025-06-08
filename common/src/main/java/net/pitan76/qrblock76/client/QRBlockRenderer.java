package net.pitan76.qrblock76.client;

import net.pitan76.mcpitanlib.api.client.registry.CompatRegistryClient;
import net.pitan76.mcpitanlib.api.client.render.CompatRenderLayer;
import net.pitan76.mcpitanlib.api.client.render.DrawObjectMV;
import net.pitan76.mcpitanlib.api.client.render.block.entity.event.BlockEntityRenderEvent;
import net.pitan76.mcpitanlib.api.client.render.block.entity.event.CompatBlockEntityRendererConstructArgs;
import net.pitan76.mcpitanlib.api.client.render.block.entity.v2.CompatBlockEntityRenderer;
import net.pitan76.mcpitanlib.api.util.client.render.VertexRenderingUtil;
import net.pitan76.qrblock76.QRBlockEntity;

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
        if (data == null) return;

        e.push();
        DrawObjectMV drawObject = e.getDrawObject(CompatRenderLayer.LINES);
        //VertexRenderingUtil.drawBox(drawObject, minX + 0.5, minY + 0.5, minZ + 0.5, maxX + 0.5, maxY + 0.5, maxZ + 0.5, 1F, 0.0F, 0.0F, 1.0F);
        e.pop();
    }
}
