package net.pitan76.qrblock76.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.pitan76.qrblock76.QRBlockMod;

@Mod(QRBlockMod.MOD_ID)
public class QRBlockModNeoForge {
    public QRBlockModNeoForge(ModContainer modContainer) {
        IEventBus eventBus = modContainer.getEventBus();

        new QRBlockMod();
    }
}