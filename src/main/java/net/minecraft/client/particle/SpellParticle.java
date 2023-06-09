package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class SpellParticle extends TextureSheetParticle
{
    private static final RandomSource RANDOM = RandomSource.create();
    private final SpriteSet sprites;

    SpellParticle(ClientLevel pLevel, double pX, double p_107764_, double pY, double p_107766_, double pZ, double p_107768_, SpriteSet pXSpeed)
    {
        super(pLevel, pX, p_107764_, pY, 0.5D - RANDOM.nextDouble(), pZ, 0.5D - RANDOM.nextDouble());
        this.friction = 0.96F;
        this.gravity = -0.1F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = pXSpeed;
        this.yd *= (double)0.2F;

        if (p_107766_ == 0.0D && p_107768_ == 0.0D)
        {
            this.xd *= (double)0.1F;
            this.zd *= (double)0.1F;
        }

        this.quadSize *= 0.75F;
        this.lifetime = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
        this.hasPhysics = false;
        this.setSpriteFromAge(pXSpeed);

        if (this.isCloseToScopingPlayer())
        {
            this.setAlpha(0.0F);
        }
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void tick()
    {
        super.tick();
        this.setSpriteFromAge(this.sprites);

        if (this.isCloseToScopingPlayer())
        {
            this.setAlpha(0.0F);
        }
        else
        {
            this.setAlpha(Mth.lerp(0.05F, this.alpha, 1.0F));
        }
    }

    private boolean isCloseToScopingPlayer()
    {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localplayer = minecraft.player;
        return localplayer != null && localplayer.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 9.0D && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping();
    }

    public static class AmbientMobProvider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public AmbientMobProvider(SpriteSet pSprites)
        {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_107798_, double pY, double p_107800_, double pZ, double p_107802_)
        {
            Particle particle = new SpellParticle(pLevel, pX, p_107798_, pY, p_107800_, pZ, p_107802_, this.sprite);
            particle.setAlpha(0.15F);
            particle.setColor((float)p_107800_, (float)pZ, (float)p_107802_);
            return particle;
        }
    }

    public static class InstantProvider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public InstantProvider(SpriteSet pSprites)
        {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_107819_, double pY, double p_107821_, double pZ, double p_107823_)
        {
            return new SpellParticle(pLevel, pX, p_107819_, pY, p_107821_, pZ, p_107823_, this.sprite);
        }
    }

    public static class MobProvider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public MobProvider(SpriteSet pSprites)
        {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_107840_, double pY, double p_107842_, double pZ, double p_107844_)
        {
            Particle particle = new SpellParticle(pLevel, pX, p_107840_, pY, p_107842_, pZ, p_107844_, this.sprite);
            particle.setColor((float)p_107842_, (float)pZ, (float)p_107844_);
            return particle;
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites)
        {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_107861_, double pY, double p_107863_, double pZ, double p_107865_)
        {
            return new SpellParticle(pLevel, pX, p_107861_, pY, p_107863_, pZ, p_107865_, this.sprite);
        }
    }

    public static class WitchProvider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprite;

        public WitchProvider(SpriteSet pSprites)
        {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_107882_, double pY, double p_107884_, double pZ, double p_107886_)
        {
            SpellParticle spellparticle = new SpellParticle(pLevel, pX, p_107882_, pY, p_107884_, pZ, p_107886_, this.sprite);
            float f = pLevel.random.nextFloat() * 0.5F + 0.35F;
            spellparticle.setColor(1.0F * f, 0.0F * f, 1.0F * f);
            return spellparticle;
        }
    }
}
