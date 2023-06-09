package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

public class MobEffectInstance implements Comparable<MobEffectInstance>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MobEffect effect;
    int duration;
    private int amplifier;
    private boolean ambient;
    private boolean noCounter;
    private boolean visible;
    private boolean showIcon;
    @Nullable
    private MobEffectInstance hiddenEffect;
    private Optional<MobEffectInstance.FactorData> factorData;

    public MobEffectInstance(MobEffect p_19513_)
    {
        this(p_19513_, 0, 0);
    }

    public MobEffectInstance(MobEffect p_19515_, int p_19516_)
    {
        this(p_19515_, p_19516_, 0);
    }

    public MobEffectInstance(MobEffect p_19518_, int p_19519_, int p_19520_)
    {
        this(p_19518_, p_19519_, p_19520_, false, true);
    }

    public MobEffectInstance(MobEffect p_19522_, int p_19523_, int p_19524_, boolean p_19525_, boolean p_19526_)
    {
        this(p_19522_, p_19523_, p_19524_, p_19525_, p_19526_, p_19526_);
    }

    public MobEffectInstance(MobEffect p_19528_, int p_19529_, int p_19530_, boolean p_19531_, boolean p_19532_, boolean p_19533_)
    {
        this(p_19528_, p_19529_, p_19530_, p_19531_, p_19532_, p_19533_, (MobEffectInstance)null, p_19528_.createFactorData());
    }

    public MobEffectInstance(MobEffect p_216887_, int p_216888_, int p_216889_, boolean p_216890_, boolean p_216891_, boolean p_216892_, @Nullable MobEffectInstance p_216893_, Optional<MobEffectInstance.FactorData> p_216894_)
    {
        this.effect = p_216887_;
        this.duration = p_216888_;
        this.amplifier = p_216889_;
        this.ambient = p_216890_;
        this.visible = p_216891_;
        this.showIcon = p_216892_;
        this.hiddenEffect = p_216893_;
        this.factorData = p_216894_;
    }

    public MobEffectInstance(MobEffectInstance p_19543_)
    {
        this.effect = p_19543_.effect;
        this.factorData = this.effect.createFactorData();
        this.setDetailsFrom(p_19543_);
    }

    public Optional<MobEffectInstance.FactorData> getFactorData()
    {
        return this.factorData;
    }

    void setDetailsFrom(MobEffectInstance p_19549_)
    {
        this.duration = p_19549_.duration;
        this.amplifier = p_19549_.amplifier;
        this.ambient = p_19549_.ambient;
        this.visible = p_19549_.visible;
        this.showIcon = p_19549_.showIcon;
    }

    public boolean update(MobEffectInstance pOther)
    {
        if (this.effect != pOther.effect)
        {
            LOGGER.warn("This method should only be called for matching effects!");
        }

        int i = this.duration;
        boolean flag = false;

        if (pOther.amplifier > this.amplifier)
        {
            if (pOther.duration < this.duration)
            {
                MobEffectInstance mobeffectinstance = this.hiddenEffect;
                this.hiddenEffect = new MobEffectInstance(this);
                this.hiddenEffect.hiddenEffect = mobeffectinstance;
            }

            this.amplifier = pOther.amplifier;
            this.duration = pOther.duration;
            flag = true;
        }
        else if (pOther.duration > this.duration)
        {
            if (pOther.amplifier == this.amplifier)
            {
                this.duration = pOther.duration;
                flag = true;
            }
            else if (this.hiddenEffect == null)
            {
                this.hiddenEffect = new MobEffectInstance(pOther);
            }
            else
            {
                this.hiddenEffect.update(pOther);
            }
        }

        if (!pOther.ambient && this.ambient || flag)
        {
            this.ambient = pOther.ambient;
            flag = true;
        }

        if (pOther.visible != this.visible)
        {
            this.visible = pOther.visible;
            flag = true;
        }

        if (pOther.showIcon != this.showIcon)
        {
            this.showIcon = pOther.showIcon;
            flag = true;
        }

        if (i != this.duration)
        {
            this.factorData.ifPresent((p_216898_) ->
            {
                p_216898_.effectChangedTimestamp += this.duration - i;
            });
            flag = true;
        }

        return flag;
    }

    public MobEffect getEffect()
    {
        return this.effect;
    }

    public int getDuration()
    {
        return this.duration;
    }

    public int getAmplifier()
    {
        return this.amplifier;
    }

    public boolean isAmbient()
    {
        return this.ambient;
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    public boolean showIcon()
    {
        return this.showIcon;
    }

    public boolean tick(LivingEntity p_19553_, Runnable p_19554_)
    {
        if (this.duration > 0)
        {
            if (this.effect.isDurationEffectTick(this.duration, this.amplifier))
            {
                this.applyEffect(p_19553_);
            }

            this.tickDownDuration();

            if (this.duration == 0 && this.hiddenEffect != null)
            {
                this.setDetailsFrom(this.hiddenEffect);
                this.hiddenEffect = this.hiddenEffect.hiddenEffect;
                p_19554_.run();
            }
        }

        this.factorData.ifPresent((p_216900_) ->
        {
            p_216900_.update(this);
        });
        return this.duration > 0;
    }

    private int tickDownDuration()
    {
        if (this.hiddenEffect != null)
        {
            this.hiddenEffect.tickDownDuration();
        }

        return --this.duration;
    }

    public void applyEffect(LivingEntity pEntity)
    {
        if (this.duration > 0)
        {
            this.effect.applyEffectTick(pEntity, this.amplifier);
        }
    }

    public String getDescriptionId()
    {
        return this.effect.getDescriptionId();
    }

    public String toString()
    {
        String s;

        if (this.amplifier > 0)
        {
            s = this.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.duration;
        }
        else
        {
            s = this.getDescriptionId() + ", Duration: " + this.duration;
        }

        if (!this.visible)
        {
            s = s + ", Particles: false";
        }

        if (!this.showIcon)
        {
            s = s + ", Show Icon: false";
        }

        return s;
    }

    public boolean equals(Object p_19574_)
    {
        if (this == p_19574_)
        {
            return true;
        }
        else if (!(p_19574_ instanceof MobEffectInstance))
        {
            return false;
        }
        else
        {
            MobEffectInstance mobeffectinstance = (MobEffectInstance)p_19574_;
            return this.duration == mobeffectinstance.duration && this.amplifier == mobeffectinstance.amplifier && this.ambient == mobeffectinstance.ambient && this.effect.equals(mobeffectinstance.effect);
        }
    }

    public int hashCode()
    {
        int i = this.effect.hashCode();
        i = 31 * i + this.duration;
        i = 31 * i + this.amplifier;
        return 31 * i + (this.ambient ? 1 : 0);
    }

    public CompoundTag save(CompoundTag pNbt)
    {
        pNbt.putInt("Id", MobEffect.getId(this.getEffect()));
        this.writeDetailsTo(pNbt);
        return pNbt;
    }

    private void writeDetailsTo(CompoundTag pNbt)
    {
        pNbt.putByte("Amplifier", (byte)this.getAmplifier());
        pNbt.putInt("Duration", this.getDuration());
        pNbt.putBoolean("Ambient", this.isAmbient());
        pNbt.putBoolean("ShowParticles", this.isVisible());
        pNbt.putBoolean("ShowIcon", this.showIcon());

        if (this.hiddenEffect != null)
        {
            CompoundTag compoundtag = new CompoundTag();
            this.hiddenEffect.save(compoundtag);
            pNbt.put("HiddenEffect", compoundtag);
        }

        this.factorData.ifPresent((p_216903_) ->
        {
            MobEffectInstance.FactorData.CODEC.encodeStart(NbtOps.INSTANCE, p_216903_).resultOrPartial(LOGGER::error).ifPresent((p_216906_) -> {
                pNbt.put("FactorCalculationData", p_216906_);
            });
        });
    }

    @Nullable
    public static MobEffectInstance load(CompoundTag pNbt)
    {
        int i = pNbt.getInt("Id");
        MobEffect mobeffect = MobEffect.byId(i);
        return mobeffect == null ? null : loadSpecifiedEffect(mobeffect, pNbt);
    }

    private static MobEffectInstance loadSpecifiedEffect(MobEffect pEffect, CompoundTag pNbt)
    {
        int i = pNbt.getByte("Amplifier");
        int j = pNbt.getInt("Duration");
        boolean flag = pNbt.getBoolean("Ambient");
        boolean flag1 = true;

        if (pNbt.contains("ShowParticles", 1))
        {
            flag1 = pNbt.getBoolean("ShowParticles");
        }

        boolean flag2 = flag1;

        if (pNbt.contains("ShowIcon", 1))
        {
            flag2 = pNbt.getBoolean("ShowIcon");
        }

        MobEffectInstance mobeffectinstance = null;

        if (pNbt.contains("HiddenEffect", 10))
        {
            mobeffectinstance = loadSpecifiedEffect(pEffect, pNbt.getCompound("HiddenEffect"));
        }

        Optional<MobEffectInstance.FactorData> optional;

        if (pNbt.contains("FactorCalculationData", 10))
        {
            optional = MobEffectInstance.FactorData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, pNbt.getCompound("FactorCalculationData"))).resultOrPartial(LOGGER::error);
        }
        else
        {
            optional = Optional.empty();
        }

        return new MobEffectInstance(pEffect, j, Math.max(i, 0), flag, flag1, flag2, mobeffectinstance, optional);
    }

    public void setNoCounter(boolean pMaxDuration)
    {
        this.noCounter = pMaxDuration;
    }

    public boolean isNoCounter()
    {
        return this.noCounter;
    }

    public int compareTo(MobEffectInstance p_19566_)
    {
        int i = 32147;
        return (this.getDuration() <= 32147 || p_19566_.getDuration() <= 32147) && (!this.isAmbient() || !p_19566_.isAmbient()) ? ComparisonChain.start().compare(this.isAmbient(), p_19566_.isAmbient()).compare(this.getDuration(), p_19566_.getDuration()).compare(this.getEffect().getColor(), p_19566_.getEffect().getColor()).result() : ComparisonChain.start().compare(this.isAmbient(), p_19566_.isAmbient()).compare(this.getEffect().getColor(), p_19566_.getEffect().getColor()).result();
    }

    public static class FactorData
    {
        public static final Codec<MobEffectInstance.FactorData> CODEC = RecordCodecBuilder.create((p_216933_) ->
        {
            return p_216933_.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("padding_duration").forGetter((p_216945_) -> {
                return p_216945_.paddingDuration;
            }), Codec.FLOAT.fieldOf("factor_start").orElse(0.0F).forGetter((p_216943_) -> {
                return p_216943_.factorStart;
            }), Codec.FLOAT.fieldOf("factor_target").orElse(1.0F).forGetter((p_216941_) -> {
                return p_216941_.factorTarget;
            }), Codec.FLOAT.fieldOf("factor_current").orElse(0.0F).forGetter((p_216939_) -> {
                return p_216939_.factorCurrent;
            }), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("effect_changed_timestamp").orElse(0).forGetter((p_216937_) -> {
                return p_216937_.effectChangedTimestamp;
            }), Codec.FLOAT.fieldOf("factor_previous_frame").orElse(0.0F).forGetter((p_216935_) -> {
                return p_216935_.factorPreviousFrame;
            }), Codec.BOOL.fieldOf("had_effect_last_tick").orElse(false).forGetter((p_216929_) -> {
                return p_216929_.hadEffectLastTick;
            })).apply(p_216933_, MobEffectInstance.FactorData::new);
        });
        private final int paddingDuration;
        private float factorStart;
        private float factorTarget;
        private float factorCurrent;
        int effectChangedTimestamp;
        private float factorPreviousFrame;
        private boolean hadEffectLastTick;

        public FactorData(int p_216919_, float p_216920_, float p_216921_, float p_216922_, int p_216923_, float p_216924_, boolean p_216925_)
        {
            this.paddingDuration = p_216919_;
            this.factorStart = p_216920_;
            this.factorTarget = p_216921_;
            this.factorCurrent = p_216922_;
            this.effectChangedTimestamp = p_216923_;
            this.factorPreviousFrame = p_216924_;
            this.hadEffectLastTick = p_216925_;
        }

        public FactorData(int p_216917_)
        {
            this(p_216917_, 0.0F, 1.0F, 0.0F, 0, 0.0F, false);
        }

        public void update(MobEffectInstance p_216931_)
        {
            this.factorPreviousFrame = this.factorCurrent;
            boolean flag = p_216931_.duration > this.paddingDuration;

            if (this.hadEffectLastTick != flag)
            {
                this.hadEffectLastTick = flag;
                this.effectChangedTimestamp = p_216931_.duration;
                this.factorStart = this.factorCurrent;
                this.factorTarget = flag ? 1.0F : 0.0F;
            }

            float f = Mth.clamp(((float)this.effectChangedTimestamp - (float)p_216931_.duration) / (float)this.paddingDuration, 0.0F, 1.0F);
            this.factorCurrent = Mth.lerp(f, this.factorStart, this.factorTarget);
        }

        public float getFactor(LivingEntity p_238414_, float p_238415_)
        {
            if (p_238414_.isRemoved())
            {
                this.factorPreviousFrame = this.factorCurrent;
            }

            return Mth.lerp(p_238415_, this.factorPreviousFrame, this.factorCurrent);
        }
    }
}
