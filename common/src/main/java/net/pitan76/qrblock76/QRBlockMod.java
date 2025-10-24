package net.pitan76.qrblock76;

import net.minecraft.block.entity.BlockEntityType;
import net.pitan76.mcpitanlib.api.CommonModInitializer;
import net.pitan76.mcpitanlib.api.block.v2.BlockSettingsBuilder;
import net.pitan76.mcpitanlib.api.item.v2.ItemSettingsBuilder;
import net.pitan76.mcpitanlib.api.network.PacketByteUtil;
import net.pitan76.mcpitanlib.api.network.v2.ServerNetworking;
import net.pitan76.mcpitanlib.api.registry.result.SupplierResult;
import net.pitan76.mcpitanlib.api.registry.v2.CompatRegistryV2;
import net.pitan76.mcpitanlib.api.sound.CompatBlockSoundGroup;
import net.pitan76.mcpitanlib.api.tile.v2.BlockEntityTypeBuilder;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;
import net.pitan76.mcpitanlib.api.util.client.ClientUtil;
import net.pitan76.mcpitanlib.core.datafixer.Pair;
import net.pitan76.mcpitanlib.midohra.block.SupplierBlockWrapper;
import net.pitan76.mcpitanlib.midohra.block.entity.BlockEntityWrapper;
import net.pitan76.mcpitanlib.midohra.easybuilder.BlockWithBlockEntityBuilder;
import net.pitan76.mcpitanlib.midohra.item.ItemGroups;
import net.pitan76.mcpitanlib.midohra.item.SupplierItemWrapper;
import net.pitan76.mcpitanlib.midohra.network.CompatPacketByteBuf;
import net.pitan76.mcpitanlib.midohra.util.math.BlockPos;
import net.pitan76.mcpitanlib.midohra.world.World;
import net.pitan76.qrblock76.client.QRBlockScreen;

public class QRBlockMod extends CommonModInitializer {
    public static final String MOD_ID = "qrblock76";
    public static final String MOD_NAME = "QRBlock";

    public static QRBlockMod INSTANCE;
    public static CompatRegistryV2 registry;

    public static SupplierBlockWrapper QR_BLOCK;
    public static SupplierItemWrapper QR_BLOCK_ITEM;
    public static SupplierResult<BlockEntityType<QRBlockEntity>> QR_BLOCK_ENTITY_TYPE;

    @Override
    public void init() {
        INSTANCE = this;
        registry = super.registry;

        Pair<SupplierBlockWrapper, SupplierItemWrapper> pairQR_BLOCK = BlockWithBlockEntityBuilder.of(new BlockSettingsBuilder(QRBlockMod._id("qrblock"))
                        .strength(1.0f, 1.0f)
                        .sounds(CompatBlockSoundGroup.STONE))
                .applyBlockEntity(() -> QR_BLOCK_ENTITY_TYPE.getOrNull())
                .onRightClick((e) -> {
                    if (e.isSneaking()) return e.pass();

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
                        CompatPacketByteBuf buf = CompatPacketByteBuf.create();
                        PacketByteUtil.writeString(buf, data);
                        ServerNetworking.send(e.player, QRBlockMod._id("qrs2c_screen"), buf);
                    }

                    return e.success();
        }).buildWithItem(this, new ItemSettingsBuilder(QRBlockMod._id("qrblock")).addGroup(ItemGroups.FUNCTIONAL).build());

        QR_BLOCK = pairQR_BLOCK.getA();
        QR_BLOCK_ITEM = pairQR_BLOCK.getB();
        QR_BLOCK_ENTITY_TYPE = registry.registerBlockEntityType(_id("qrblock"), BlockEntityTypeBuilder.create(QRBlockEntity::new, QR_BLOCK));

        ServerNetworking.registerReceiver(_id("qrc2s"), (e) -> {
            BlockPos pos = e.getCompatBuf().readBlockPosMidohra();
            String text = PacketByteUtil.readString(e.getCompatBuf());

            e.execute(() -> {
                World world = e.getMidohraWorld();

                BlockEntityWrapper blockEntity = world.getBlockEntity(pos);
                if (!(blockEntity.get() instanceof QRBlockEntity)) return;

                QRBlockEntity entity = (QRBlockEntity) blockEntity.get();
                entity.setData(text);
            });
        });

        ServerNetworking.registerReceiver(_id("request_qrdata"), (e) -> {
            BlockPos pos = e.getCompatBuf().readBlockPosMidohra();

            e.execute(() -> {
                World world = e.getMidohraWorld();
                BlockEntityWrapper blockEntityWrapper = world.getBlockEntity(pos);
                if (!(blockEntityWrapper.get() instanceof QRBlockEntity)) return;

                QRBlockEntity entity = (QRBlockEntity) blockEntityWrapper.get();
                String data = entity.getData();
                if (data == null || data.isEmpty()) return;

                CompatPacketByteBuf buf = CompatPacketByteBuf.create();
                buf.writeBlockPos(pos);
                PacketByteUtil.writeString(buf, data);
                ServerNetworking.send(e.serverPlayer, _id("qrs2c"), buf);
            });
        });
    }

    // ----
    /**
     * @param path The path of the id
     * @return The id
     */
    public static CompatIdentifier _id(String path) {
        return CompatIdentifier.of(MOD_ID, path);
    }

    @Override
    public String getId() {
        return MOD_ID;
    }

    @Override
    public String getName() {
        return MOD_NAME;
    }
}