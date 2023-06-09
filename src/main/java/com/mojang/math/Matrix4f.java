package com.mojang.math;

import java.nio.FloatBuffer;
import java.util.Random;

public final class Matrix4f
{
    private static final int ORDER = 4;
    protected float m00;
    protected float m01;
    protected float m02;
    protected float m03;
    protected float m10;
    protected float m11;
    protected float m12;
    protected float m13;
    protected float m20;
    protected float m21;
    protected float m22;
    protected float m23;
    protected float m30;
    protected float m31;
    protected float m32;
    protected float m33;

    public Matrix4f()
    {
    }

    public Matrix4f(Matrix4f pOther)
    {
        this.m00 = pOther.m00;
        this.m01 = pOther.m01;
        this.m02 = pOther.m02;
        this.m03 = pOther.m03;
        this.m10 = pOther.m10;
        this.m11 = pOther.m11;
        this.m12 = pOther.m12;
        this.m13 = pOther.m13;
        this.m20 = pOther.m20;
        this.m21 = pOther.m21;
        this.m22 = pOther.m22;
        this.m23 = pOther.m23;
        this.m30 = pOther.m30;
        this.m31 = pOther.m31;
        this.m32 = pOther.m32;
        this.m33 = pOther.m33;
    }

    public Matrix4f(Quaternion pOther)
    {
        float f = pOther.i();
        float f1 = pOther.j();
        float f2 = pOther.k();
        float f3 = pOther.r();
        float f4 = 2.0F * f * f;
        float f5 = 2.0F * f1 * f1;
        float f6 = 2.0F * f2 * f2;
        this.m00 = 1.0F - f5 - f6;
        this.m11 = 1.0F - f6 - f4;
        this.m22 = 1.0F - f4 - f5;
        this.m33 = 1.0F;
        float f7 = f * f1;
        float f8 = f1 * f2;
        float f9 = f2 * f;
        float f10 = f * f3;
        float f11 = f1 * f3;
        float f12 = f2 * f3;
        this.m10 = 2.0F * (f7 + f12);
        this.m01 = 2.0F * (f7 - f12);
        this.m20 = 2.0F * (f9 - f11);
        this.m02 = 2.0F * (f9 + f11);
        this.m21 = 2.0F * (f8 + f10);
        this.m12 = 2.0F * (f8 - f10);
    }

    public boolean isInteger()
    {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.m30 = 1.0F;
        matrix4f.m31 = 1.0F;
        matrix4f.m32 = 1.0F;
        matrix4f.m33 = 0.0F;
        Matrix4f matrix4f1 = this.copy();
        matrix4f1.multiply(matrix4f);
        return isInteger(matrix4f1.m00 / matrix4f1.m03) && isInteger(matrix4f1.m10 / matrix4f1.m13) && isInteger(matrix4f1.m20 / matrix4f1.m23) && isInteger(matrix4f1.m01 / matrix4f1.m03) && isInteger(matrix4f1.m11 / matrix4f1.m13) && isInteger(matrix4f1.m21 / matrix4f1.m23) && isInteger(matrix4f1.m02 / matrix4f1.m03) && isInteger(matrix4f1.m12 / matrix4f1.m13) && isInteger(matrix4f1.m22 / matrix4f1.m23);
    }

    private static boolean isInteger(float pValue)
    {
        return (double)Math.abs(pValue - (float)Math.round(pValue)) <= 1.0E-5D;
    }

