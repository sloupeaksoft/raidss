package com.minerustraid.client;

import com.minerustraid.ModContent;
import com.minerustraid.entity.EntityRocket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/** Renders the 3D rocket item model, pointed along the flight direction. */
public class RenderRocket extends Render<EntityRocket> {

    private ItemStack stack;

    public RenderRocket(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.1F;
    }

    @Override
    public void doRender(EntityRocket entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (stack == null) {
            stack = new ItemStack(ModContent.ROCKET);
        }
        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + entity.height / 2.0F, (float) z);
        GlStateManager.rotate(yaw - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(1.3F, 1.3F, 1.3F);
        GlStateManager.enableRescaleNormal();
        this.bindEntityTexture(entity);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRocket entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
