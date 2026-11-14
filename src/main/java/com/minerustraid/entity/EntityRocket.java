package com.minerustraid.entity;

import javax.annotation.Nullable;

import com.minerustraid.RaidConfig;
import com.minerustraid.RaidLogic;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityRocket extends EntityThrowable {

    public EntityRocket(World world) {
        super(world);
        setSize(0.25F, 0.25F);
    }

    public EntityRocket(World world, EntityLivingBase shooter) {
        super(world, shooter);
        setSize(0.25F, 0.25F);
    }

    @Override
    protected float getGravityVelocity() {
        return (float) RaidConfig.rocketGravity;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) {
            for (int i = 0; i < 3; i++) {
                double f = i / 3.0D;
                double px = this.prevPosX + (this.posX - this.prevPosX) * f;
                double py = this.prevPosY + (this.posY - this.prevPosY) * f + this.height / 2.0D;
                double pz = this.prevPosZ + (this.posZ - this.prevPosZ) * f;
                this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, px, py, pz, 0.0D, 0.01D, 0.0D);
            }
            this.world.spawnParticle(EnumParticleTypes.FLAME, this.prevPosX, this.prevPosY + this.height / 2.0D, this.prevPosZ, 0.0D, 0.0D, 0.0D);
        } else if (!this.isDead && this.ticksExisted > RaidConfig.rocketLifetimeTicks) {
            explode(null);
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (this.world.isRemote || this.isDead) {
            return;
        }
        if (result.typeOfHit == RayTraceResult.Type.ENTITY && result.entityHit != null) {
            if (result.entityHit == getThrower() && this.ticksExisted < 5) {
                return;
            }
            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), (float) RaidConfig.rocketDirectDamage);
        }
        explode(result);
    }

    private void explode(@Nullable RayTraceResult result) {
        double x = this.posX;
        double y = this.posY;
        double z = this.posZ;
        if (result != null && result.hitVec != null) {
            x = result.hitVec.x;
            y = result.hitVec.y;
            z = result.hitVec.z;
        }
        BlockPos center;
        if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.getBlockPos() != null) {
            center = result.getBlockPos();
        } else {
            center = new BlockPos(x, y, z);
        }
        // isSmoking = false: no vanilla terrain damage, raid damage is applied below
        this.world.newExplosion(this, x, y, z, (float) RaidConfig.rocketExplosionPower, false, false);
        // 3x3 area
        RaidLogic.applyArea(this.world, center, 1, RaidConfig.rocketDamage);
        setDead();
    }
}
