package com.minerustraid.client;

import com.minerustraid.CommonProxy;
import com.minerustraid.entity.EntityRocket;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityRocket.class, RenderRocket::new);
    }
}
