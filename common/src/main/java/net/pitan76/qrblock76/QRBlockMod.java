package net.pitan76.qrblock76;

import net.pitan76.mcpitanlib.api.CommonModInitializer;
import net.pitan76.mcpitanlib.api.registry.v2.CompatRegistryV2;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;

public class QRBlockMod extends CommonModInitializer {
    public static final String MOD_ID = "qrblock76";
    public static final String MOD_NAME = "QRBlock";

    public static QRBlockMod INSTANCE;
    public static CompatRegistryV2 registry;

    @Override
    public void init() {
        INSTANCE = this;
        registry = super.registry;
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