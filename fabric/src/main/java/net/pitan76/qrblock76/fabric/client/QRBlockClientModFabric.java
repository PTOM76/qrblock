package net.pitan76.qrblock76.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.pitan76.qrblock76.client.QRBlockClientMod;

public class QRBlockClientModFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        QRBlockClientMod.init();
    }
}