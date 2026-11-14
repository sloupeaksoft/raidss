package com.minerustraid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public final class RaidLogic {

    public enum Tier { TWIG, WOOD, STONE, METAL, ARMORED, DOOR }

    private static Map<String, Tier> cache;

    private RaidLogic() {
    }

    public static void invalidateCache() {
        cache = null;
    }

    private static Map<String, Tier> map() {
        if (cache == null) {
            Map<String, Tier> m = new HashMap<String, Tier>();
            put(m, RaidConfig.twigBlocks, Tier.TWIG);
            put(m, RaidConfig.woodBlocks, Tier.WOOD);
            put(m, RaidConfig.stoneBlocks, Tier.STONE);
            put(m, RaidConfig.metalBlocks, Tier.METAL);
            put(m, RaidConfig.armoredBlocks, Tier.ARMORED);
            put(m, RaidConfig.doorBlocks, Tier.DOOR);
            cache = m;
        }
        return cache;
    }

    private static void put(Map<String, Tier> m, String[] ids, Tier tier) {
        if (ids == null) {
            return;
        }
        for (String id : ids) {
            if (id != null && !id.trim().isEmpty()) {
                m.put(id.trim().toLowerCase(Locale.ROOT), tier);
            }
        }
    }

    @Nullable
    public static Tier getTier(IBlockState state) {
        ResourceLocation rl = state.getBlock().getRegistryName();
        return rl == null ? null : map().get(rl.toString());
    }

    public static double getHp(Tier tier) {
        switch (tier) {
            case TWIG: return RaidConfig.twigHp;
            case WOOD: return RaidConfig.woodHp;
            case STONE: return RaidConfig.stoneHp;
            case METAL: return RaidConfig.metalHp;
            case ARMORED: return RaidConfig.armoredHp;
            case DOOR: return RaidConfig.doorHp;
            default: return 1.0D;
        }
    }

    /** For doors the lower half is always used as the key. */
    public static BlockPos normalize(World world, BlockPos pos, Tier tier) {
        if (tier != Tier.DOOR) {
            return pos;
        }
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockDoor) {
            return state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER ? pos.down() : pos;
        }
        if (getTier(world.getBlockState(pos.down())) == Tier.DOOR) {
            return pos.down();
        }
        return pos;
    }

    /** Applies raid damage to one block. Returns true if the block is a raid target. */
    public static boolean applyDamage(World world, BlockPos pos, double amount) {
        if (world.isRemote || amount <= 0.0D) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        Tier tier = getTier(state);
        if (tier == null) {
            return false;
        }
        BlockPos key = normalize(world, pos, tier);
        RaidDamageData data = RaidDamageData.get(world);
        double hp = getHp(tier);
        double total = data.add(key, amount);
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                    key.getX() + 0.5D, key.getY() + 0.5D, key.getZ() + 0.5D,
                    30, 0.4D, 0.6D, 0.4D, 0.15D, Block.getStateId(state));
        }
        if (total >= hp - 1.0E-4D) {
            data.remove(key);
            clearCracks(world, key);
            destroy(world, key, tier);
        } else {
            sendCracks(world, key, tier, total / hp);
        }
        return true;
    }

    /** Rocket area: every target in a (2r+1)^3 cube takes damage once. */
    public static void applyArea(World world, BlockPos center, int radius, double amount) {
        if (world.isRemote) {
            return;
        }
        Set<BlockPos> done = new HashSet<BlockPos>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = center.add(dx, dy, dz);
                    Tier tier = getTier(world.getBlockState(p));
                    if (tier == null) {
                        continue;
                    }
                    BlockPos key = normalize(world, p, tier);
                    if (done.add(key)) {
                        applyDamage(world, key, amount);
                    }
                }
            }
        }
    }

    private static void destroy(World world, BlockPos key, Tier tier) {
        if (tier == Tier.DOOR) {
            BlockPos up = key.up();
            IBlockState upState = world.getBlockState(up);
            if (getTier(upState) == Tier.DOOR) {
                world.playEvent(2001, up, Block.getStateId(upState));
                world.setBlockState(up, Blocks.AIR.getDefaultState(), 2);
            }
        }
        IBlockState state = world.getBlockState(key);
        world.playEvent(2001, key, Block.getStateId(state));
        world.setBlockState(key, Blocks.AIR.getDefaultState(), 3);
    }

    private static int breakerId(BlockPos pos) {
        return -1 - (pos.hashCode() & 0x3FFFFFFF);
    }

    public static void sendCracks(World world, BlockPos key, Tier tier, double fraction) {
        int progress = (int) Math.floor(fraction * 10.0D);
        progress = Math.max(0, Math.min(9, progress));
        world.sendBlockBreakProgress(breakerId(key), key, progress);
        if (tier == Tier.DOOR && getTier(world.getBlockState(key.up())) == Tier.DOOR) {
            world.sendBlockBreakProgress(breakerId(key.up()), key.up(), progress);
        }
    }

    public static void clearCracks(World world, BlockPos key) {
        world.sendBlockBreakProgress(breakerId(key), key, -1);
        world.sendBlockBreakProgress(breakerId(key.up()), key.up(), -1);
    }

    /** Re-sends crack overlays (the client forgets them after a while) and removes stale entries. */
    public static void refreshCracks(World world) {
        RaidDamageData data = RaidDamageData.get(world);
        for (Map.Entry<Long, Double> e : data.snapshot().entrySet()) {
            BlockPos key = BlockPos.fromLong(e.getKey());
            if (!world.isBlockLoaded(key)) {
                continue;
            }
            Tier tier = getTier(world.getBlockState(key));
            if (tier == null) {
                data.remove(e.getKey());
                clearCracks(world, key);
                continue;
            }
            sendCracks(world, key, tier, e.getValue() / getHp(tier));
        }
    }

    public static void forget(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) {
            return;
        }
        Tier tier = getTier(state);
        BlockPos key = tier == null ? pos : normalize(world, pos, tier);
        RaidDamageData data = RaidDamageData.get(world);
        data.remove(key);
        clearCracks(world, key);
    }
}
