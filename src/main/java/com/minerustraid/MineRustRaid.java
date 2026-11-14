package com.minerustraid;

import com.minerustraid.entity.EntityRocket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

@Mod(modid = MineRustRaid.MODID, name = MineRustRaid.NAME, version = MineRustRaid.VERSION,
        dependencies = "required-after:minerustaddons", acceptedMinecraftVersions = "[1.12.2]")
public class MineRustRaid {
    public static final String MODID = "minerustraid";
    public static final String NAME = "MineRust Raid Addon";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MODID)
    public static MineRustRaid instance;

    @SidedProxy(clientSide = "com.minerustraid.client.ClientProxy", serverSide = "com.minerustraid.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "rocket"), EntityRocket.class,
                MODID + ".rocket", 0, this, 128, 1, true);
        proxy.preInit();
    }
}
