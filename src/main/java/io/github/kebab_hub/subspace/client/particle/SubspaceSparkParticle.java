package io.github.kebab_hub.subspace.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;

/** Reference sprite particle for the visual pipeline. Nothing spawns it automatically. */
public final class SubspaceSparkParticle extends TextureSheetParticle {
    private static final int LIFETIME_TICKS = 16;

    private SubspaceSparkParticle(ClientLevel level, double x, double y, double z,
                                  double velocityX, double velocityY, double velocityZ, SpriteSet sprites) {
        super(level, x, y, z);
        xd = velocityX;
        yd = velocityY;
        zd = velocityZ;
        lifetime = LIFETIME_TICKS;
        quadSize = 0.08F;
        friction = 0.9F;
        hasPhysics = false;
        pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        alpha = Math.max(0.0F, 1.0F - (float) age / lifetime);
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new SubspaceSparkParticle(level, x, y, z, velocityX, velocityY, velocityZ, sprites);
        }
    }
}
