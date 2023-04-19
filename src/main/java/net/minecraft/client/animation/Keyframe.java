package net.minecraft.client.animation;

import com.mojang.math.Vector3f;

public record Keyframe(float timestamp, Vector3f target, AnimationChannel.Interpolation interpolation)
{
}
