package com.minerustraid;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Sounds taken from MineRust Addons (C4 / satchel). */
@Mod.EventBusSubscriber(modid = MineRustRaid.MODID)
public final class ModSounds {

    public static SoundEvent C4_PLACE;
    public static SoundEvent C4_BEEP;
    public static SoundEvent C4_EXPLODE;
    public static SoundEvent SATCHEL_PLACE;
    public static SoundEvent SATCHEL_FUSE;
    public static SoundEvent SATCHEL_EXPLODE;
    public static SoundEvent STONE_DESTROY;

    private ModSounds() {
    }

    private static SoundEvent make(String name) {
        ResourceLocation rl = new ResourceLocation(MineRustRaid.MODID, name);
        SoundEvent e = new SoundEvent(rl);
        e.setRegistryName(rl);
        return e;
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        C4_PLACE = make("c4_place");
        C4_BEEP = make("c4_beep");
        C4_EXPLODE = make("c4_explode");
        SATCHEL_PLACE = make("satchel_place");
        SATCHEL_FUSE = make("satchel_fuse");
        SATCHEL_EXPLODE = make("satchel_explode");
        STONE_DESTROY = make("stone_destroy");
        event.getRegistry().registerAll(C4_PLACE, C4_BEEP, C4_EXPLODE,
                SATCHEL_PLACE, SATCHEL_FUSE, SATCHEL_EXPLODE, STONE_DESTROY);
    }
}
