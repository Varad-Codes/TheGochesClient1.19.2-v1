package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum Mirror implements StringRepresentable
{
    NONE("none", OctahedralGroup.IDENTITY),
    LEFT_RIGHT("left_right", OctahedralGroup.INVERT_Z),
    FRONT_BACK("front_back", OctahedralGroup.INVERT_X);

    public static final Codec<Mirror> CODEC = StringRepresentable.fromEnum(Mirror::values);
    private final String id;
    private final Component symbol;
    private final OctahedralGroup rotation;

    private Mirror(String p_221529_, OctahedralGroup p_221530_)
    {
        this.id = p_221529_;
        this.symbol = Component.translatable("mirror." + p_221529_);
        this.rotation = p_221530_;
    }

    public int mirror(int pRotation, int pRotationCount)
    {
        int i = pRotationCount / 2;
        int j = pRotation > i ? pRotation - pRotationCount : pRotation;

        switch (this)
        {
            case FRONT_BACK:
                return (pRotationCount - j) % pRotationCount;

            case LEFT_RIGHT:
                return (i - j + pRotationCount) % pRotationCount;

            default:
                return pRotation;
        }
    }

    public Rotation getRotation(Direction pFacing)
    {
        Direction.Axis direction$axis = pFacing.getAxis();
        return (this != LEFT_RIGHT || direction$axis != Direction.Axis.Z) && (this != FRONT_BACK || direction$axis != Direction.Axis.X) ? Rotation.NONE : Rotation.CLOCKWISE_180;
    }

    public Direction mirror(Direction pFacing)
    {
        if (this == FRONT_BACK && pFacing.getAxis() == Direction.Axis.X)
        {
            return pFacing.getOpposite();
        }
        else
        {
            return this == LEFT_RIGHT && pFacing.getAxis() == Direction.Axis.Z ? pFacing.getOpposite() : pFacing;
        }
    }

    public OctahedralGroup rotation()
    {
        return this.rotation;
    }

    public Component symbol()
    {
        return this.symbol;
    }

    public String getSerializedName()
    {
        return this.id;
    }
}
