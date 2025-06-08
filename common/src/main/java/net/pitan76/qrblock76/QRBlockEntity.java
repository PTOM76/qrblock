package net.pitan76.qrblock76;

import net.minecraft.block.entity.BlockEntityType;
import net.pitan76.mcpitanlib.api.event.block.TileCreateEvent;
import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.tile.CompatBlockEntity;
import net.pitan76.mcpitanlib.api.util.NbtUtil;

public class QRBlockEntity extends CompatBlockEntity {

    public String data = "";

    public QRBlockEntity(BlockEntityType<?> type, TileCreateEvent e) {
        super(type, e);
    }

    public QRBlockEntity(TileCreateEvent e) {
        this(QRBlockMod.QR_BLOCK_ENTITY_TYPE.getOrNull(), e);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public void writeNbt(WriteNbtArgs args) {
        super.writeNbt(args);
        NbtUtil.putString(args.getNbt(), "data", getData());
    }

    @Override
    public void readNbt(ReadNbtArgs args) {
        super.readNbt(args);
        setData(NbtUtil.getString(args.getNbt(), "data"));
    }
}
