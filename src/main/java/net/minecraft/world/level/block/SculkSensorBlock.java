package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkSensorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock
{
    public static final int ACTIVE_TICKS = 40;
    public static final int COOLDOWN_TICKS = 1;
    public static final Object2IntMap<GameEvent> VIBRATION_FREQUENCY_FOR_EVENT = Object2IntMaps.unmodifiable(Util.make(new Object2IntOpenHashMap<>(), (p_238254_) ->
    {
        p_238254_.put(GameEvent.STEP, 1);
        p_238254_.put(GameEvent.FLAP, 2);
        p_238254_.put(GameEvent.SWIM, 3);
        p_238254_.put(GameEvent.ELYTRA_GLIDE, 4);
        p_238254_.put(GameEvent.HIT_GROUND, 5);
        p_238254_.put(GameEvent.TELEPORT, 5);
        p_238254_.put(GameEvent.SPLASH, 6);
        p_238254_.put(GameEvent.ENTITY_SHAKE, 6);
        p_238254_.put(GameEvent.BLOCK_CHANGE, 6);
        p_238254_.put(GameEvent.NOTE_BLOCK_PLAY, 6);
        p_238254_.put(GameEvent.PROJECTILE_SHOOT, 7);
        p_238254_.put(GameEvent.DRINK, 7);
        p_238254_.put(GameEvent.PRIME_FUSE, 7);
        p_238254_.put(GameEvent.PROJECTILE_LAND, 8);
        p_238254_.put(GameEvent.EAT, 8);
        p_238254_.put(GameEvent.ENTITY_INTERACT, 8);
        p_238254_.put(GameEvent.ENTITY_DAMAGE, 8);
        p_238254_.put(GameEvent.EQUIP, 9);
        p_238254_.put(GameEvent.SHEAR, 9);
        p_238254_.put(GameEvent.ENTITY_ROAR, 9);
        p_238254_.put(GameEvent.BLOCK_CLOSE, 10);
        p_238254_.put(GameEvent.BLOCK_DEACTIVATE, 10);
        p_238254_.put(GameEvent.BLOCK_DETACH, 10);
        p_238254_.put(GameEvent.DISPENSE_FAIL, 10);
        p_238254_.put(GameEvent.BLOCK_OPEN, 11);
        p_238254_.put(GameEvent.BLOCK_ACTIVATE, 11);
        p_238254_.put(GameEvent.BLOCK_ATTACH, 11);
        p_238254_.put(GameEvent.ENTITY_PLACE, 12);
        p_238254_.put(GameEvent.BLOCK_PLACE, 12);
        p_238254_.put(GameEvent.FLUID_PLACE, 12);
        p_238254_.put(GameEvent.ENTITY_DIE, 13);
        p_238254_.put(GameEvent.BLOCK_DESTROY, 13);
        p_238254_.put(GameEvent.FLUID_PICKUP, 13);
        p_238254_.put(GameEvent.ITEM_INTERACT_FINISH, 14);
        p_238254_.put(GameEvent.CONTAINER_CLOSE, 14);
        p_238254_.put(GameEvent.PISTON_CONTRACT, 14);
        p_238254_.put(GameEvent.PISTON_EXTEND, 15);
        p_238254_.put(GameEvent.CONTAINER_OPEN, 15);
        p_238254_.put(GameEvent.ITEM_INTERACT_START, 15);
        p_238254_.put(GameEvent.EXPLODE, 15);
        p_238254_.put(GameEvent.LIGHTNING_STRIKE, 15);
        p_238254_.put(GameEvent.INSTRUMENT_PLAY, 15);
    }));
    public static final EnumProperty<SculkSensorPhase> PHASE = BlockStateProperties.SCULK_SENSOR_PHASE;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    private final int listenerRange;

    public SculkSensorBlock(BlockBehaviour.Properties pProperties, int pListenerRange)
    {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, SculkSensorPhase.INACTIVE).setValue(POWER, Integer.valueOf(0)).setValue(WATERLOGGED, Boolean.valueOf(false)));
        this.listenerRange = pListenerRange;
    }

    public int getListenerRange()
    {
        return this.listenerRange;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        BlockPos blockpos = pContext.getClickedPos();
        FluidState fluidstate = pContext.getLevel().getFluidState(blockpos);
        return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
    }

    public FluidState getFluidState(BlockState pState)
    {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom)
    {
        if (getPhase(pState) != SculkSensorPhase.ACTIVE)
        {
            if (getPhase(pState) == SculkSensorPhase.COOLDOWN)
            {
                pLevel.setBlock(pPos, pState.setValue(PHASE, SculkSensorPhase.INACTIVE), 3);
            }
        }
        else
        {
            deactivate(pLevel, pPos, pState);
        }
    }

    public void stepOn(Level p_222132_, BlockPos p_222133_, BlockState p_222134_, Entity p_222135_)
    {
        if (!p_222132_.isClientSide() && canActivate(p_222134_) && p_222135_.getType() != EntityType.WARDEN)
        {
            BlockEntity blockentity = p_222132_.getBlockEntity(p_222133_);

            if (blockentity instanceof SculkSensorBlockEntity)
            {
                SculkSensorBlockEntity sculksensorblockentity = (SculkSensorBlockEntity)blockentity;
                sculksensorblockentity.setLastVibrationFrequency(VIBRATION_FREQUENCY_FOR_EVENT.get(GameEvent.STEP));
            }

            activate(p_222135_, p_222132_, p_222133_, p_222134_, 15);
        }

        super.stepOn(p_222132_, p_222133_, p_222134_, p_222135_);
    }

    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving)
    {
        if (!pLevel.isClientSide() && !pState.is(pOldState.getBlock()))
        {
            if (pState.getValue(POWER) > 0 && !pLevel.getBlockTicks().hasScheduledTick(pPos, this))
            {
                pLevel.setBlock(pPos, pState.setValue(POWER, Integer.valueOf(0)), 18);
            }

            pLevel.scheduleTick(new BlockPos(pPos), pState.getBlock(), 1);
        }
    }

    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving)
    {
        if (!pState.is(pNewState.getBlock()))
        {
            if (getPhase(pState) == SculkSensorPhase.ACTIVE)
            {
                updateNeighbours(pLevel, pPos);
            }

            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos)
    {
        if (pState.getValue(WATERLOGGED))
        {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }

        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
    }

    private static void updateNeighbours(Level pLevel, BlockPos pPos)
    {
        pLevel.updateNeighborsAt(pPos, Blocks.SCULK_SENSOR);
        pLevel.updateNeighborsAt(pPos.relative(Direction.UP.getOpposite()), Blocks.SCULK_SENSOR);
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return new SculkSensorBlockEntity(pPos, pState);
    }

    @Nullable
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel pLevel, T pBlockEntity)
    {
        return pBlockEntity instanceof SculkSensorBlockEntity ? ((SculkSensorBlockEntity)pBlockEntity).getListener() : null;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType)
    {
        return !pLevel.isClientSide ? createTickerHelper(pBlockEntityType, BlockEntityType.SCULK_SENSOR, (p_154417_, p_154418_, p_154419_, p_154420_) ->
        {
            p_154420_.getListener().tick(p_154417_);
        }) : null;
    }

    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return SHAPE;
    }

    public boolean isSignalSource(BlockState pState)
    {
        return true;
    }

    public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection)
    {
        return pState.getValue(POWER);
    }

    public static SculkSensorPhase getPhase(BlockState pState)
    {
        return pState.getValue(PHASE);
    }

    public static boolean canActivate(BlockState pState)
    {
        return getPhase(pState) == SculkSensorPhase.INACTIVE;
    }

    public static void deactivate(Level pLevel, BlockPos pPos, BlockState pState)
    {
        pLevel.setBlock(pPos, pState.setValue(PHASE, SculkSensorPhase.COOLDOWN).setValue(POWER, Integer.valueOf(0)), 3);
        pLevel.scheduleTick(pPos, pState.getBlock(), 1);

        if (!pState.getValue(WATERLOGGED))
        {
            pLevel.playSound((Player)null, pPos, SoundEvents.SCULK_CLICKING_STOP, SoundSource.BLOCKS, 1.0F, pLevel.random.nextFloat() * 0.2F + 0.8F);
        }

        updateNeighbours(pLevel, pPos);
    }

    public static void activate(@Nullable Entity p_222126_, Level p_222127_, BlockPos p_222128_, BlockState p_222129_, int p_222130_)
    {
        p_222127_.setBlock(p_222128_, p_222129_.setValue(PHASE, SculkSensorPhase.ACTIVE).setValue(POWER, Integer.valueOf(p_222130_)), 3);
        p_222127_.scheduleTick(p_222128_, p_222129_.getBlock(), 40);
        updateNeighbours(p_222127_, p_222128_);
        p_222127_.gameEvent(p_222126_, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, p_222128_);

        if (!p_222129_.getValue(WATERLOGGED))
        {
            p_222127_.playSound((Player)null, (double)p_222128_.getX() + 0.5D, (double)p_222128_.getY() + 0.5D, (double)p_222128_.getZ() + 0.5D, SoundEvents.SCULK_CLICKING, SoundSource.BLOCKS, 1.0F, p_222127_.random.nextFloat() * 0.2F + 0.8F);
        }
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom)
    {
        if (getPhase(pState) == SculkSensorPhase.ACTIVE)
        {
            Direction direction = Direction.getRandom(pRandom);

            if (direction != Direction.UP && direction != Direction.DOWN)
            {
                double d0 = (double)pPos.getX() + 0.5D + (direction.getStepX() == 0 ? 0.5D - pRandom.nextDouble() : (double)direction.getStepX() * 0.6D);
                double d1 = (double)pPos.getY() + 0.25D;
                double d2 = (double)pPos.getZ() + 0.5D + (direction.getStepZ() == 0 ? 0.5D - pRandom.nextDouble() : (double)direction.getStepZ() * 0.6D);
                double d3 = (double)pRandom.nextFloat() * 0.04D;
                pLevel.addParticle(DustColorTransitionOptions.SCULK_TO_REDSTONE, d0, d1, d2, 0.0D, d3, 0.0D);
            }
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.a(PHASE, POWER, WATERLOGGED);
    }

    public boolean hasAnalogOutputSignal(BlockState pState)
    {
        return true;
    }

    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos)
    {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);

        if (blockentity instanceof SculkSensorBlockEntity sculksensorblockentity)
        {
            return getPhase(pState) == SculkSensorPhase.ACTIVE ? sculksensorblockentity.getLastVibrationFrequency() : 0;
        }
        else
        {
            return 0;
        }
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType)
    {
        return false;
    }

    public boolean useShapeForLightOcclusion(BlockState pState)
    {
        return true;
    }

    public void spawnAfterBreak(BlockState p_222142_, ServerLevel p_222143_, BlockPos p_222144_, ItemStack p_222145_, boolean p_222146_)
    {
        super.spawnAfterBreak(p_222142_, p_222143_, p_222144_, p_222145_, p_222146_);

        if (p_222146_)
        {
            this.tryDropExperience(p_222143_, p_222144_, p_222145_, ConstantInt.of(5));
        }
    }
}