    public boolean equals(Object pOther)
    {
        if (this == pOther)
        {
            return true;
        }
        else if (pOther != null && this.getClass() == pOther.getClass())
        {
            Matrix4f matrix4f = (Matrix4f)pOther;
            return Float.compare(matrix4f.m00, this.m00) == 0 && Float.compare(matrix4f.m01, this.m01) == 0 && Float.compare(matrix4f.m02, this.m02) == 0 && Float.compare(matrix4f.m03, this.m03) == 0 && Float.compare(matrix4f.m10, this.m10) == 0 && Float.compare(matrix4f.m11, this.m11) == 0 && Float.compare(matrix4f.m12, this.m12) == 0 && Float.compare(matrix4f.m13, this.m13) == 0 && Float.compare(matrix4f.m20, this.m20) == 0 && Float.compare(matrix4f.m21, this.m21) == 0 && Float.compare(matrix4f.m22, this.m22) == 0 && Float.compare(matrix4f.m23, this.m23) == 0 && Float.compare(matrix4f.m30, this.m30) == 0 && Float.compare(matrix4f.m31, this.m31) == 0 && Float.compare(matrix4f.m32, this.m32) == 0 && Float.compare(matrix4f.m33, this.m33) == 0;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        int i = this.m00 != 0.0F ? Float.floatToIntBits(this.m00) : 0;
        i = 31 * i + (this.m01 != 0.0F ? Float.floatToIntBits(this.m01) : 0);
        i = 31 * i + (this.m02 != 0.0F ? Float.floatToIntBits(this.m02) : 0);
        i = 31 * i + (this.m03 != 0.0F ? Float.floatToIntBits(this.m03) : 0);
        i = 31 * i + (this.m10 != 0.0F ? Float.floatToIntBits(this.m10) : 0);
        i = 31 * i + (this.m11 != 0.0F ? Float.floatToIntBits(this.m11) : 0);
        i = 31 * i + (this.m12 != 0.0F ? Float.floatToIntBits(this.m12) : 0);
        i = 31 * i + (this.m13 != 0.0F ? Float.floatToIntBits(this.m13) : 0);
        i = 31 * i + (this.m20 != 0.0F ? Float.floatToIntBits(this.m20) : 0);
        i = 31 * i + (this.m21 != 0.0F ? Float.floatToIntBits(this.m21) : 0);
        i = 31 * i + (this.m22 != 0.0F ? Float.floatToIntBits(this.m22) : 0);
        i = 31 * i + (this.m23 != 0.0F ? Float.floatToIntBits(this.m23) : 0);
        i = 31 * i + (this.m30 != 0.0F ? Float.floatToIntBits(this.m30) : 0);
        i = 31 * i + (this.m31 != 0.0F ? Float.floatToIntBits(this.m31) : 0);
        i = 31 * i + (this.m32 != 0.0F ? Float.floatToIntBits(this.m32) : 0);
        return 31 * i + (this.m33 != 0.0F ? Float.floatToIntBits(this.m33) : 0);
    }

    private static int bufferIndex(int pX, int pY)
    {
        return pY * 4 + pX;
    }

    public void load(FloatBuffer pOther)
    {
        this.m00 = pOther.get(bufferIndex(0, 0));
        this.m01 = pOther.get(bufferIndex(0, 1));
        this.m02 = pOther.get(bufferIndex(0, 2));
        this.m03 = pOther.get(bufferIndex(0, 3));
        this.m10 = pOther.get(bufferIndex(1, 0));
        this.m11 = pOther.get(bufferIndex(1, 1));
        this.m12 = pOther.get(bufferIndex(1, 2));
        this.m13 = pOther.get(bufferIndex(1, 3));
        this.m20 = pOther.get(bufferIndex(2, 0));
        this.m21 = pOther.get(bufferIndex(2, 1));
        this.m22 = pOther.get(bufferIndex(2, 2));
        this.m23 = pOther.get(bufferIndex(2, 3));
        this.m30 = pOther.get(bufferIndex(3, 0));
        this.m31 = pOther.get(bufferIndex(3, 1));
        this.m32 = pOther.get(bufferIndex(3, 2));
        this.m33 = pOther.get(bufferIndex(3, 3));
    }

    public void loadTransposed(FloatBuffer pBuffer)
    {
        this.m00 = pBuffer.get(bufferIndex(0, 0));
        this.m01 = pBuffer.get(bufferIndex(1, 0));
        this.m02 = pBuffer.get(bufferIndex(2, 0));
        this.m03 = pBuffer.get(bufferIndex(3, 0));
        this.m10 = pBuffer.get(bufferIndex(0, 1));
        this.m11 = pBuffer.get(bufferIndex(1, 1));
        this.m12 = pBuffer.get(bufferIndex(2, 1));
        this.m13 = pBuffer.get(bufferIndex(3, 1));
        this.m20 = pBuffer.get(bufferIndex(0, 2));
        this.m21 = pBuffer.get(bufferIndex(1, 2));
        this.m22 = pBuffer.get(bufferIndex(2, 2));
        this.m23 = pBuffer.get(bufferIndex(3, 2));
        this.m30 = pBuffer.get(bufferIndex(0, 3));
        this.m31 = pBuffer.get(bufferIndex(1, 3));
        this.m32 = pBuffer.get(bufferIndex(2, 3));
        this.m33 = pBuffer.get(bufferIndex(3, 3));
    }

    public void load(FloatBuffer pBuffer, boolean pTranspose)
    {
        if (pTranspose)
        {
            this.loadTransposed(pBuffer);
        }
        else
        {
            this.load(pBuffer);
        }
    }

    public void load(Matrix4f pOther)
    {
        this.m00 = pOther.m00;
        this.m01 = pOther.m01;
        this.m02 = pOther.m02;
        this.m03 = pOther.m03;
        this.m10 = pOther.m10;
        this.m11 = pOther.m11;
        this.m12 = pOther.m12;
        this.m13 = pOther.m13;
        this.m20 = pOther.m20;
        this.m21 = pOther.m21;
        this.m22 = pOther.m22;
        this.m23 = pOther.m23;
        this.m30 = pOther.m30;
        this.m31 = pOther.m31;
        this.m32 = pOther.m32;
        this.m33 = pOther.m33;
    }

    public String toString()
    {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("Matrix4f:\n");
        stringbuilder.append(this.m00);
        stringbuilder.append(" ");
        stringbuilder.append(this.m01);
        stringbuilder.append(" ");
        stringbuilder.append(this.m02);
        stringbuilder.append(" ");
        stringbuilder.append(this.m03);
        stringbuilder.append("\n");
        stringbuilder.append(this.m10);
        stringbuilder.append(" ");
        stringbuilder.append(this.m11);
        stringbuilder.append(" ");
        stringbuilder.append(this.m12);
        stringbuilder.append(" ");
        stringbuilder.append(this.m13);
        stringbuilder.append("\n");
        stringbuilder.append(this.m20);
        stringbuilder.append(" ");
        stringbuilder.append(this.m21);
        stringbuilder.append(" ");
        stringbuilder.append(this.m22);
        stringbuilder.append(" ");
        stringbuilder.append(this.m23);
        stringbuilder.append("\n");
        stringbuilder.append(this.m30);
        stringbuilder.append(" ");
        stringbuilder.append(this.m31);
        stringbuilder.append(" ");
        stringbuilder.append(this.m32);
        stringbuilder.append(" ");
        stringbuilder.append(this.m33);
        stringbuilder.append("\n");
        return stringbuilder.toString();
    }

    public void store(FloatBuffer pBuffer)
    {
        pBuffer.put(bufferIndex(0, 0), this.m00);
        pBuffer.put(bufferIndex(0, 1), this.m01);
        pBuffer.put(bufferIndex(0, 2), this.m02);
        pBuffer.put(bufferIndex(0, 3), this.m03);
        pBuffer.put(bufferIndex(1, 0), this.m10);
        pBuffer.put(bufferIndex(1, 1), this.m11);
        pBuffer.put(bufferIndex(1, 2), this.m12);
        pBuffer.put(bufferIndex(1, 3), this.m13);
        pBuffer.put(bufferIndex(2, 0), this.m20);
        pBuffer.put(bufferIndex(2, 1), this.m21);
        pBuffer.put(bufferIndex(2, 2), this.m22);
        pBuffer.put(bufferIndex(2, 3), this.m23);
        pBuffer.put(bufferIndex(3, 0), this.m30);
        pBuffer.put(bufferIndex(3, 1), this.m31);
        pBuffer.put(bufferIndex(3, 2), this.m32);
        pBuffer.put(bufferIndex(3, 3), this.m33);
    }

    public void storeTransposed(FloatBuffer pBuffer)
    {
        pBuffer.put(bufferIndex(0, 0), this.m00);
        pBuffer.put(bufferIndex(1, 0), this.m01);
        pBuffer.put(bufferIndex(2, 0), this.m02);
        pBuffer.put(bufferIndex(3, 0), this.m03);
        pBuffer.put(bufferIndex(0, 1), this.m10);
        pBuffer.put(bufferIndex(1, 1), this.m11);
        pBuffer.put(bufferIndex(2, 1), this.m12);
        pBuffer.put(bufferIndex(3, 1), this.m13);
        pBuffer.put(bufferIndex(0, 2), this.m20);
        pBuffer.put(bufferIndex(1, 2), this.m21);
        pBuffer.put(bufferIndex(2, 2), this.m22);
        pBuffer.put(bufferIndex(3, 2), this.m23);
        pBuffer.put(bufferIndex(0, 3), this.m30);
        pBuffer.put(bufferIndex(1, 3), this.m31);
        pBuffer.put(bufferIndex(2, 3), this.m32);
        pBuffer.put(bufferIndex(3, 3), this.m33);
    }

    public void store(FloatBuffer pBuffer, boolean pTranspose)
    {
        if (pTranspose)
        {
            this.storeTransposed(pBuffer);
        }
        else
        {
            this.store(pBuffer);
        }
    }

    public void setIdentity()
    {
        this.m00 = 1.0F;
        this.m01 = 0.0F;
        this.m02 = 0.0F;
        this.m03 = 0.0F;
        this.m10 = 0.0F;
        this.m11 = 1.0F;
        this.m12 = 0.0F;
        this.m13 = 0.0F;
        this.m20 = 0.0F;
        this.m21 = 0.0F;
        this.m22 = 1.0F;
        this.m23 = 0.0F;
        this.m30 = 0.0F;
        this.m31 = 0.0F;
        this.m32 = 0.0F;
        this.m33 = 1.0F;
    }

    public boolean isIdentity()
    {
        return this.m00 == 1.0F && this.m01 == 0.0F && this.m02 == 0.0F && this.m03 == 0.0F && this.m10 == 0.0F && this.m11 == 1.0F && this.m12 == 0.0F && this.m13 == 0.0F && this.m20 == 0.0F && this.m21 == 0.0F && this.m22 == 1.0F && this.m23 == 0.0F && this.m30 == 0.0F && this.m31 == 0.0F && this.m32 == 0.0F && this.m33 == 1.0F;
    }

    public float adjugateAndDet()
    {
        float f = this.m00 * this.m11 - this.m01 * this.m10;
        float f1 = this.m00 * this.m12 - this.m02 * this.m10;
        float f2 = this.m00 * this.m13 - this.m03 * this.m10;
        float f3 = this.m01 * this.m12 - this.m02 * this.m11;
        float f4 = this.m01 * this.m13 - this.m03 * this.m11;
        float f5 = this.m02 * this.m13 - this.m03 * this.m12;
        float f6 = this.m20 * this.m31 - this.m21 * this.m30;
        float f7 = this.m20 * this.m32 - this.m22 * this.m30;
        float f8 = this.m20 * this.m33 - this.m23 * this.m30;
        float f9 = this.m21 * this.m32 - this.m22 * this.m31;
        float f10 = this.m21 * this.m33 - this.m23 * this.m31;
        float f11 = this.m22 * this.m33 - this.m23 * this.m32;
        float f12 = this.m11 * f11 - this.m12 * f10 + this.m13 * f9;
        float f13 = -this.m10 * f11 + this.m12 * f8 - this.m13 * f7;
        float f14 = this.m10 * f10 - this.m11 * f8 + this.m13 * f6;
        float f15 = -this.m10 * f9 + this.m11 * f7 - this.m12 * f6;
        float f16 = -this.m01 * f11 + this.m02 * f10 - this.m03 * f9;
        float f17 = this.m00 * f11 - this.m02 * f8 + this.m03 * f7;
        float f18 = -this.m00 * f10 + this.m01 * f8 - this.m03 * f6;
        float f19 = this.m00 * f9 - this.m01 * f7 + this.m02 * f6;
        float f20 = this.m31 * f5 - this.m32 * f4 + this.m33 * f3;
        float f21 = -this.m30 * f5 + this.m32 * f2 - this.m33 * f1;
        float f22 = this.m30 * f4 - this.m31 * f2 + this.m33 * f;
        float f23 = -this.m30 * f3 + this.m31 * f1 - this.m32 * f;
        float f24 = -this.m21 * f5 + this.m22 * f4 - this.m23 * f3;
        float f25 = this.m20 * f5 - this.m22 * f2 + this.m23 * f1;
        float f26 = -this.m20 * f4 + this.m21 * f2 - this.m23 * f;
        float f27 = this.m20 * f3 - this.m21 * f1 + this.m22 * f;
        this.m00 = f12;
        this.m10 = f13;
        this.m20 = f14;
        this.m30 = f15;
        this.m01 = f16;
        this.m11 = f17;
        this.m21 = f18;
        this.m31 = f19;
        this.m02 = f20;
        this.m12 = f21;
        this.m22 = f22;
        this.m32 = f23;
        this.m03 = f24;
        this.m13 = f25;
        this.m23 = f26;
        this.m33 = f27;
        return f * f11 - f1 * f10 + f2 * f9 + f3 * f8 - f4 * f7 + f5 * f6;
    }

    public float determinant()
    {
        float f = this.m00 * this.m11 - this.m01 * this.m10;
        float f1 = this.m00 * this.m12 - this.m02 * this.m10;
        float f2 = this.m00 * this.m13 - this.m03 * this.m10;
        float f3 = this.m01 * this.m12 - this.m02 * this.m11;
        float f4 = this.m01 * this.m13 - this.m03 * this.m11;
        float f5 = this.m02 * this.m13 - this.m03 * this.m12;
        float f6 = this.m20 * this.m31 - this.m21 * this.m30;
        float f7 = this.m20 * this.m32 - this.m22 * this.m30;
        float f8 = this.m20 * this.m33 - this.m23 * this.m30;
        float f9 = this.m21 * this.m32 - this.m22 * this.m31;
        float f10 = this.m21 * this.m33 - this.m23 * this.m31;
        float f11 = this.m22 * this.m33 - this.m23 * this.m32;
        return f * f11 - f1 * f10 + f2 * f9 + f3 * f8 - f4 * f7 + f5 * f6;
    }

    public void transpose()
    {
        float f = this.m10;
        this.m10 = this.m01;
        this.m01 = f;
        f = this.m20;
        this.m20 = this.m02;
        this.m02 = f;
        f = this.m21;
        this.m21 = this.m12;
        this.m12 = f;
        f = this.m30;
        this.m30 = this.m03;
        this.m03 = f;
        f = this.m31;
        this.m31 = this.m13;
        this.m13 = f;
        f = this.m32;
        this.m32 = this.m23;
        this.m23 = f;
    }

    public boolean invertMojang()
    {
        float f = this.adjugateAndDet();

        if (Math.abs(f) > 1.0E-6F)
        {
            this.multiply(f);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean invert()
    {
        float f = this.adjugateAndDet();

        if ((double)f != 0.0D)
        {
            this.multiply(1.0F / f);
            return true;
        }
        else
        {
            return false;
        }
    }

    public void multiply(Matrix4f pMultiplier)
    {
        float f = this.m00 * pMultiplier.m00 + this.m01 * pMultiplier.m10 + this.m02 * pMultiplier.m20 + this.m03 * pMultiplier.m30;
        float f1 = this.m00 * pMultiplier.m01 + this.m01 * pMultiplier.m11 + this.m02 * pMultiplier.m21 + this.m03 * pMultiplier.m31;
        float f2 = this.m00 * pMultiplier.m02 + this.m01 * pMultiplier.m12 + this.m02 * pMultiplier.m22 + this.m03 * pMultiplier.m32;
        float f3 = this.m00 * pMultiplier.m03 + this.m01 * pMultiplier.m13 + this.m02 * pMultiplier.m23 + this.m03 * pMultiplier.m33;
        float f4 = this.m10 * pMultiplier.m00 + this.m11 * pMultiplier.m10 + this.m12 * pMultiplier.m20 + this.m13 * pMultiplier.m30;
        float f5 = this.m10 * pMultiplier.m01 + this.m11 * pMultiplier.m11 + this.m12 * pMultiplier.m21 + this.m13 * pMultiplier.m31;
        float f6 = this.m10 * pMultiplier.m02 + this.m11 * pMultiplier.m12 + this.m12 * pMultiplier.m22 + this.m13 * pMultiplier.m32;
        float f7 = this.m10 * pMultiplier.m03 + this.m11 * pMultiplier.m13 + this.m12 * pMultiplier.m23 + this.m13 * pMultiplier.m33;
        float f8 = this.m20 * pMultiplier.m00 + this.m21 * pMultiplier.m10 + this.m22 * pMultiplier.m20 + this.m23 * pMultiplier.m30;
        float f9 = this.m20 * pMultiplier.m01 + this.m21 * pMultiplier.m11 + this.m22 * pMultiplier.m21 + this.m23 * pMultiplier.m31;
        float f10 = this.m20 * pMultiplier.m02 + this.m21 * pMultiplier.m12 + this.m22 * pMultiplier.m22 + this.m23 * pMultiplier.m32;
        float f11 = this.m20 * pMultiplier.m03 + this.m21 * pMultiplier.m13 + this.m22 * pMultiplier.m23 + this.m23 * pMultiplier.m33;
        float f12 = this.m30 * pMultiplier.m00 + this.m31 * pMultiplier.m10 + this.m32 * pMultiplier.m20 + this.m33 * pMultiplier.m30;
        float f13 = this.m30 * pMultiplier.m01 + this.m31 * pMultiplier.m11 + this.m32 * pMultiplier.m21 + this.m33 * pMultiplier.m31;
        float f14 = this.m30 * pMultiplier.m02 + this.m31 * pMultiplier.m12 + this.m32 * pMultiplier.m22 + this.m33 * pMultiplier.m32;
        float f15 = this.m30 * pMultiplier.m03 + this.m31 * pMultiplier.m13 + this.m32 * pMultiplier.m23 + this.m33 * pMultiplier.m33;
        this.m00 = f;
        this.m01 = f1;
        this.m02 = f2;
        this.m03 = f3;
        this.m10 = f4;
        this.m11 = f5;
        this.m12 = f6;
        this.m13 = f7;
        this.m20 = f8;
        this.m21 = f9;
        this.m22 = f10;
        this.m23 = f11;
        this.m30 = f12;
        this.m31 = f13;
        this.m32 = f14;
        this.m33 = f15;
    }

    public void multiply(Quaternion pMultiplier)
    {
        float f = pMultiplier.i();
        float f1 = pMultiplier.j();
        float f2 = pMultiplier.k();
        float f3 = pMultiplier.r();
        float f4 = 2.0F * f * f;
        float f5 = 2.0F * f1 * f1;
        float f6 = 2.0F * f2 * f2;
        float f7 = f * f1;
        float f8 = f1 * f2;
        float f9 = f2 * f;
        float f10 = f * f3;
        float f11 = f1 * f3;
        float f12 = f2 * f3;
        float f13 = 1.0F - f5 - f6;
        float f14 = 2.0F * (f7 - f12);
        float f15 = 2.0F * (f9 + f11);
        float f16 = 0.0F;
        float f17 = 2.0F * (f7 + f12);
        float f18 = 1.0F - f6 - f4;
        float f19 = 2.0F * (f8 - f10);
        float f20 = 0.0F;
        float f21 = 2.0F * (f9 - f11);
        float f22 = 2.0F * (f8 + f10);
        float f23 = 1.0F - f4 - f5;
        float f24 = 0.0F;
        float f25 = 0.0F;
        float f26 = 0.0F;
        float f27 = 0.0F;
        float f28 = 1.0F;
        float f29 = this.m00 * f13 + this.m01 * f17 + this.m02 * f21 + this.m03 * f25;
        float f30 = this.m00 * f14 + this.m01 * f18 + this.m02 * f22 + this.m03 * f26;
        float f31 = this.m00 * f15 + this.m01 * f19 + this.m02 * f23 + this.m03 * f27;
        float f32 = this.m00 * f16 + this.m01 * f20 + this.m02 * f24 + this.m03 * f28;
        float f33 = this.m10 * f13 + this.m11 * f17 + this.m12 * f21 + this.m13 * f25;
        float f34 = this.m10 * f14 + this.m11 * f18 + this.m12 * f22 + this.m13 * f26;
        float f35 = this.m10 * f15 + this.m11 * f19 + this.m12 * f23 + this.m13 * f27;
        float f36 = this.m10 * f16 + this.m11 * f20 + this.m12 * f24 + this.m13 * f28;
        float f37 = this.m20 * f13 + this.m21 * f17 + this.m22 * f21 + this.m23 * f25;
        float f38 = this.m20 * f14 + this.m21 * f18 + this.m22 * f22 + this.m23 * f26;
        float f39 = this.m20 * f15 + this.m21 * f19 + this.m22 * f23 + this.m23 * f27;
        float f40 = this.m20 * f16 + this.m21 * f20 + this.m22 * f24 + this.m23 * f28;
        float f41 = this.m30 * f13 + this.m31 * f17 + this.m32 * f21 + this.m33 * f25;
        float f42 = this.m30 * f14 + this.m31 * f18 + this.m32 * f22 + this.m33 * f26;
        float f43 = this.m30 * f15 + this.m31 * f19 + this.m32 * f23 + this.m33 * f27;
        float f44 = this.m30 * f16 + this.m31 * f20 + this.m32 * f24 + this.m33 * f28;
        this.m00 = f29;
        this.m01 = f30;
        this.m02 = f31;
        this.m03 = f32;
        this.m10 = f33;
        this.m11 = f34;
        this.m12 = f35;
        this.m13 = f36;
        this.m20 = f37;
        this.m21 = f38;
        this.m22 = f39;
        this.m23 = f40;
        this.m30 = f41;
        this.m31 = f42;
        this.m32 = f43;
        this.m33 = f44;
    }

    public void multiply(float pMultiplier)
    {
        this.m00 *= pMultiplier;
        this.m01 *= pMultiplier;
        this.m02 *= pMultiplier;
        this.m03 *= pMultiplier;
        this.m10 *= pMultiplier;
        this.m11 *= pMultiplier;
        this.m12 *= pMultiplier;
        this.m13 *= pMultiplier;
        this.m20 *= pMultiplier;
        this.m21 *= pMultiplier;
        this.m22 *= pMultiplier;
        this.m23 *= pMultiplier;
        this.m30 *= pMultiplier;
        this.m31 *= pMultiplier;
        this.m32 *= pMultiplier;
        this.m33 *= pMultiplier;
    }

    public void add(Matrix4f pOther)
    {
        this.m00 += pOther.m00;
        this.m01 += pOther.m01;
        this.m02 += pOther.m02;
        this.m03 += pOther.m03;
        this.m10 += pOther.m10;
        this.m11 += pOther.m11;
        this.m12 += pOther.m12;
        this.m13 += pOther.m13;
        this.m20 += pOther.m20;
        this.m21 += pOther.m21;
        this.m22 += pOther.m22;
        this.m23 += pOther.m23;
        this.m30 += pOther.m30;
        this.m31 += pOther.m31;
        this.m32 += pOther.m32;
        this.m33 += pOther.m33;
    }

    public void subtract(Matrix4f pOther)
    {
        this.m00 -= pOther.m00;
        this.m01 -= pOther.m01;
        this.m02 -= pOther.m02;
        this.m03 -= pOther.m03;
        this.m10 -= pOther.m10;
        this.m11 -= pOther.m11;
        this.m12 -= pOther.m12;
        this.m13 -= pOther.m13;
        this.m20 -= pOther.m20;
        this.m21 -= pOther.m21;
        this.m22 -= pOther.m22;
        this.m23 -= pOther.m23;
        this.m30 -= pOther.m30;
        this.m31 -= pOther.m31;
        this.m32 -= pOther.m32;
        this.m33 -= pOther.m33;
    }

    public float trace()
    {
        return this.m00 + this.m11 + this.m22 + this.m33;
    }

    public static Matrix4f perspective(double pFov, float p_27627_, float pAspectRatio, float pNearPlane)
    {
        float f = (float)(1.0D / Math.tan(pFov * (double)((float)Math.PI / 180F) / 2.0D));
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.m00 = f / p_27627_;
        matrix4f.m11 = f;
        matrix4f.m22 = (pNearPlane + pAspectRatio) / (pAspectRatio - pNearPlane);
        matrix4f.m32 = -1.0F;
        matrix4f.m23 = 2.0F * pNearPlane * pAspectRatio / (pAspectRatio - pNearPlane);
        return matrix4f;
    }

    public static Matrix4f orthographic(float pWidth, float pHeight, float pNearPlane, float pFarPlane)
    {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.m00 = 2.0F / pWidth;
        matrix4f.m11 = 2.0F / pHeight;
        float f = pFarPlane - pNearPlane;
        matrix4f.m22 = -2.0F / f;
        matrix4f.m33 = 1.0F;
        matrix4f.m03 = -1.0F;
        matrix4f.m13 = 1.0F;
        matrix4f.m23 = -(pFarPlane + pNearPlane) / f;
        return matrix4f;
    }

    public static Matrix4f orthographic(float p_162204_, float p_162205_, float p_162206_, float p_162207_, float p_162208_, float p_162209_)
    {
        Matrix4f matrix4f = new Matrix4f();
        float f = p_162205_ - p_162204_;
        float f1 = p_162206_ - p_162207_;
        float f2 = p_162209_ - p_162208_;
        matrix4f.m00 = 2.0F / f;
        matrix4f.m11 = 2.0F / f1;
        matrix4f.m22 = -2.0F / f2;
        matrix4f.m03 = -(p_162205_ + p_162204_) / f;
        matrix4f.m13 = -(p_162206_ + p_162207_) / f1;
        matrix4f.m23 = -(p_162209_ + p_162208_) / f2;
        matrix4f.m33 = 1.0F;
        return matrix4f;
    }

    public void translate(Vector3f pVector)
    {
        this.m03 += pVector.x();
        this.m13 += pVector.y();
        this.m23 += pVector.z();
    }

    public Matrix4f copy()
    {
        return new Matrix4f(this);
    }

    public void multiplyWithTranslation(float p_162200_, float p_162201_, float p_162202_)
    {
        this.m03 += this.m00 * p_162200_ + this.m01 * p_162201_ + this.m02 * p_162202_;
        this.m13 += this.m10 * p_162200_ + this.m11 * p_162201_ + this.m12 * p_162202_;
        this.m23 += this.m20 * p_162200_ + this.m21 * p_162201_ + this.m22 * p_162202_;
        this.m33 += this.m30 * p_162200_ + this.m31 * p_162201_ + this.m32 * p_162202_;
    }

    public static Matrix4f createScaleMatrix(float p_27633_, float p_27634_, float p_27635_)
    {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.m00 = p_27633_;
        matrix4f.m11 = p_27634_;
        matrix4f.m22 = p_27635_;
        matrix4f.m33 = 1.0F;
        return matrix4f;
    }

    public static Matrix4f createTranslateMatrix(float p_27654_, float p_27655_, float p_27656_)
    {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.m00 = 1.0F;
        matrix4f.m11 = 1.0F;
        matrix4f.m22 = 1.0F;
        matrix4f.m33 = 1.0F;
        matrix4f.m03 = p_27654_;
        matrix4f.m13 = p_27655_;
        matrix4f.m23 = p_27656_;
        return matrix4f;
    }

    public float getTransformX(float x, float y, float z, float w)
    {
        return this.m00 * x + this.m01 * y + this.m02 * z + this.m03 * w;
    }

    public float getTransformY(float x, float y, float z, float w)
    {
        return this.m10 * x + this.m11 * y + this.m12 * z + this.m13 * w;
    }

    public float getTransformZ(float x, float y, float z, float w)
    {
        return this.m20 * x + this.m21 * y + this.m22 * z + this.m23 * w;
    }

    public float getTransformW(float x, float y, float z, float w)
    {
        return this.m30 * x + this.m31 * y + this.m32 * z + this.m33 * w;
    }

    public void mulScale(float xIn, float yIn, float zIn)
    {
        this.m00 *= xIn;
        this.m01 *= yIn;
        this.m02 *= zIn;
        this.m10 *= xIn;
        this.m11 *= yIn;
        this.m12 *= zIn;
        this.m20 *= xIn;
        this.m21 *= yIn;
        this.m22 *= zIn;
        this.m30 *= xIn;
        this.m31 *= yIn;
        this.m32 *= zIn;
    }

    public void setRandom(Random r)
    {
        this.m00 = r.nextFloat();
        this.m01 = r.nextFloat();
        this.m02 = r.nextFloat();
        this.m03 = r.nextFloat();
        this.m10 = r.nextFloat();
        this.m11 = r.nextFloat();
        this.m12 = r.nextFloat();
        this.m13 = r.nextFloat();
        this.m20 = r.nextFloat();
        this.m21 = r.nextFloat();
        this.m22 = r.nextFloat();
        this.m23 = r.nextFloat();
        this.m30 = r.nextFloat();
        this.m31 = r.nextFloat();
        this.m32 = r.nextFloat();
        this.m33 = r.nextFloat();
    }

    public void write(float[] floatArrayIn)
    {
        floatArrayIn[0] = this.m00;
        floatArrayIn[1] = this.m01;
        floatArrayIn[2] = this.m02;
        floatArrayIn[3] = this.m03;
        floatArrayIn[4] = this.m10;
        floatArrayIn[5] = this.m11;
        floatArrayIn[6] = this.m12;
        floatArrayIn[7] = this.m13;
        floatArrayIn[8] = this.m20;
        floatArrayIn[9] = this.m21;
        floatArrayIn[10] = this.m22;
        floatArrayIn[11] = this.m23;
        floatArrayIn[12] = this.m30;
        floatArrayIn[13] = this.m31;
        floatArrayIn[14] = this.m32;
        floatArrayIn[15] = this.m33;
    }

    public Matrix4f(float[] values)
    {
        this.m00 = values[0];
        this.m01 = values[1];
        this.m02 = values[2];
        this.m03 = values[3];
        this.m10 = values[4];
        this.m11 = values[5];
        this.m12 = values[6];
        this.m13 = values[7];
        this.m20 = values[8];
        this.m21 = values[9];
        this.m22 = values[10];
        this.m23 = values[11];
        this.m30 = values[12];
        this.m31 = values[13];
        this.m32 = values[14];
        this.m33 = values[15];
    }

    public void set(Matrix4f mat)
    {
        this.m00 = mat.m00;
        this.m01 = mat.m01;
        this.m02 = mat.m02;
        this.m03 = mat.m03;
        this.m10 = mat.m10;
        this.m11 = mat.m11;
        this.m12 = mat.m12;
        this.m13 = mat.m13;
        this.m20 = mat.m20;
        this.m21 = mat.m21;
        this.m22 = mat.m22;
        this.m23 = mat.m23;
        this.m30 = mat.m30;
        this.m31 = mat.m31;
        this.m32 = mat.m32;
        this.m33 = mat.m33;
    }

    public void multiplyBackward(Matrix4f other)
    {
        Matrix4f matrix4f = other.copy();
        matrix4f.multiply(this);
        this.set(matrix4f);
    }

    public void setTranslation(float x, float y, float z)
    {
        this.m00 = 1.0F;
        this.m11 = 1.0F;
        this.m22 = 1.0F;
        this.m33 = 1.0F;
        this.m03 = x;
        this.m13 = y;
        this.m23 = z;
    }
}
