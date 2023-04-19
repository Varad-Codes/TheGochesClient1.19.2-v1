package net.minecraft.client.resources.sounds;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class SimpleSoundInstance extends AbstractSoundInstance
{
    public SimpleSoundInstance(SoundEvent p_235109_, SoundSource p_235110_, float p_235111_, float p_235112_, RandomSource p_235113_, BlockPos p_235114_)
    {
        this(p_235109_, p_235110_, p_235111_, p_235112_, p_235113_, (double)p_235114_.getX() + 0.5D, (double)p_235114_.getY() + 0.5D, (double)p_235114_.getZ() + 0.5D);
    }

    public static SimpleSoundInstance forUI(SoundEvent pSound, float pPitch)
    {
        return forUI(pSound, pPitch, 0.25F);
    }

    public static SimpleSoundInstance forUI(SoundEvent pSound, float pPitch, float pVolume)
    {
        return new SimpleSoundInstance(pSound.getLocation(), SoundSource.MASTER, pVolume, pPitch, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true);
    }

    public static SimpleSoundInstance forMusic(SoundEvent pSound)
    {
        return new SimpleSoundInstance(pSound.getLocation(), SoundSource.MUSIC, 1.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true);
    }

    public static SimpleSoundInstance forRecord(SoundEvent pSound, double pX, double p_119750_, double pY)
    {
        return new SimpleSoundInstance(pSound, SoundSource.RECORDS, 4.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.LINEAR, pX, p_119750_, pY);
    }

    public static SimpleSoundInstance forLocalAmbience(SoundEvent pSound, float pVolume, float pPitch)
    {
        return new SimpleSoundInstance(pSound.getLocation(), SoundSource.AMBIENT, pPitch, pVolume, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true);
    }

    public static SimpleSoundInstance forAmbientAddition(SoundEvent pSound)
    {
        return forLocalAmbience(pSound, 1.0F, 1.0F);
    }

    public static SimpleSoundInstance forAmbientMood(SoundEvent p_235128_, RandomSource p_235129_, double p_235130_, double p_235131_, double p_235132_)
    {
        return new SimpleSoundInstance(p_235128_, SoundSource.AMBIENT, 1.0F, 1.0F, p_235129_, false, 0, SoundInstance.Attenuation.LINEAR, p_235130_, p_235131_, p_235132_);
    }

    public SimpleSoundInstance(SoundEvent p_235100_, SoundSource p_235101_, float p_235102_, float p_235103_, RandomSource p_235104_, double p_235105_, double p_235106_, double p_235107_)
    {
        this(p_235100_, p_235101_, p_235102_, p_235103_, p_235104_, false, 0, SoundInstance.Attenuation.LINEAR, p_235105_, p_235106_, p_235107_);
    }

    private SimpleSoundInstance(SoundEvent pLocation, SoundSource pSource, float pVolume, float pPitch, RandomSource pLooping, boolean pDelay, int pAttenuation, SoundInstance.Attenuation pX, double p_235124_, double pY, double p_235126_)
    {
        this(pLocation.getLocation(), pSource, pVolume, pPitch, pLooping, pDelay, pAttenuation, pX, p_235124_, pY, p_235126_, false);
    }

    public SimpleSoundInstance(ResourceLocation p_235087_, SoundSource p_235088_, float p_235089_, float p_235090_, RandomSource p_235091_, boolean p_235092_, int p_235093_, SoundInstance.Attenuation p_235094_, double p_235095_, double p_235096_, double p_235097_, boolean p_235098_)
    {
        super(p_235087_, p_235088_, p_235091_);
        this.volume = p_235089_;
        this.pitch = p_235090_;
        this.x = p_235095_;
        this.y = p_235096_;
        this.z = p_235097_;
        this.looping = p_235092_;
        this.delay = p_235093_;
        this.attenuation = p_235094_;
        this.relative = p_235098_;
    }
}
