package net.pitan76.qrblock76.client;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.pitan76.mcpitanlib.api.client.SimpleScreen;
import net.pitan76.mcpitanlib.api.network.v2.ClientNetworking;
import net.pitan76.mcpitanlib.api.util.TextUtil;
import net.pitan76.mcpitanlib.api.util.client.ClientUtil;
import net.pitan76.mcpitanlib.api.util.client.widget.TextFieldUtil;
import net.pitan76.mcpitanlib.midohra.network.CompatPacketByteBuf;
import net.pitan76.mcpitanlib.midohra.util.math.BlockPos;
import net.pitan76.qrblock76.QRBlockMod;

public class QRBlockScreen extends SimpleScreen {
    private final BlockPos pos;
    public TextFieldWidget textField;

    public QRBlockScreen(Text title, BlockPos pos) {
        super(title);
        this.pos = pos;
    }

    public QRBlockScreen(BlockPos pos) {
        this(TextUtil.translatable("screen.qrblock76.qr_block"), pos);
    }

    @Override
    public void initOverride() {
        super.initOverride();

        textField = TextFieldUtil.create(ClientUtil.getTextRenderer(),
                ClientUtil.getScreen().width / 2 - 100, ClientUtil.getScreen().height / 2 - 10, 200, 20,
                TextUtil.of(""));

        TextFieldUtil.setMaxLength(textField, 512);

        if (QRBlockClientMod.hasTextCache())
            TextFieldUtil.setText(textField, QRBlockClientMod.useTextCache());

        addDrawableChild_compatibility(textField);
    }

    @Override
    public void closeOverride() {
        super.closeOverride();

        String text = TextFieldUtil.getText(textField);

        CompatPacketByteBuf buf = CompatPacketByteBuf.create();
        buf.writeBlockPos(pos);
        buf.writeString(text);

        ClientNetworking.send(QRBlockMod._id("qrc2s"), buf);
        QRBlockClientMod.syncQRBEText(pos, text);
    }
}
