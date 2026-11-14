package com.minerustraid;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = MineRustRaid.MODID)
public final class RaidEvents {

    private RaidEvents() {
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (MineRustRaid.MODID.equals(event.getModID())) {
            ConfigManager.sync(MineRustRaid.MODID, Config.Type.INSTANCE);
            RaidLogic.invalidateCache();
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
            return;
        }
        if (event.world.getTotalWorldTime() % 100L == 0L) {
            RaidLogic.refreshCracks(event.world);
        }
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        RaidLogic.forget(event.getWorld(), event.getPos(), event.getState());
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.PlaceEvent event) {
        RaidLogic.forget(event.getWorld(), event.getPos(), event.getPlacedBlock());
    }
}
