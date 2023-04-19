package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class DarkOakTreeGrower extends AbstractMegaTreeGrower
{
    @Nullable
    protected Holder <? extends ConfiguredFeature <? , ? >> getConfiguredFeature(RandomSource pRandom, boolean pLargeHive)
    {
        return null;
    }

    @Nullable
    protected Holder <? extends ConfiguredFeature <? , ? >> getConfiguredMegaFeature(RandomSource pRand)
    {
        return TreeFeatures.DARK_OAK;
    }
}
