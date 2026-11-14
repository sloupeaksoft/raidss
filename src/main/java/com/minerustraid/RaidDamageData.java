package com.minerustraid;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class RaidDamageData extends WorldSavedData {
    public static final String NAME = "minerustraid_damage";
    private final Map<Long, Double> damage = new HashMap<Long, Double>();

    public RaidDamageData() {
        this(NAME);
    }

    public RaidDamageData(String name) {
        super(name);
    }

    public static RaidDamageData get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        RaidDamageData data = (RaidDamageData) storage.getOrLoadData(RaidDamageData.class, NAME);
        if (data == null) {
            data = new RaidDamageData();
            storage.setData(NAME, data);
        }
        return data;
    }

    public double add(BlockPos pos, double amount) {
        long key = pos.toLong();
        Double old = damage.get(key);
        double value = (old == null ? 0.0D : old.doubleValue()) + amount;
        damage.put(key, value);
        markDirty();
        return value;
    }

    public void remove(BlockPos pos) {
        remove(pos.toLong());
    }

    public void remove(long key) {
        if (damage.remove(key) != null) {
            markDirty();
        }
    }

    public Map<Long, Double> snapshot() {
        return new HashMap<Long, Double>(damage);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        damage.clear();
        NBTTagList list = nbt.getTagList("entries", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound c = list.getCompoundTagAt(i);
            damage.put(c.getLong("pos"), c.getDouble("dmg"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<Long, Double> e : damage.entrySet()) {
            NBTTagCompound c = new NBTTagCompound();
            c.setLong("pos", e.getKey());
            c.setDouble("dmg", e.getValue());
            list.appendTag(c);
        }
        nbt.setTag("entries", list);
        return nbt;
    }
}
