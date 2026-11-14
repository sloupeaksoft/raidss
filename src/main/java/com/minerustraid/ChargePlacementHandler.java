package com.minerustraid;

import javax.annotation.Nullable;

import com.minerustraid.block.BlockWallCharge;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Replaces placement of MineRust satchel / C4 items:
 * only on the side of MineRust walls/doors, always flush to the clicked face,
 * several charges can be stacked on one spot.
 */
@Mod.EventBusSubscriber(modid = MineRustRaid.MODID)
public final class ChargePlacementHandler {

    private ChargePlacementHandler() {
    }

    @Nullable
    private static BlockWallCharge chargeFor(Item item) {
        ResourceLocation rl = item.getRegistryName();
        if (rl == null) {
            return null;
        }
        String id = rl.toString();
        for (String s : RaidConfig.satchelItems) {
            if (id.equals(s)) {
                return ModContent.SATCHEL_WALL;
            }
        }
        for (String s : RaidConfig.c4Items) {
            if (id.equals(s)) {
                return ModContent.C4_WALL;
            }
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        BlockWallCharge charge = chargeFor(stack.getItem());
        if (charge == null) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);

        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }
        EntityPlayer player = event.getEntityPlayer();
        BlockPos clicked = event.getPos();
        EnumFacing face = event.getFace();
        IBlockState clickedState = world.getBlockState(clicked);

        // clicked an existing charge of the same type -> stack one more
        if (clickedState.getBlock() == charge) {
            if (charge.tryAddCharge(world, clicked, clickedState)) {
                consume(player, stack, world, clicked, charge);
            } else {
                msg(player, "msg.minerustraid.full");
            }
            return;
        }

        if (face == null || face.getAxis() == EnumFacing.Axis.Y) {
            msg(player, "msg.minerustraid.only_wall");
            return;
        }
        if (RaidLogic.getTier(clickedState) == null) {
            msg(player, "msg.minerustraid.only_minerust");
            return;
        }

        BlockPos place = clicked.offset(face);
        IBlockState placeState = world.getBlockState(place);
        if (placeState.getBlock() == charge && placeState.getValue(BlockWallCharge.FACING) == face) {
            if (charge.tryAddCharge(world, place, placeState)) {
                consume(player, stack, world, place, charge);
            } else {
                msg(player, "msg.minerustraid.full");
            }
            return;
        }
        if (!placeState.getBlock().isReplaceable(world, place) || !player.canPlayerEdit(place, face, stack)) {
            msg(player, "msg.minerustraid.blocked");
            return;
        }
        world.setBlockState(place, charge.getDefaultState()
                .withProperty(BlockWallCharge.FACING, face)
                .withProperty(BlockWallCharge.COUNT, 1), 3);
        consume(player, stack, world, place, charge);
    }

    private static void consume(EntityPlayer player, ItemStack stack, World world, BlockPos pos, BlockWallCharge charge) {
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        if (charge == ModContent.C4_WALL) {
            world.playSound(null, pos, ModSounds.C4_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.playSound(null, pos, ModSounds.C4_BEEP, SoundCategory.BLOCKS, 1.0F, 1.0F);
        } else {
            world.playSound(null, pos, ModSounds.SATCHEL_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.playSound(null, pos, ModSounds.SATCHEL_FUSE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static void msg(EntityPlayer player, String key) {
        player.sendStatusMessage(new TextComponentTranslation(key), true);
    }
}
