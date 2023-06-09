package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;

public enum GraphicsStatus implements OptionEnum
{
    FAST(0, "options.graphics.fast"),
    FANCY(1, "options.graphics.fancy"),
    FABULOUS(2, "options.graphics.fabulous");

    private static final GraphicsStatus[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(GraphicsStatus::getId)).toArray((p_90778_) -> {
        return new GraphicsStatus[p_90778_];
    });
    private final int id;
    private final String key;

    private GraphicsStatus(int p_90771_, String p_90772_)
    {
        this.id = p_90771_;
        this.key = p_90772_;
    }

    public int getId()
    {
        return this.id;
    }

    public String getKey()
    {
        return this.key;
    }

    public String toString()
    {
        switch (this)
        {
            case FAST:
                return "fast";

            case FANCY:
                return "fancy";

            case FABULOUS:
                return "fabulous";

            default:
                throw new IllegalArgumentException();
        }
    }

    public static GraphicsStatus byId(int pId)
    {
        return BY_ID[Mth.positiveModulo(pId, BY_ID.length)];
    }
}
