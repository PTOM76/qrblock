package net.pitan76.qrblock76;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.pitan76.mcpitanlib.api.block.ExtendBlockEntityProvider;
import net.pitan76.mcpitanlib.api.block.v2.BlockSettingsBuilder;
import net.pitan76.mcpitanlib.api.block.v2.CompatBlock;
import net.pitan76.mcpitanlib.api.block.v2.CompatibleBlockSettings;
import net.pitan76.mcpitanlib.api.event.block.BlockUseEvent;
import net.pitan76.mcpitanlib.api.event.block.TileCreateEvent;
import net.pitan76.mcpitanlib.api.network.PacketByteUtil;
import net.pitan76.mcpitanlib.api.network.v2.ServerNetworking;
import net.pitan76.mcpitanlib.api.util.CompatActionResult;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;
import net.pitan76.mcpitanlib.api.util.client.ClientUtil;
import net.pitan76.qrblock76.client.QRBlockScreen;
import org.jetbrains.annotations.Nullable;

public class QRBlock extends CompatBlock implements ExtendBlockEntityProvider {

    public static final BlockSettingsBuilder SETTINGS = new BlockSettingsBuilder(QRBlockMod._id("qrblock"))
            .copy(CompatIdentifier.of("minecraft", "stone"));

    public QRBlock() {
        this(SETTINGS.build());
    }

    public QRBlock(CompatibleBlockSettings settings) {
        super(settings);
    }

    @Override
    public CompatActionResult onRightClick(BlockUseEvent e) {
        if (e.isSneaking()) return super.onRightClick(e);

        String data = null;
        if (e.hasBlockEntity() && e.getBlockEntity() instanceof QRBlockEntity) {
            QRBlockEntity entity = (QRBlockEntity) e.getBlockEntity();
            data = entity.getData();
        }

        if (e.isClient()) {
            ClientUtil.setScreen(new QRBlockScreen(e.getMidohraPos()));
            return e.success();
        }

        if (data != null) {
            PacketByteBuf buf = PacketByteUtil.create();
            buf.writeString(data);
            ServerNetworking.send(e.player, QRBlockMod._id("qrs2c_screen"), buf);
        }

        return e.success();
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(TileCreateEvent e) {
        return new QRBlockEntity(e);
    }
}
