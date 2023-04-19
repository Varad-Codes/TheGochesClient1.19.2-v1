package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RootSystemFeature extends Feature<RootSystemConfiguration>
{
    public RootSystemFeature(Codec<RootSystemConfiguration> p_160218_)
    {
        super(p_160218_);
    }

    public boolean place(FeaturePlaceContext<RootSystemConfiguration> pContext)
    {
        WorldGenLevel worldgenlevel = pContext.level();
        BlockPos blockpos = pContext.origin();

        if (!worldgenlevel.getBlockState(blockpos).isAir())
        {
            return false;
        }
        else
        {
            RandomSource randomsource = pContext.random();
            BlockPos blockpos1 = pContext.origin();
            RootSystemConfiguration rootsystemconfiguration = pContext.config();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = blockpos1.mutable();

            if (placeDirtAndTree(worldgenlevel, pContext.chunkGenerator(), rootsystemconfiguration, randomsource, blockpos$mutableblockpos, blockpos1))
            {
                placeRoots(worldgenlevel, rootsystemconfiguration, randomsource, blockpos1, blockpos$mutableblockpos);
            }

            return true;
        }
    }

    private static boolean spaceForTree(WorldGenLevel pLevel, RootSystemConfiguration pConfig, BlockPos pPos)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();

        for (int i = 1; i <= pConfig.requiredVerticalSpaceForTree; ++i)
        {
            blockpos$mutableblockpos.move(Direction.UP);
            BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos);

            if (!isAllowedTreeSpace(blockstate, i, pConfig.allowedVerticalWaterForTree))
            {
                return false;
            }
        }

        return true;
    }

    private static boolean isAllowedTreeSpace(BlockState pState, int pY, int pAllowedVerticalWater)
    {
        if (pState.isAir())
        {
            return true;
        }
        else
        {
            int i = pY + 1;
            return i <= pAllowedVerticalWater && pState.getFluidState().is(FluidTags.WATER);
        }
    }

    private static boolean placeDirtAndTree(WorldGenLevel pLevel, ChunkGenerator pGenerator, RootSystemConfiguration pConfig, RandomSource pRandom, BlockPos.MutableBlockPos pMutablePos, BlockPos pBasePos)
    {
        for (int i = 0; i < pConfig.rootColumnMaxHeight; ++i)
        {
            pMutablePos.move(Direction.UP);

            if (pConfig.allowedTreePosition.test(pLevel, pMutablePos) && spaceForTree(pLevel, pConfig, pMutablePos))
            {
                BlockPos blockpos = pMutablePos.below();

                if (pLevel.getFluidState(blockpos).is(FluidTags.LAVA) || !pLevel.getBlockState(blockpos).getMaterial().isSolid())
                {
                    return false;
                }

                if (((PlacedFeature)pConfig.treeFeature.value()).place(pLevel, pGenerator, pRandom, pMutablePos))
                {
                    placeDirt(pBasePos, pBasePos.getY() + i, pLevel, pConfig, pRandom);
                    return true;
                }
            }
        }

        return false;
    }

    private static void placeDirt(BlockPos p_225223_, int p_225224_, WorldGenLevel p_225225_, RootSystemConfiguration p_225226_, RandomSource p_225227_)
    {
        int i = p_225223_.getX();
        int j = p_225223_.getZ();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = p_225223_.mutable();

        for (int k = p_225223_.getY(); k < p_225224_; ++k)
        {
            placeRootedDirt(p_225225_, p_225226_, p_225227_, i, j, blockpos$mutableblockpos.set(i, k, j));
        }
    }

    private static void placeRootedDirt(WorldGenLevel p_225210_, RootSystemConfiguration p_225211_, RandomSource p_225212_, int p_225213_, int p_225214_, BlockPos.MutableBlockPos p_225215_)
    {
        int i = p_225211_.rootRadius;
        Predicate<BlockState> predicate = (p_204762_) ->
        {
            return p_204762_.is(p_225211_.rootReplaceable);
        };

        for (int j = 0; j < p_225211_.rootPlacementAttempts; ++j)
        {
            p_225215_.setWithOffset(p_225215_, p_225212_.nextInt(i) - p_225212_.nextInt(i), 0, p_225212_.nextInt(i) - p_225212_.nextInt(i));

            if (predicate.test(p_225210_.getBlockState(p_225215_)))
            {
                p_225210_.setBlock(p_225215_, p_225211_.rootStateProvider.getState(p_225212_, p_225215_), 2);
            }

            p_225215_.setX(p_225213_);
            p_225215_.setZ(p_225214_);
        }
    }

    private static void placeRoots(WorldGenLevel pLevel, RootSystemConfiguration pConfig, RandomSource pRandom, BlockPos pBasePos, BlockPos.MutableBlockPos pMutablePos)
    {
        int i = pConfig.hangingRootRadius;
        int j = pConfig.hangingRootsVerticalSpan;

        for (int k = 0; k < pConfig.hangingRootPlacementAttempts; ++k)
        {
            pMutablePos.setWithOffset(pBasePos, pRandom.nextInt(i) - pRandom.nextInt(i), pRandom.nextInt(j) - pRandom.nextInt(j), pRandom.nextInt(i) - pRandom.nextInt(i));

            if (pLevel.isEmptyBlock(pMutablePos))
            {
                BlockState blockstate = pConfig.hangingRootStateProvider.getState(pRandom, pMutablePos);

                if (blockstate.canSurvive(pLevel, pMutablePos) && pLevel.getBlockState(pMutablePos.above()).isFaceSturdy(pLevel, pMutablePos, Direction.DOWN))
                {
                    pLevel.setBlock(pMutablePos, blockstate, 2);
                }
            }
        }
    }
}
