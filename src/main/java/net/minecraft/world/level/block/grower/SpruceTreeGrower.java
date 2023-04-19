package net.minecraft.world.level.block.grower;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class SpruceTreeGrower extends AbstractMegaTreeGrower
{
    protected Holder <? extends ConfiguredFeature <? , ? >> getConfiguredFeature(RandomSource pRandom, boolean pLargeHive)
    {
        return TreeFeatures.SPRUCE;
    }

    protected Holder <? extends ConfiguredFeature <? , ? >> getConfiguredMegaFeature(RandomSource pRandom)
    {
        return pRandom.nextBoolean() ? TreeFeatures.MEGA_SPRUCE : TreeFeatures.MEGA_PINE;
    }
}
