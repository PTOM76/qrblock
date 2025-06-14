package net.pitan76.qrblock76;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.pitan76.mcpitanlib.api.CommonModInitializer;
import net.pitan76.mcpitanlib.api.block.v2.BlockSettingsBuilder;
import net.pitan76.mcpitanlib.api.item.v2.ItemSettingsBuilder;
import net.pitan76.mcpitanlib.api.network.v2.ServerNetworking;
import net.pitan76.mcpitanlib.api.registry.result.RegistryResult;
import net.pitan76.mcpitanlib.api.registry.result.SupplierResult;
import net.pitan76.mcpitanlib.api.registry.v2.CompatRegistryV2;
import net.pitan76.mcpitanlib.api.tile.BlockEntityTypeBuilder;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;
import net.pitan76.mcpitanlib.api.util.item.ItemUtil;
import net.pitan76.mcpitanlib.midohra.network.CompatPacketByteBuf;
import net.pitan76.mcpitanlib.midohra.util.math.BlockPos;
import net.pitan76.mcpitanlib.midohra.world.World;

public class QRBlockMod extends CommonModInitializer {
    public static final String MOD_ID = "qrblock76";
    public static final String MOD_NAME = "QRBlock";

    public static QRBlockMod INSTANCE;
    public static CompatRegistryV2 registry;

    public static final BlockSettingsBuilder SETTINGS = new BlockSettingsBuilder(QRBlockMod._id("qrblock"))
            .copy(CompatIdentifier.of("minecraft", "stone"));

    public static RegistryResult<Block> QR_BLOCK;
    public static SupplierResult<BlockEntityType<QRBlockEntity>> QR_BLOCK_ENTITY_TYPE;

//    public static QRBlock QR_BLOCK;
//    public static SupplierResult<BlockEntityType<BlockEntity>> QR_BLOCK_ENTITY_TYPE;

    @Override
    public void init() {
        INSTANCE = this;
        registry = super.registry;

//        BlockWithBlockEntityBuilder.of(SETTINGS).applyBlockEntity(QR_BLOCK_ENTITY_TYPE.getOrNull())
//                .onRightClick((e) -> {
//            return e.success();
//        }).build(this);
//
//        QR_BLOCK_ENTITY_TYPE = registry.registerBlockEntityType(_id("qrblock"), BlockEntityTypeBuilder.create(QRBlockEntity::new, QR_BLOCK));

        QR_BLOCK = registry.registerBlock(_id("qrblock"), QRBlock::new);
        QR_BLOCK_ENTITY_TYPE = registry.registerBlockEntityType(_id("qrblock"), BlockEntityTypeBuilder.create(QRBlockEntity::new, QR_BLOCK.getOrNull()));

        registry.registerItem(_id("qrblock"), () -> ItemUtil.create(QR_BLOCK.getOrNull(),
                new ItemSettingsBuilder(QRBlockMod._id("qrblock")).build()));

        ServerNetworking.registerReceiver(_id("qrc2s"), (e) -> {
            BlockPos pos = BlockPos.of(e.getBuf().readBlockPos());
            String text = e.getBuf().readString();

            e.server.execute(() -> {
                World world = World.of(e.getPlayer().getWorld());

                BlockEntity blockEntity = world.getBlockEntity(pos).get();
                if (!(blockEntity instanceof QRBlockEntity)) return;

                QRBlockEntity entity = (QRBlockEntity) blockEntity;
                entity.setData(text);
            });
        });

        ServerNetworking.registerReceiver(_id("request_qrdata"), (e) -> {
            BlockPos pos = e.getCompatBuf().readBlockPosMidohra();

            e.execute(() -> {
                World world = World.of(e.getWorld());
                BlockEntity blockEntity = world.getBlockEntity(pos).get();
                if (!(blockEntity instanceof QRBlockEntity)) return;

                QRBlockEntity entity = (QRBlockEntity) blockEntity;
                String data = entity.getData();
                if (data == null || data.isEmpty()) return;

                CompatPacketByteBuf buf = CompatPacketByteBuf.create();
                buf.writeBlockPos(pos);
                buf.writeString(data);
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