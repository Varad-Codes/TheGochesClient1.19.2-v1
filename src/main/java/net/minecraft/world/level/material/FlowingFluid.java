package net.minecraft.world.level.material;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FlowingFluid extends Fluid
{
    public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_FLOWING;
    private static final int CACHE_SIZE = 200;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() ->
    {
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(200)
        {
            protected void rehash(int p_76102_)
            {
            }
        };
        object2bytelinkedopenhashmap.defaultReturnValue((byte)127);
        return object2bytelinkedopenhashmap;
    });
    private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> pBuilder)
    {
        pBuilder.a(FALLING);
    }

    public Vec3 getFlow(BlockGetter pBlockReader, BlockPos pPos, FluidState pFluidState)
    {
        double d0 = 0.0D;
        double d1 = 0.0D;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            blockpos$mutableblockpos.setWithOffset(pPos, direction);
            FluidState fluidstate = pBlockReader.getFluidState(blockpos$mutableblockpos);

            if (this.affectsFlow(fluidstate))
            {
                float f = fluidstate.getOwnHeight();
                float f1 = 0.0F;

                if (f == 0.0F)
                {
                    if (!pBlockReader.getBlockState(blockpos$mutableblockpos).getMaterial().blocksMotion())
                    {
                        BlockPos blockpos = blockpos$mutableblockpos.below();
                        FluidState fluidstate1 = pBlockReader.getFluidState(blockpos);

                        if (this.affectsFlow(fluidstate1))
                        {
                            f = fluidstate1.getOwnHeight();

                            if (f > 0.0F)
                            {
                                f1 = pFluidState.getOwnHeight() - (f - 0.8888889F);
                            }
                        }
                    }
                }
                else if (f > 0.0F)
                {
                    f1 = pFluidState.getOwnHeight() - f;
                }

                if (f1 != 0.0F)
                {
                    d0 += (double)((float)direction.getStepX() * f1);
                    d1 += (double)((float)direction.getStepZ() * f1);
                }
            }
        }

        Vec3 vec3 = new Vec3(d0, 0.0D, d1);

        if (pFluidState.getValue(FALLING))
        {
            for (Direction direction1 : Direction.Plane.HORIZONTAL)
            {
                blockpos$mutableblockpos.setWithOffset(pPos, direction1);

                if (this.isSolidFace(pBlockReader, blockpos$mutableblockpos, direction1) || this.isSolidFace(pBlockReader, blockpos$mutableblockpos.above(), direction1))
                {
                    vec3 = vec3.normalize().add(0.0D, -6.0D, 0.0D);
                    break;
                }
            }
        }

        return vec3.normalize();
    }

    private boolean affectsFlow(FluidState pState)
    {
        return pState.isEmpty() || pState.getType().isSame(this);
    }

    protected boolean isSolidFace(BlockGetter pLevel, BlockPos pNeighborPos, Direction pSide)
    {
        BlockState blockstate = pLevel.getBlockState(pNeighborPos);
        FluidState fluidstate = pLevel.getFluidState(pNeighborPos);

        if (fluidstate.getType().isSame(this))
        {
            return false;
        }
        else if (pSide == Direction.UP)
        {
            return true;
        }
        else
        {
            return blockstate.getMaterial() == Material.ICE ? false : blockstate.isFaceSturdy(pLevel, pNeighborPos, pSide);
        }
    }

    protected void spread(LevelAccessor pLevel, BlockPos pPos, FluidState pState)
    {
        if (!pState.isEmpty())
        {
            BlockState blockstate = pLevel.getBlockState(pPos);
            BlockPos blockpos = pPos.below();
            BlockState blockstate1 = pLevel.getBlockState(blockpos);
            FluidState fluidstate = this.getNewLiquid(pLevel, blockpos, blockstate1);

            if (this.canSpreadTo(pLevel, pPos, blockstate, Direction.DOWN, blockpos, blockstate1, pLevel.getFluidState(blockpos), fluidstate.getType()))
            {
                this.spreadTo(pLevel, blockpos, blockstate1, Direction.DOWN, fluidstate);

                if (this.sourceNeighborCount(pLevel, pPos) >= 3)
                {
                    this.spreadToSides(pLevel, pPos, pState, blockstate);
                }
            }
            else if (pState.isSource() || !this.isWaterHole(pLevel, fluidstate.getType(), pPos, blockstate, blockpos, blockstate1))
            {
                this.spreadToSides(pLevel, pPos, pState, blockstate);
            }
        }
    }

    private void spreadToSides(LevelAccessor p_76015_, BlockPos p_76016_, FluidState p_76017_, BlockState p_76018_)
    {
        int i = p_76017_.getAmount() - this.getDropOff(p_76015_);

        if (p_76017_.getValue(FALLING))
        {
            i = 7;
        }

        if (i > 0)
        {
            Map<Direction, FluidState> map = this.getSpread(p_76015_, p_76016_, p_76018_);

            for (Map.Entry<Direction, FluidState> entry : map.entrySet())
            {
                Direction direction = entry.getKey();
                FluidState fluidstate = entry.getValue();
                BlockPos blockpos = p_76016_.relative(direction);
                BlockState blockstate = p_76015_.getBlockState(blockpos);

                if (this.canSpreadTo(p_76015_, p_76016_, p_76018_, direction, blockpos, blockstate, p_76015_.getFluidState(blockpos), fluidstate.getType()))
                {
                    this.spreadTo(p_76015_, blockpos, blockstate, direction, fluidstate);
                }
            }
        }
    }

    protected FluidState getNewLiquid(LevelReader pLevel, BlockPos pPos, BlockState pBlockState)
    {
        int i = 0;
        int j = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            BlockPos blockpos = pPos.relative(direction);
            BlockState blockstate = pLevel.getBlockState(blockpos);
            FluidState fluidstate = blockstate.getFluidState();

            if (fluidstate.getType().isSame(this) && this.canPassThroughWall(direction, pLevel, pPos, pBlockState, blockpos, blockstate))
            {
                if (fluidstate.isSource())
                {
                    ++j;
                }

                i = Math.max(i, fluidstate.getAmount());
            }
        }

        if (this.canConvertToSource() && j >= 2)
        {
            BlockState blockstate1 = pLevel.getBlockState(pPos.below());
            FluidState fluidstate1 = blockstate1.getFluidState();

            if (blockstate1.getMaterial().isSolid() || this.isSourceBlockOfThisType(fluidstate1))
            {
                return this.getSource(false);
            }
        }

        BlockPos blockpos1 = pPos.above();
        BlockState blockstate2 = pLevel.getBlockState(blockpos1);
        FluidState fluidstate2 = blockstate2.getFluidState();

        if (!fluidstate2.isEmpty() && fluidstate2.getType().isSame(this) && this.canPassThroughWall(Direction.UP, pLevel, pPos, pBlockState, blockpos1, blockstate2))
        {
            return this.getFlowing(8, true);
        }
        else
        {
            int k = i - this.getDropOff(pLevel);
            return k <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(k, false);
        }
    }

    private boolean canPassThroughWall(Direction p_76062_, BlockGetter p_76063_, BlockPos p_76064_, BlockState p_76065_, BlockPos p_76066_, BlockState p_76067_)
    {
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap;

        if (!p_76065_.getBlock().hasDynamicShape() && !p_76067_.getBlock().hasDynamicShape())
        {
            object2bytelinkedopenhashmap = OCCLUSION_CACHE.get();
        }
        else
        {
            object2bytelinkedopenhashmap = null;
        }

        Block.BlockStatePairKey block$blockstatepairkey;

        if (object2bytelinkedopenhashmap != null)
        {
            block$blockstatepairkey = new Block.BlockStatePairKey(p_76065_, p_76067_, p_76062_);
            byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$blockstatepairkey);

            if (b0 != 127)
            {
                return b0 != 0;
            }
        }
        else
        {
            block$blockstatepairkey = null;
        }

        VoxelShape voxelshape1 = p_76065_.getCollisionShape(p_76063_, p_76064_);
        VoxelShape voxelshape = p_76067_.getCollisionShape(p_76063_, p_76066_);
        boolean flag = !Shapes.mergedFaceOccludes(voxelshape1, voxelshape, p_76062_);

        if (object2bytelinkedopenhashmap != null)
        {
            if (object2bytelinkedopenhashmap.size() == 200)
            {
                object2bytelinkedopenhashmap.removeLastByte();
            }

            object2bytelinkedopenhashmap.putAndMoveToFirst(block$blockstatepairkey, (byte)(flag ? 1 : 0));
        }

        return flag;
    }

    public abstract Fluid getFlowing();

    public FluidState getFlowing(int pLevel, boolean pFalling)
    {
        return this.getFlowing().defaultFluidState().setValue(LEVEL, Integer.valueOf(pLevel)).setValue(FALLING, Boolean.valueOf(pFalling));
    }

    public abstract Fluid getSource();

    public FluidState getSource(boolean pFalling)
    {
        return this.getSource().defaultFluidState().setValue(FALLING, Boolean.valueOf(pFalling));
    }

    protected abstract boolean canConvertToSource();

    protected void spreadTo(LevelAccessor pLevel, BlockPos pPos, BlockState pBlockState, Direction pDirection, FluidState pFluidState)
    {
        if (pBlockState.getBlock() instanceof LiquidBlockContainer)
        {
            ((LiquidBlockContainer)pBlockState.getBlock()).placeLiquid(pLevel, pPos, pBlockState, pFluidState);
        }
        else
        {
            if (!pBlockState.isAir())
            {
                this.beforeDestroyingBlock(pLevel, pPos, pBlockState);
            }

            pLevel.setBlock(pPos, pFluidState.createLegacyBlock(), 3);
        }
    }

    protected abstract void beforeDestroyingBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState);

    private static short getCacheKey(BlockPos p_76059_, BlockPos p_76060_)
    {
        int i = p_76060_.getX() - p_76059_.getX();
        int j = p_76060_.getZ() - p_76059_.getZ();
        return (short)((i + 128 & 255) << 8 | j + 128 & 255);
    }

    protected int getSlopeDistance(LevelReader p_76027_, BlockPos p_76028_, int p_76029_, Direction p_76030_, BlockState p_76031_, BlockPos p_76032_, Short2ObjectMap<Pair<BlockState, FluidState>> p_76033_, Short2BooleanMap p_76034_)
    {
        int i = 1000;

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            if (direction != p_76030_)
            {
                BlockPos blockpos = p_76028_.relative(direction);
                short short1 = getCacheKey(p_76032_, blockpos);
                Pair<BlockState, FluidState> pair = p_76033_.computeIfAbsent(short1, (p_192916_) ->
                {
                    BlockState blockstate1 = p_76027_.getBlockState(blockpos);
                    return Pair.of(blockstate1, blockstate1.getFluidState());
                });
                BlockState blockstate = pair.getFirst();
                FluidState fluidstate = pair.getSecond();

                if (this.canPassThrough(p_76027_, this.getFlowing(), p_76028_, p_76031_, direction, blockpos, blockstate, fluidstate))
                {
                    boolean flag = p_76034_.computeIfAbsent(short1, (p_192912_) ->
                    {
                        BlockPos blockpos1 = blockpos.below();
                        BlockState blockstate1 = p_76027_.getBlockState(blockpos1);
                        return this.isWaterHole(p_76027_, this.getFlowing(), blockpos, blockstate, blockpos1, blockstate1);
                    });

                    if (flag)
                    {
                        return p_76029_;
                    }

                    if (p_76029_ < this.getSlopeFindDistance(p_76027_))
                    {
                        int j = this.getSlopeDistance(p_76027_, blockpos, p_76029_ + 1, direction.getOpposite(), blockstate, p_76032_, p_76033_, p_76034_);

                        if (j < i)
                        {
                            i = j;
                        }
                    }
                }
            }
        }

        return i;
    }

    private boolean isWaterHole(BlockGetter p_75957_, Fluid p_75958_, BlockPos p_75959_, BlockState p_75960_, BlockPos p_75961_, BlockState p_75962_)
    {
        if (!this.canPassThroughWall(Direction.DOWN, p_75957_, p_75959_, p_75960_, p_75961_, p_75962_))
        {
            return false;
        }
        else
        {
            return p_75962_.getFluidState().getType().isSame(this) ? true : this.canHoldFluid(p_75957_, p_75961_, p_75962_, p_75958_);
        }
    }

    private boolean canPassThrough(BlockGetter p_75964_, Fluid p_75965_, BlockPos p_75966_, BlockState p_75967_, Direction p_75968_, BlockPos p_75969_, BlockState p_75970_, FluidState p_75971_)
    {
        return !this.isSourceBlockOfThisType(p_75971_) && this.canPassThroughWall(p_75968_, p_75964_, p_75966_, p_75967_, p_75969_, p_75970_) && this.canHoldFluid(p_75964_, p_75969_, p_75970_, p_75965_);
    }

    private boolean isSourceBlockOfThisType(FluidState pState)
    {
        return pState.getType().isSame(this) && pState.isSource();
    }

    protected abstract int getSlopeFindDistance(LevelReader pLevel);

    private int sourceNeighborCount(LevelReader pLevel, BlockPos pPos)
    {
        int i = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            BlockPos blockpos = pPos.relative(direction);
            FluidState fluidstate = pLevel.getFluidState(blockpos);

            if (this.isSourceBlockOfThisType(fluidstate))
            {
                ++i;
            }
        }

        return i;
    }

    protected Map<Direction, FluidState> getSpread(LevelReader p_76080_, BlockPos p_76081_, BlockState p_76082_)
    {
        int i = 1000;
        Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
        Short2ObjectMap<Pair<BlockState, FluidState>> short2objectmap = new Short2ObjectOpenHashMap<>();
        Short2BooleanMap short2booleanmap = new Short2BooleanOpenHashMap();

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            BlockPos blockpos = p_76081_.relative(direction);
            short short1 = getCacheKey(p_76081_, blockpos);
            Pair<BlockState, FluidState> pair = short2objectmap.computeIfAbsent(short1, (p_192907_) ->
            {
                BlockState blockstate1 = p_76080_.getBlockState(blockpos);
                return Pair.of(blockstate1, blockstate1.getFluidState());
            });
            BlockState blockstate = pair.getFirst();
            FluidState fluidstate = pair.getSecond();
            FluidState fluidstate1 = this.getNewLiquid(p_76080_, blockpos, blockstate);

            if (this.canPassThrough(p_76080_, fluidstate1.getType(), p_76081_, p_76082_, direction, blockpos, blockstate, fluidstate))
            {
                BlockPos blockpos1 = blockpos.below();
                boolean flag = short2booleanmap.computeIfAbsent(short1, (p_192903_) ->
                {
                    BlockState blockstate1 = p_76080_.getBlockState(blockpos1);
                    return this.isWaterHole(p_76080_, this.getFlowing(), blockpos, blockstate, blockpos1, blockstate1);
                });
                int j;

                if (flag)
                {
                    j = 0;
                }
                else
                {
                    j = this.getSlopeDistance(p_76080_, blockpos, 1, direction.getOpposite(), blockstate, p_76081_, short2objectmap, short2booleanmap);
                }

                if (j < i)
                {
                    map.clear();
                }

                if (j <= i)
                {
                    map.put(direction, fluidstate1);
                    i = j;
                }
            }
        }

        return map;
    }

    private boolean canHoldFluid(BlockGetter pLevel, BlockPos pPos, BlockState pState, Fluid pFluid)
    {
        Block block = pState.getBlock();

        if (block instanceof LiquidBlockContainer)
        {
            return ((LiquidBlockContainer)block).canPlaceLiquid(pLevel, pPos, pState, pFluid);
        }
        else if (!(block instanceof DoorBlock) && !pState.is(BlockTags.SIGNS) && !pState.is(Blocks.LADDER) && !pState.is(Blocks.SUGAR_CANE) && !pState.is(Blocks.BUBBLE_COLUMN))
        {
            Material material = pState.getMaterial();

            if (material != Material.PORTAL && material != Material.STRUCTURAL_AIR && material != Material.WATER_PLANT && material != Material.REPLACEABLE_WATER_PLANT)
            {
                return !material.blocksMotion();
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    protected boolean canSpreadTo(BlockGetter pLevel, BlockPos pFromPos, BlockState pFromBlockState, Direction pDirection, BlockPos pToPos, BlockState pToBlockState, FluidState pToFluidState, Fluid pFluid)
    {
        return pToFluidState.canBeReplacedWith(pLevel, pToPos, pFluid, pDirection) && this.canPassThroughWall(pDirection, pLevel, pFromPos, pFromBlockState, pToPos, pToBlockState) && this.canHoldFluid(pLevel, pToPos, pToBlockState, pFluid);
    }

    protected abstract int getDropOff(LevelReader pLevel);

    protected int getSpreadDelay(Level p_75998_, BlockPos p_75999_, FluidState p_76000_, FluidState p_76001_)
    {
        return this.getTickDelay(p_75998_);
    }

    public void tick(Level pLevel, BlockPos pPos, FluidState pState)
    {
        if (!pState.isSource())
        {
            FluidState fluidstate = this.getNewLiquid(pLevel, pPos, pLevel.getBlockState(pPos));
            int i = this.getSpreadDelay(pLevel, pPos, pState, fluidstate);

            if (fluidstate.isEmpty())
            {
                pState = fluidstate;
                pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
            }
            else if (!fluidstate.equals(pState))
            {
                pState = fluidstate;
                BlockState blockstate = fluidstate.createLegacyBlock();
                pLevel.setBlock(pPos, blockstate, 2);
                pLevel.scheduleTick(pPos, fluidstate.getType(), i);
                pLevel.updateNeighborsAt(pPos, blockstate.getBlock());
            }
        }

        this.spread(pLevel, pPos, pState);
    }

    protected static int getLegacyLevel(FluidState pState)
    {
        return pState.isSource() ? 0 : 8 - Math.min(pState.getAmount(), 8) + (pState.getValue(FALLING) ? 8 : 0);
    }

    private static boolean hasSameAbove(FluidState p_76089_, BlockGetter p_76090_, BlockPos p_76091_)
    {
        return p_76089_.getType().isSame(p_76090_.getFluidState(p_76091_.above()).getType());
    }

    public float getHeight(FluidState p_76050_, BlockGetter p_76051_, BlockPos p_76052_)
    {
        return hasSameAbove(p_76050_, p_76051_, p_76052_) ? 1.0F : p_76050_.getOwnHeight();
    }

    public float getOwnHeight(FluidState p_76048_)
    {
        return (float)p_76048_.getAmount() / 9.0F;
    }

    public abstract int getAmount(FluidState pState);

    public VoxelShape getShape(FluidState p_76084_, BlockGetter p_76085_, BlockPos p_76086_)
    {
        return p_76084_.getAmount() == 9 && hasSameAbove(p_76084_, p_76085_, p_76086_) ? Shapes.block() : this.shapes.computeIfAbsent(p_76084_, (p_76073_) ->
        {
            return Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double)p_76073_.getHeight(p_76085_, p_76086_), 1.0D);
        });
    }
}
