package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig;

public class TwistingVinesFeature extends Feature<TwistingVinesConfig>
{
    public TwistingVinesFeature(Codec<TwistingVinesConfig> p_67292_)
    {
        super(p_67292_);
    }

    public boolean place(FeaturePlaceContext<TwistingVinesConfig> pContext)
    {
        WorldGenLevel worldgenlevel = pContext.level();
        BlockPos blockpos = pContext.origin();

        if (isInvalidPlacementLocation(worldgenlevel, blockpos))
        {
            return false;
        }
        else
        {
            RandomSource randomsource = pContext.random();
            TwistingVinesConfig twistingvinesconfig = pContext.config();
            int i = twistingvinesconfig.spreadWidth();
            int j = twistingvinesconfig.spreadHeight();
            int k = twistingvinesconfig.maxHeight();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int l = 0; l < i * i; ++l)
            {
                blockpos$mutableblockpos.set(blockpos).move(Mth.nextInt(randomsource, -i, i), Mth.nextInt(randomsource, -j, j), Mth.nextInt(randomsource, -i, i));

                if (findFirstAirBlockAboveGround(worldgenlevel, blockpos$mutableblockpos) && !isInvalidPlacementLocation(worldgenlevel, blockpos$mutableblockpos))
                {
                    int i1 = Mth.nextInt(randomsource, 1, k);

                    if (randomsource.nextInt(6) == 0)
                    {
                        i1 *= 2;
                    }

                    if (randomsource.nextInt(5) == 0)
                    {
                        i1 = 1;
                    }

                    int j1 = 17;
                    int k1 = 25;
                    placeWeepingVinesColumn(worldgenlevel, randomsource, blockpos$mutableblockpos, i1, 17, 25);
                }
            }

            return true;
        }
    }

    private static boolean findFirstAirBlockAboveGround(LevelAccessor pLevel, BlockPos.MutableBlockPos pPos)
    {
        do
        {
            pPos.move(0, -1, 0);

            if (pLevel.isOutsideBuildHeight(pPos))
            {
                return false;
            }
        }
        while (pLevel.getBlockState(pPos).isAir());

        pPos.move(0, 1, 0);
        return true;
    }

    public static void placeWeepingVinesColumn(LevelAccessor p_225301_, RandomSource p_225302_, BlockPos.MutableBlockPos p_225303_, int p_225304_, int p_225305_, int p_225306_)
    {
        for (int i = 1; i <= p_225304_; ++i)
        {
            if (p_225301_.isEmptyBlock(p_225303_))
            {
                if (i == p_225304_ || !p_225301_.isEmptyBlock(p_225303_.above()))
                {
                    p_225301_.setBlock(p_225303_, Blocks.TWISTING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Integer.valueOf(Mth.nextInt(p_225302_, p_225305_, p_225306_))), 2);
                    break;
                }

                p_225301_.setBlock(p_225303_, Blocks.TWISTING_VINES_PLANT.defaultBlockState(), 2);
            }

            p_225303_.move(Direction.UP);
        }
    }

    private static boolean isInvalidPlacementLocation(LevelAccessor pLevel, BlockPos pPos)
    {
        if (!pLevel.isEmptyBlock(pPos))
        {
            return true;
        }
        else
        {
            BlockState blockstate = pLevel.getBlockState(pPos.below());
            return !blockstate.is(Blocks.NETHERRACK) && !blockstate.is(Blocks.WARPED_NYLIUM) && !blockstate.is(Blocks.WARPED_WART_BLOCK);
        }
    }
}
