package net.minecraft.world.phys;

import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

public class Vec3 implements Position
{
    public static final Codec<Vec3> CODEC = Codec.DOUBLE.listOf().comapFlatMap((p_231079_) ->
    {
        return Util.fixedSize(p_231079_, 3).map((p_231081_) -> {
            return new Vec3(p_231081_.get(0), p_231081_.get(1), p_231081_.get(2));
        });
    }, (p_231083_) ->
    {
        return List.of(p_231083_.x(), p_231083_.y(), p_231083_.z());
    });
    public static final Vec3 ZERO = new Vec3(0.0D, 0.0D, 0.0D);
    public final double x;
    public final double y;
    public final double z;

    public static Vec3 fromRGB24(int pPacked)
    {
        double d0 = (double)(pPacked >> 16 & 255) / 255.0D;
        double d1 = (double)(pPacked >> 8 & 255) / 255.0D;
        double d2 = (double)(pPacked & 255) / 255.0D;
        return new Vec3(d0, d1, d2);
    }

    public static Vec3 atCenterOf(Vec3i pToCopy)
    {
        return new Vec3((double)pToCopy.getX() + 0.5D, (double)pToCopy.getY() + 0.5D, (double)pToCopy.getZ() + 0.5D);
    }

    public static Vec3 atLowerCornerOf(Vec3i pToCopy)
    {
        return new Vec3((double)pToCopy.getX(), (double)pToCopy.getY(), (double)pToCopy.getZ());
    }

    public static Vec3 atBottomCenterOf(Vec3i pToCopy)
    {
        return new Vec3((double)pToCopy.getX() + 0.5D, (double)pToCopy.getY(), (double)pToCopy.getZ() + 0.5D);
    }

    public static Vec3 upFromBottomCenterOf(Vec3i pToCopy, double pVerticalOffset)
    {
        return new Vec3((double)pToCopy.getX() + 0.5D, (double)pToCopy.getY() + pVerticalOffset, (double)pToCopy.getZ() + 0.5D);
    }

    public Vec3(double pX, double p_82485_, double pY)
    {
        this.x = pX;
        this.y = p_82485_;
        this.z = pY;
    }

    public Vec3(Vector3f pFloatVector)
    {
        this((double)pFloatVector.x(), (double)pFloatVector.y(), (double)pFloatVector.z());
    }

    public Vec3 vectorTo(Vec3 pVec)
    {
        return new Vec3(pVec.x - this.x, pVec.y - this.y, pVec.z - this.z);
    }

    public Vec3 normalize()
    {
        double d0 = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        return d0 < 1.0E-4D ? ZERO : new Vec3(this.x / d0, this.y / d0, this.z / d0);
    }

    public double dot(Vec3 pVec)
    {
        return this.x * pVec.x + this.y * pVec.y + this.z * pVec.z;
    }

    public Vec3 cross(Vec3 pVec)
    {
        return new Vec3(this.y * pVec.z - this.z * pVec.y, this.z * pVec.x - this.x * pVec.z, this.x * pVec.y - this.y * pVec.x);
    }

    public Vec3 subtract(Vec3 pVec)
    {
        return this.subtract(pVec.x, pVec.y, pVec.z);
    }

    public Vec3 subtract(double pX, double p_82494_, double pY)
    {
        return this.add(-pX, -p_82494_, -pY);
    }

    public Vec3 add(Vec3 pVec)
    {
        return this.add(pVec.x, pVec.y, pVec.z);
    }

    public Vec3 add(double pX, double p_82522_, double pY)
    {
        return new Vec3(this.x + pX, this.y + p_82522_, this.z + pY);
    }

    public boolean closerThan(Position pPos, double pDistance)
    {
        return this.distanceToSqr(pPos.x(), pPos.y(), pPos.z()) < pDistance * pDistance;
    }

