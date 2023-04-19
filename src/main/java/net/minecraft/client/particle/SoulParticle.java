package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SoulParticle extends RisingParticle
{
    private final SpriteSet sprites;
    protected boolean isGlowing;

    SoulParticle(ClientLevel pLevel, double pX, double p_107719_, double pY, double p_107721_, double pZ, double p_107723_, SpriteSet pXSpeed)
    {
        super(pLevel, pX, p_107719_, pY, p_107721_, pZ, p_107723_);
        this.sprites = pXSpeed;
        this.scale(1.5F);
        this.setSpriteFromAge(pXSpeed);
    }

    public int getLightColor(float p_234080_)
    {
        return this.isGlowing ? 240 : super.getLightColor(p_234080_);
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void tick()
    {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    public static class EmissiveProvider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public EmissiveProvider(SpriteSet p_234083_)
        {
            this.sprite = p_234083_;
        }

        public Particle createParticle(SimpleParticleType p_234094_, ClientLevel p_234095_, double p_234096_, double p_234097_, double p_234098_, double p_234099_, double p_234100_, double p_234101_)
        {
            SoulParticle soulparticle = new SoulParticle(p_234095_, p_234096_, p_234097_, p_234098_, p_234099_, p_234100_, p_234101_, this.sprite);
            soulparticle.setAlpha(1.0F);
            soulparticle.isGlowing = true;
            return soulparticle;
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites)
        {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_107753_, double pY, double p_107755_, double pZ, double p_107757_)
        {
            SoulParticle soulparticle = new SoulParticle(pLevel, pX, p_107753_, pY, p_107755_, pZ, p_107757_, this.sprite);
            soulparticle.setAlpha(1.0F);
            return soulparticle;
        }
    }
}
