package com.minerustraid.item;

import java.util.List;

import javax.annotation.Nullable;

import com.minerustraid.MineRustRaid;
import com.minerustraid.ModContent;
import com.minerustraid.RaidConfig;
import com.minerustraid.entity.EntityRocket;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRocketLauncher extends Item {

    public ItemRocketLauncher() {
        setRegistryName(MineRustRaid.MODID, "rocket_launcher");
        setTranslationKey(MineRustRaid.MODID + ".rocket_launcher");
        setMaxStackSize(1);
        setFull3D();
        setCreativeTab(CreativeTabs.COMBAT);
    }

    private static boolean isAmmo(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == ModContent.ROCKET;
    }

    private static ItemStack findAmmo(EntityPlayer player) {
        ItemStack off = player.getHeldItem(EnumHand.OFF_HAND);
        if (isAmmo(off)) {
            return off;
        }
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack s = player.inventory.getStackInSlot(i);
            if (isAmmo(s)) {
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        boolean creative = playerIn.capabilities.isCreativeMode;
        ItemStack ammo = findAmmo(playerIn);
        if (!creative && ammo.isEmpty()) {
            if (!worldIn.isRemote) {
                playerIn.sendStatusMessage(new TextComponentTranslation("msg.minerustraid.no_rockets"), true);
            }
            worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 1.0F, 1.2F);
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }
        if (!worldIn.isRemote) {
            EntityRocket rocket = new EntityRocket(worldIn, playerIn);
            rocket.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, (float) RaidConfig.rocketSpeed, 0.5F);
            worldIn.spawnEntity(rocket);
            if (!creative) {
                ammo.shrink(1);
            }
        }
        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_FIREWORK_LAUNCH, SoundCategory.PLAYERS, 2.0F, 0.6F);
        playerIn.getCooldownTracker().setCooldown(this, Math.max(1, RaidConfig.launcherCooldownTicks));
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("tooltip.minerustraid.rocket_launcher"));
    }
}
