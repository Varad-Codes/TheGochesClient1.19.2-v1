package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.lighting.LayerLightEngine;

public class NyliumBlock extends Block implements BonemealableBlock
{
    protected NyliumBlock(BlockBehaviour.Properties p_55057_)
    {
        super(p_55057_);
    }

    private static boolean canBeNylium(BlockState pState, LevelReader pReader, BlockPos pPos)
    {
        BlockPos blockpos = pPos.above();
        BlockState blockstate = pReader.getBlockState(blockpos);
        int i = LayerLightEngine.getLightBlockInto(pReader, pState, pPos, blockstate, blockpos, Direction.UP, blockstate.getLightBlock(pReader, blockpos));
        return i < pReader.getMaxLightLevel();
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom)
    {
        if (!canBeNylium(pState, pLevel, pPos))
        {
            pLevel.setBlockAndUpdate(pPos, Blocks.NETHERRACK.defaultBlockState());
        }
    }

    public boolean isValidBonemealTarget(BlockGetter pLevel, BlockPos pPos, BlockState pState, boolean pIsClient)
    {
        return pLevel.getBlockState(pPos.above()).isAir();
    }

    public boolean isBonemealSuccess(Level pLevel, RandomSource pRand, BlockPos pPos, BlockState pState)
    {
        return true;
    }

    public void performBonemeal(ServerLevel pLevel, RandomSource pRand, BlockPos pPos, BlockState pState)
    {
        BlockState blockstate = pLevel.getBlockState(pPos);
        BlockPos blockpos = pPos.above();
        ChunkGenerator chunkgenerator = pLevel.getChunkSource().getGenerator();

        if (blockstate.is(Blocks.CRIMSON_NYLIUM))
        {
            ((ConfiguredFeature)NetherFeatures.CRIMSON_FOREST_VEGETATION_BONEMEAL.value()).place(pLevel, chunkgenerator, pRand, blockpos);
        }
        else if (blockstate.is(Blocks.WARPED_NYLIUM))
        {
            ((ConfiguredFeature)NetherFeatures.WARPED_FOREST_VEGETATION_BONEMEAL.value()).place(pLevel, chunkgenerator, pRand, blockpos);
            ((ConfiguredFeature)NetherFeatures.NETHER_SPROUTS_BONEMEAL.value()).place(pLevel, chunkgenerator, pRand, blockpos);

            if (pRand.nextInt(8) == 0)
            {
                ((ConfiguredFeature)NetherFeatures.TWISTING_VINES_BONEMEAL.value()).place(pLevel, chunkgenerator, pRand, blockpos);
            }
        }
    }
}
