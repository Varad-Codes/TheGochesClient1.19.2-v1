package net.minecraftforge.client.extensions.common;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public interface IClientMobEffectExtensions
{
    IClientMobEffectExtensions DUMMY = new IClientMobEffectExtensions()
    {
    };

    static IClientMobEffectExtensions of(MobEffectInstance mobeffectinstance)
    {
        return DUMMY;
    }

    static IClientMobEffectExtensions of(MobEffect effect)
    {
        return DUMMY;
    }

default boolean isVisibleInInventory(MobEffectInstance instance)
    {
        return true;
    }

default boolean isVisibleInGui(MobEffectInstance instance)
    {
        return true;
    }

default boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, PoseStack poseStack, int x, int y, int blitOffset)
    {
        return false;
    }

default boolean renderInventoryText(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, PoseStack poseStack, int x, int y, int blitOffset)
    {
        return false;
    }

default boolean renderGuiIcon(MobEffectInstance instance, Gui gui, PoseStack poseStack, int x, int y, float z, float alpha)
    {
        return false;
    }
}