    public double distanceTo(Vec3 pVec)
    {
        double d0 = pVec.x - this.x;
        double d1 = pVec.y - this.y;
        double d2 = pVec.z - this.z;
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public double distanceToSqr(Vec3 pVec)
    {
        double d0 = pVec.x - this.x;
        double d1 = pVec.y - this.y;
        double d2 = pVec.z - this.z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double distanceToSqr(double pX, double p_82533_, double pY)
    {
        double d0 = pX - this.x;
        double d1 = p_82533_ - this.y;
        double d2 = pY - this.z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public Vec3 scale(double pFactor)
    {
        return this.multiply(pFactor, pFactor, pFactor);
    }

    public Vec3 reverse()
    {
        return this.scale(-1.0D);
    }

    public Vec3 multiply(Vec3 pVec)
    {
        return this.multiply(pVec.x, pVec.y, pVec.z);
    }

    public Vec3 multiply(double pFactorX, double p_82544_, double pFactorY)
    {
        return new Vec3(this.x * pFactorX, this.y * p_82544_, this.z * pFactorY);
    }

    public double length()
    {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double lengthSqr()
    {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double horizontalDistance()
    {
        return Math.sqrt(this.x * this.x + this.z * this.z);
    }

    public double horizontalDistanceSqr()
    {
        return this.x * this.x + this.z * this.z;
    }

    public boolean equals(Object p_82552_)
    {
        if (this == p_82552_)
        {
            return true;
        }
        else if (!(p_82552_ instanceof Vec3))
        {
            return false;
        }
        else
        {
            Vec3 vec3 = (Vec3)p_82552_;

            if (Double.compare(vec3.x, this.x) != 0)
            {
                return false;
            }
            else if (Double.compare(vec3.y, this.y) != 0)
            {
                return false;
            }
            else
            {
                return Double.compare(vec3.z, this.z) == 0;
            }
        }
    }

    public int hashCode()
    {
        long j = Double.doubleToLongBits(this.x);
        int i = (int)(j ^ j >>> 32);
        j = Double.doubleToLongBits(this.y);
        i = 31 * i + (int)(j ^ j >>> 32);
        j = Double.doubleToLongBits(this.z);
        return 31 * i + (int)(j ^ j >>> 32);
    }

    public String toString()
    {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public Vec3 lerp(Vec3 pTo, double pDelta)
    {
        return new Vec3(Mth.lerp(pDelta, this.x, pTo.x), Mth.lerp(pDelta, this.y, pTo.y), Mth.lerp(pDelta, this.z, pTo.z));
    }

    public Vec3 xRot(float pPitch)
    {
        float f = Mth.cos(pPitch);
        float f1 = Mth.sin(pPitch);
        double d0 = this.x;
        double d1 = this.y * (double)f + this.z * (double)f1;
        double d2 = this.z * (double)f - this.y * (double)f1;
        return new Vec3(d0, d1, d2);
    }

    public Vec3 yRot(float pYaw)
    {
        float f = Mth.cos(pYaw);
        float f1 = Mth.sin(pYaw);
        double d0 = this.x * (double)f + this.z * (double)f1;
        double d1 = this.y;
        double d2 = this.z * (double)f - this.x * (double)f1;
        return new Vec3(d0, d1, d2);
    }

    public Vec3 zRot(float pRoll)
    {
        float f = Mth.cos(pRoll);
        float f1 = Mth.sin(pRoll);
        double d0 = this.x * (double)f + this.y * (double)f1;
        double d1 = this.y * (double)f - this.x * (double)f1;
        double d2 = this.z;
        return new Vec3(d0, d1, d2);
    }

    public static Vec3 directionFromRotation(Vec2 pVec)
    {
        return directionFromRotation(pVec.x, pVec.y);
    }

    public static Vec3 directionFromRotation(float pPitch, float pYaw)
    {
        float f = Mth.cos(-pYaw * ((float)Math.PI / 180F) - (float)Math.PI);
        float f1 = Mth.sin(-pYaw * ((float)Math.PI / 180F) - (float)Math.PI);
        float f2 = -Mth.cos(-pPitch * ((float)Math.PI / 180F));
        float f3 = Mth.sin(-pPitch * ((float)Math.PI / 180F));
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

    public Vec3 align(EnumSet<Direction.Axis> pAxes)
    {
        double d0 = pAxes.contains(Direction.Axis.X) ? (double)Mth.floor(this.x) : this.x;
        double d1 = pAxes.contains(Direction.Axis.Y) ? (double)Mth.floor(this.y) : this.y;
        double d2 = pAxes.contains(Direction.Axis.Z) ? (double)Mth.floor(this.z) : this.z;
        return new Vec3(d0, d1, d2);
    }

    public double get(Direction.Axis pAxis)
    {
        return pAxis.choose(this.x, this.y, this.z);
    }

    public Vec3 with(Direction.Axis p_193104_, double p_193105_)
    {
        double d0 = p_193104_ == Direction.Axis.X ? p_193105_ : this.x;
        double d1 = p_193104_ == Direction.Axis.Y ? p_193105_ : this.y;
        double d2 = p_193104_ == Direction.Axis.Z ? p_193105_ : this.z;
        return new Vec3(d0, d1, d2);
    }

    public Vec3 relative(Direction p_231076_, double p_231077_)
    {
        Vec3i vec3i = p_231076_.getNormal();
        return new Vec3(this.x + p_231077_ * (double)vec3i.getX(), this.y + p_231077_ * (double)vec3i.getY(), this.z + p_231077_ * (double)vec3i.getZ());
    }

    public final double x()
    {
        return this.x;
    }

    public final double y()
    {
        return this.y;
    }

    public final double z()
    {
        return this.z;
    }
}
