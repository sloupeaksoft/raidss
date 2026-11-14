package com.minerustraid;

import net.minecraftforge.common.config.Config;

@Config(modid = MineRustRaid.MODID)
public class RaidConfig {

    @Config.Comment("Satchel explode chance (0.8 = 80%)")
    @Config.RangeDouble(min = 0.0, max = 1.0)
    public static double satchelExplodeChance = 0.8D;

    @Config.Comment("Satchel fuse in ticks (20 ticks = 1 second). MineRust Addons = 148")
    public static int satchelFuseTicks = 148;

    @Config.Comment("C4 fuse in ticks (20 ticks = 1 second). MineRust Addons = 152")
    public static int c4FuseTicks = 152;

    @Config.Comment("Max charges stacked on one wall spot (1-4)")
    @Config.RangeInt(min = 1, max = 4)
    public static int maxChargesPerBlock = 4;

    @Config.Comment("Raid damage of one satchel")
    public static double satchelDamage = 1.0D;

    @Config.Comment("Raid damage of one C4")
    public static double c4Damage = 2.0D;

    @Config.Comment("Raid damage of one rocket (to every target block in the 3x3x3 area)")
    public static double rocketDamage = 1.0D;

    @Config.Comment("HP of iron doors (2 satchels / 1 C4 / 2 rockets)")
    public static double doorHp = 2.0D;

    @Config.Comment("HP of twig (hay) blocks")
    public static double twigHp = 1.0D;

    @Config.Comment("HP of wood blocks (2 rockets)")
    public static double woodHp = 2.0D;

    @Config.Comment("HP of stone blocks (4 rockets)")
    public static double stoneHp = 4.0D;

    @Config.Comment("HP of metal blocks (8 rockets)")
    public static double metalHp = 8.0D;

    @Config.Comment("HP of armored blocks (12 rockets)")
    public static double armoredHp = 12.0D;

    @Config.Comment("Power of each C4 explosion (original MineRust Addons = 0.7, 7 explosions around the charge)")
    public static double c4ExplosionPower = 0.7D;

    @Config.Comment("Power of each satchel explosion (original MineRust Addons = 0.9, 4 explosions around the charge)")
    public static double satchelExplosionPower = 0.9D;

    @Config.Comment("C4 / satchel explosions may break MineRust building blocks (walls, doors...). Dirt, stone and other blocks are never broken")
    public static boolean chargeBreakTerrain = true;

    @Config.Comment("Explosion power of rockets (visual + entity damage, does NOT break terrain)")
    public static double rocketExplosionPower = 2.5D;

    @Config.Comment("Direct hit damage of a rocket")
    public static double rocketDirectDamage = 20.0D;

    @Config.Comment("Rocket speed")
    public static double rocketSpeed = 1.4D;

    @Config.Comment("Rocket gravity per tick")
    public static double rocketGravity = 0.004D;

    @Config.Comment("Rocket lifetime in ticks")
    public static int rocketLifetimeTicks = 200;

    @Config.Comment("Rocket launcher cooldown in ticks")
    public static int launcherCooldownTicks = 40;

    @Config.Comment("Items that place a wall satchel")
    public static String[] satchelItems = {
            "minerustaddons:sachel",
            "minerustaddons:sacheldown"
    };

    @Config.Comment("Items that place a wall C4")
    public static String[] c4Items = {
            "minerustaddons:c4",
            "minerustaddons:c4down"
    };

    @Config.Comment("Door blocks (iron door tier, both halves)")
    public static String[] doorBlocks = {
            "minecraft:iron_door",
            "minerustaddons:door1lower",
            "minerustaddons:door1up",
            "minerustaddons:door2lower",
            "minerustaddons:door2up",
            "minerustaddons:door3lower",
            "minerustaddons:door3up",
            "minerustaddons:door4lower",
            "minerustaddons:door4up",
            "minerustaddons:door5lower",
            "minerustaddons:door5up",
            "minerustaddons:door6lower",
            "minerustaddons:door6up",
            "minerustaddons:garage2"
    };

    @Config.Comment("Twig (hay) blocks")
    public static String[] twigBlocks = {
            "minerustaddons:haywall",
            "minerustaddons:haywalllvldestroy1",
            "minerustaddons:haywalllvldestroy2",
            "minerustaddons:haywalllvldestroy3",
            "minerustaddons:haywalllvldestroy4",
            "minerustaddons:hayceiling",
            "minerustaddons:hayceilinglvldestroy1",
            "minerustaddons:hayceilinglvldestroy2",
            "minerustaddons:hayceilinglvldestroy3",
            "minerustaddons:hayceilinglvldestroy4"
    };

    @Config.Comment("Wood blocks")
    public static String[] woodBlocks = {
            "minerustaddons:woodwall",
            "minerustaddons:woodwalllvldestroy1",
            "minerustaddons:woodwalllvldestroy2",
            "minerustaddons:woodwalllvldestroy3",
            "minerustaddons:woodwalllvldestroy4",
            "minerustaddons:woodceiling",
            "minerustaddons:woodceilinglvldestroy1",
            "minerustaddons:woodceilinglvldestroy2",
            "minerustaddons:woodceilinglvldestroy3",
            "minerustaddons:woodceilinglvldestroy4"
    };

    @Config.Comment("Stone blocks")
    public static String[] stoneBlocks = {
            "minerustaddons:stonewall",
            "minerustaddons:stonewalllvldestroy1",
            "minerustaddons:stonewalllvldestroy2",
            "minerustaddons:stonewalllvldestroy3",
            "minerustaddons:stonewalllvldestroy4",
            "minerustaddons:stonesmallwall",
            "minerustaddons:stoneceiling",
            "minerustaddons:stoneceilinglvldestroy1",
            "minerustaddons:stoneceilinglvldestroy2",
            "minerustaddons:stoneceilinglvldestroy3",
            "minerustaddons:stoneceilinglvldestroy4"
    };

    @Config.Comment("Metal blocks")
    public static String[] metalBlocks = {
            "minerustaddons:metalwall",
            "minerustaddons:metalwalllvldestroy1",
            "minerustaddons:metalwalllvldestroy2",
            "minerustaddons:metalwalllvldestroy3",
            "minerustaddons:metalwalllvldestroy4",
            "minerustaddons:metalceiling",
            "minerustaddons:metalceilinglvldestroy1",
            "minerustaddons:metalceilinglvldestroy2",
            "minerustaddons:metalceilinglvldestroy3",
            "minerustaddons:metalceilinglvldestroy4"
    };

    @Config.Comment("Armored (MVK) blocks")
    public static String[] armoredBlocks = {
            "minerustaddons:armoredwall",
            "minerustaddons:armoredwalllvldestroy1",
            "minerustaddons:armoredwalllvldestroy2",
            "minerustaddons:armoredwalllvldestroy3",
            "minerustaddons:armoredwalllvldestroy4",
            "minerustaddons:armoredceiling",
            "minerustaddons:armoredceilinglvldestroy1",
            "minerustaddons:armoredceilinglvldestroy2",
            "minerustaddons:armoredceilinglvldestroy3",
            "minerustaddons:armoredceilinglvldestroy4",
            "minerustaddons:armoredfloor",
            "minerustaddons:armoredfloorlvldestroy1",
            "minerustaddons:armoredfloorlvldestroy2",
            "minerustaddons:armoredfloorlvldestroy3",
            "minerustaddons:armoredfloorlvldestroy4"
    };
}
