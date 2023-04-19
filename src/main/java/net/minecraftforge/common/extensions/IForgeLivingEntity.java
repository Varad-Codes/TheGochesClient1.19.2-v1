package net.minecraftforge.common.extensions;

import net.minecraftforge.fluids.FluidType;

public interface IForgeLivingEntity
{
default void jumpInFluid(FluidType type)
    {
    }
}
