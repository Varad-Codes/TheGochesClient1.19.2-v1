package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;

public enum ParticleStatus implements OptionEnum
{
    ALL(0, "options.particles.all"),
    DECREASED(1, "options.particles.decreased"),
    MINIMAL(2, "options.particles.minimal");

    private static final ParticleStatus[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(ParticleStatus::getId)).toArray((p_92200_) -> {
        return new ParticleStatus[p_92200_];
    });
    private final int id;
    private final String key;

    private ParticleStatus(int p_92193_, String p_92194_)
    {
        this.id = p_92193_;
        this.key = p_92194_;
    }

    public String getKey()
    {
        return this.key;
    }

    public int getId()
    {
        return this.id;
    }

    public static ParticleStatus byId(int pId)
    {
        return BY_ID[Mth.positiveModulo(pId, BY_ID.length)];
    }
}
