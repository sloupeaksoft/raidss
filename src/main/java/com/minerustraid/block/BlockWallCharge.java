package com.minerustraid.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.minerustraid.MineRustRaid;
import com.minerustraid.ModSounds;
import com.minerustraid.RaidConfig;
import com.minerustraid.RaidLogic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Charge glued to the side of a wall. FACING = direction from the wall to the charge
 * (= the clicked face), so the model always lies flat on the wall regardless of camera angle.
 */
public class BlockWallCharge extends Block {

    public enum Type { SATCHEL, C4 }

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyInteger COUNT = PropertyInteger.create("count", 1, 4);

    private static final EnumFacing[] META_FACINGS = {EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};

    private static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.2D, 0.15D, 0.75D, 0.8D, 0.85D, 1.0D);
    private static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.2D, 0.15D, 0.0D, 0.8D, 0.85D, 0.25D);
    private static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.0D, 0.15D, 0.2D, 0.25D, 0.85D, 0.8D);
    private static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(0.75D, 0.15D, 0.2D, 1.0D, 0.85D, 0.8D);

    private final Type type;

    public BlockWallCharge(Type type, String name) {
        super(Material.CIRCUITS);
        this.type = type;
        setRegistryName(MineRustRaid.MODID, name);
        setTranslationKey(MineRustRaid.MODID + "." + name);
        setHardness(0.3F);
        setResistance(0.5F);
        setSoundType(SoundType.METAL);
        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(COUNT, 1));
    }

    public Type getType() {
        return type;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, COUNT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, META_FACINGS[meta & 3]).withProperty(COUNT, ((meta >> 2) & 3) + 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        int f = 0;
        for (int i = 0; i < META_FACINGS.length; i++) {
            if (META_FACINGS[i] == facing) {
                f = i;
            }
        }
        return f | ((state.getValue(COUNT) - 1) << 2);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case SOUTH: return AABB_SOUTH;
            case EAST: return AABB_EAST;
            case WEST: return AABB_WEST;
            default: return AABB_NORTH;
        }
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    public int getFuseTicks() {
        return Math.max(1, type == Type.C4 ? RaidConfig.c4FuseTicks : RaidConfig.satchelFuseTicks);
    }

    public boolean tryAddCharge(World world, BlockPos pos, IBlockState state) {
        int count = state.getValue(COUNT);
        int max = Math.max(1, Math.min(4, RaidConfig.maxChargesPerBlock));
        if (count >= max) {
            return false;
        }
        world.setBlockState(pos, state.withProperty(COUNT, count + 1), 3);
        return true;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            worldIn.scheduleUpdate(pos, this, getFuseTicks());
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            detonate(worldIn, pos, state, rand);
        }
    }

    private void detonate(World world, BlockPos pos, IBlockState state, Random rand) {
        int count = state.getValue(COUNT);
        EnumFacing facing = state.getValue(FACING);
        BlockPos wallPos = pos.offset(facing.getOpposite());

        int exploded = count;
        if (type == Type.SATCHEL) {
            exploded = 0;
            for (int i = 0; i < count; i++) {
                if (rand.nextDouble() < RaidConfig.satchelExplodeChance) {
                    exploded++;
                }
            }
        }

        world.setBlockToAir(pos);
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        if (exploded <= 0) {
            // dud
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 0.8F);
            if (world instanceof WorldServer) {
                ((WorldServer) world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 12, 0.2D, 0.2D, 0.2D, 0.02D);
            }
            return;
        }

        // raid damage first, then the original MineRust Addons explosion
        double perCharge = type == Type.C4 ? RaidConfig.c4Damage : RaidConfig.satchelDamage;
        RaidLogic.applyDamage(world, wallPos, perCharge * exploded);

        int bx = pos.getX();
        int by = pos.getY();
        int bz = pos.getZ();
        boolean terrain = RaidConfig.chargeBreakTerrain;
        if (type == Type.C4) {
            world.playSound(null, pos, ModSounds.C4_EXPLODE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            float p = (float) RaidConfig.c4ExplosionPower;
            boom(world, bx, by + 1, bz, p, terrain);
            boom(world, bx, by, bz - 1, p, terrain);
            boom(world, bx + 1, by + 1, bz + 1, p, terrain);
            boom(world, bx + 1, by + 1, bz - 1, p, terrain);
            boom(world, bx, by, bz, p, terrain);
            boom(world, bx, by - 1, bz, p + 0.2F, terrain);
            boom(world, bx, by - 1, bz, p, terrain);
        } else {
            world.playSound(null, pos, ModSounds.STONE_DESTROY, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.playSound(null, pos, ModSounds.SATCHEL_EXPLODE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            float p = (float) RaidConfig.satchelExplosionPower;
            boom(world, bx, by + 1, bz, p, terrain);
            boom(world, bx + 1, by + 1, bz + 1, p, terrain);
            boom(world, bx, by, bz, p, terrain);
            boom(world, bx, by - 1, bz, p, terrain);
        }
    }

    /** True while a C4 / satchel explosion is running (see ChargeExplosionFilter). */
    public static boolean chargeExploding = false;

    private static void boom(World world, int x, int y, int z, float power, boolean terrain) {
        chargeExploding = true;
        try {
            world.newExplosion(null, x, y, z, power, false, terrain);
        } finally {
            chargeExploding = false;
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (worldIn.isRemote) {
            return;
        }
        BlockPos wall = pos.offset(state.getValue(FACING).getOpposite());
        if (worldIn.isAirBlock(wall)) {
            dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.75D;
        double z = pos.getZ() + 0.5D;
        if (type == Type.SATCHEL) {
            worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.03D, 0.0D);
        } else {
            worldIn.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Nullable
    public Item getSourceItem() {
        String[] ids = type == Type.C4 ? RaidConfig.c4Items : RaidConfig.satchelItems;
        if (ids == null || ids.length == 0) {
            return null;
        }
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(ids[0]));
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Item item = getSourceItem();
        if (item != null) {
            drops.add(new ItemStack(item, state.getValue(COUNT)));
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        Item item = getSourceItem();
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return false;
    }
}
