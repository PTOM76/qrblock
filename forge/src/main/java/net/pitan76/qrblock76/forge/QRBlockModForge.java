package net.pitan76.qrblock76.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.eventbus.api.IEventBus;
import net.pitan76.qrblock76.QRBlockMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(QRBlockMod.MOD_ID)
public class QRBlockModForge {
    public QRBlockModForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        EventBuses.registerModEventBus(QRBlockMod.MOD_ID, modEventBus);
        new QRBlockMod();
    }
}