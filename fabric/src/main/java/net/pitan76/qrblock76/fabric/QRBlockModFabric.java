package net.pitan76.qrblock76.fabric;

import net.pitan76.qrblock76.QRBlockMod;
import net.fabricmc.api.ModInitializer;

public class QRBlockModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new QRBlockMod();
    }
}