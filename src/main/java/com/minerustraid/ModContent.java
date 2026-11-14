package com.minerustraid;

import com.minerustraid.block.BlockWallCharge;
import com.minerustraid.item.ItemRocketLauncher;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = MineRustRaid.MODID)
public final class ModContent {

    public static BlockWallCharge SATCHEL_WALL;
    public static BlockWallCharge C4_WALL;
    public static Item ROCKET;
    public static Item ROCKET_LAUNCHER;

    private ModContent() {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        SATCHEL_WALL = new BlockWallCharge(BlockWallCharge.Type.SATCHEL, "satchel_wall");
        C4_WALL = new BlockWallCharge(BlockWallCharge.Type.C4, "c4_wall");
        event.getRegistry().register(SATCHEL_WALL);
        event.getRegistry().register(C4_WALL);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ROCKET = new Item();
        ROCKET.setRegistryName(MineRustRaid.MODID, "rocket");
        ROCKET.setTranslationKey(MineRustRaid.MODID + ".rocket");
        ROCKET.setCreativeTab(CreativeTabs.COMBAT);
        ROCKET.setMaxStackSize(16);

        ROCKET_LAUNCHER = new ItemRocketLauncher();

        event.getRegistry().register(ROCKET);
        event.getRegistry().register(ROCKET_LAUNCHER);
    }
}
