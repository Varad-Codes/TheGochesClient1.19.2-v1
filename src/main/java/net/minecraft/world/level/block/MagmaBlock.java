package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MagmaBlock extends Block
{
    private static final int BUBBLE_COLUMN_CHECK_DELAY = 20;

    public MagmaBlock(BlockBehaviour.Properties p_54800_)
    {
        super(p_54800_);
    }

    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity)
    {
        if (!pEntity.isSteppingCarefully() && pEntity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)pEntity))
        {
            pEntity.hurt(DamageSource.HOT_FLOOR, 1.0F);
        }

        super.stepOn(pLevel, pPos, pState, pEntity);
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRand)
    {
        BubbleColumnBlock.updateColumn(pLevel, pPos.above(), pState);
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        if (pFacing == Direction.UP && pFacingState.is(Blocks.WATER))
        {
            pLevel.scheduleTick(pCurrentPos, this, 20);
        }

        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom)
    {
        BlockPos blockpos = pPos.above();

        if (pLevel.getFluidState(pPos).is(FluidTags.WATER))
        {
            pLevel.playSound((Player)null, pPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (pLevel.random.nextFloat() - pLevel.random.nextFloat()) * 0.8F);
            pLevel.sendParticles(ParticleTypes.LARGE_SMOKE, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.25D, (double)blockpos.getZ() + 0.5D, 8, 0.5D, 0.25D, 0.5D, 0.0D);
        }
    }

    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving)
    {
        pLevel.scheduleTick(pPos, this, 20);
    }
}
