package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;

public enum AttackIndicatorStatus implements OptionEnum
{
    OFF(0, "options.off"),
    CROSSHAIR(1, "options.attack.crosshair"),
    HOTBAR(2, "options.attack.hotbar");

    private static final AttackIndicatorStatus[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(AttackIndicatorStatus::getId)).toArray((p_90513_) -> {
        return new AttackIndicatorStatus[p_90513_];
    });
    private final int id;
    private final String key;

    private AttackIndicatorStatus(int p_90506_, String p_90507_)
    {
        this.id = p_90506_;
        this.key = p_90507_;
    }

    public int getId()
    {
        return this.id;
    }

    public String getKey()
    {
        return this.key;
    }

    public static AttackIndicatorStatus byId(int pId)
    {
        return BY_ID[Mth.positiveModulo(pId, BY_ID.length)];
    }
}
