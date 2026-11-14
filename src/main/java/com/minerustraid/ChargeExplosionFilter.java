package com.minerustraid;

import com.minerustraid.block.BlockWallCharge;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * C4 / satchel explosions may only break MineRust Addons building blocks.
 * Dirt, grass, stone, wood and any other blocks stay untouched.
 */
@Mod.EventBusSubscriber(modid = MineRustRaid.MODID)
public final class ChargeExplosionFilter {

    private ChargeExplosionFilter() {
    }

    @SubscribeEvent
    public static void onDetonate(ExplosionEvent.Detonate event) {
        if (!BlockWallCharge.chargeExploding) {
            return;
        }
        final World world = event.getWorld();
        event.getAffectedBlocks().removeIf(p -> RaidLogic.getTier(world.getBlockState(p)) == null);
    }
}
