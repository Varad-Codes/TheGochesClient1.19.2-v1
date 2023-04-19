package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class MultifaceBlock extends Block
{
    private static final float AABB_OFFSET = 1.0F;
    private static final VoxelShape UP_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape DOWN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
    private static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
    private static final VoxelShape EAST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    private static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
    private static final Map<Direction, VoxelShape> SHAPE_BY_DIRECTION = Util.make(Maps.newEnumMap(Direction.class), (p_153923_) ->
    {
        p_153923_.put(Direction.NORTH, NORTH_AABB);
        p_153923_.put(Direction.EAST, EAST_AABB);
        p_153923_.put(Direction.SOUTH, SOUTH_AABB);
        p_153923_.put(Direction.WEST, WEST_AABB);
        p_153923_.put(Direction.UP, UP_AABB);
        p_153923_.put(Direction.DOWN, DOWN_AABB);
    });
    protected static final Direction[] DIRECTIONS = Direction.values();
    private final ImmutableMap<BlockState, VoxelShape> shapesCache;
    private final boolean canRotate;
    private final boolean canMirrorX;
    private final boolean canMirrorZ;

    public MultifaceBlock(BlockBehaviour.Properties p_153822_)
    {
        super(p_153822_);
        this.registerDefaultState(getDefaultMultifaceState(this.stateDefinition));
        this.shapesCache = this.getShapeForEachState(MultifaceBlock::calculateMultifaceShape);
        this.canRotate = Direction.Plane.HORIZONTAL.stream().allMatch(this::isFaceSupported);
        this.canMirrorX = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::isFaceSupported).count() % 2L == 0L;
        this.canMirrorZ = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::isFaceSupported).count() % 2L == 0L;
    }

    public static Set<Direction> availableFaces(BlockState p_221585_)
    {
        if (!(p_221585_.getBlock() instanceof MultifaceBlock))
        {
            return Set.of();
        }
        else
        {
            Set<Direction> set = EnumSet.noneOf(Direction.class);

            for (Direction direction : Direction.values())
            {
                if (hasFace(p_221585_, direction))
                {
                    set.add(direction);
                }
            }

            return set;
        }
    }

    public static Set<Direction> unpack(byte p_221570_)
    {
        Set<Direction> set = EnumSet.noneOf(Direction.class);

        for (Direction direction : Direction.values())
        {
            if ((p_221570_ & (byte)(1 << direction.ordinal())) > 0)
            {
                set.add(direction);
            }
        }

        return set;
    }

    public static byte pack(Collection<Direction> p_221577_)
    {
        byte b0 = 0;

        for (Direction direction : p_221577_)
        {
            b0 = (byte)(b0 | 1 << direction.ordinal());
        }

        return b0;
    }

    protected boolean isFaceSupported(Direction p_153921_)
    {
        return true;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        for (Direction direction : DIRECTIONS)
        {
            if (this.isFaceSupported(direction))
            {
                pBuilder.a(getFaceProperty(direction));
            }
        }
    }

    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos)
    {
        if (!hasAnyFace(pState))
        {
            return Blocks.AIR.defaultBlockState();
        }
        else
        {
            return hasFace(pState, pDirection) && !canAttachTo(pLevel, pDirection, pNeighborPos, pNeighborState) ? removeFace(pState, getFaceProperty(pDirection)) : pState;
        }
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return this.shapesCache.get(pState);
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        boolean flag = false;

        for (Direction direction : DIRECTIONS)
        {
            if (hasFace(pState, direction))
            {
                BlockPos blockpos = pPos.relative(direction);

                if (!canAttachTo(pLevel, direction, blockpos, pLevel.getBlockState(blockpos)))
                {
                    return false;
                }

                flag = true;
            }
        }

        return flag;
    }

    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext)
    {
        return hasAnyVacantFace(pState);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        return Arrays.stream(pContext.getNearestLookingDirections()).map((p_153865_) ->
        {
            return this.getStateForPlacement(blockstate, level, blockpos, p_153865_);
        }).filter(Objects::nonNull).findFirst().orElse((BlockState)null);
    }

    public boolean isValidStateForPlacement(BlockGetter p_221572_, BlockState p_221573_, BlockPos p_221574_, Direction p_221575_)
    {
        if (this.isFaceSupported(p_221575_) && (!p_221573_.is(this) || !hasFace(p_221573_, p_221575_)))
        {
            BlockPos blockpos = p_221574_.relative(p_221575_);
            return canAttachTo(p_221572_, p_221575_, blockpos, p_221572_.getBlockState(blockpos));
        }
        else
        {
            return false;
        }
    }

    @Nullable
    public BlockState getStateForPlacement(BlockState pCurrentState, BlockGetter pLevel, BlockPos pPos, Direction pLookingDirection)
    {
        if (!this.isValidStateForPlacement(pLevel, pCurrentState, pPos, pLookingDirection))
        {
            return null;
        }
        else
        {
            BlockState blockstate;

            if (pCurrentState.is(this))
            {
                blockstate = pCurrentState;
            }
            else if (this.isWaterloggable() && pCurrentState.getFluidState().isSourceOfType(Fluids.WATER))
            {
                blockstate = this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
            }
            else
            {
                blockstate = this.defaultBlockState();
            }

            return blockstate.setValue(getFaceProperty(pLookingDirection), Boolean.valueOf(true));
        }
    }

    public BlockState rotate(BlockState pState, Rotation pRotation)
    {
        return !this.canRotate ? pState : this.mapDirections(pState, pRotation::rotate);
    }

    public BlockState mirror(BlockState pState, Mirror pMirror)
    {
        if (pMirror == Mirror.FRONT_BACK && !this.canMirrorX)
        {
            return pState;
        }
        else
        {
            return pMirror == Mirror.LEFT_RIGHT && !this.canMirrorZ ? pState : this.mapDirections(pState, pMirror::mirror);
        }
    }

    private BlockState mapDirections(BlockState p_153911_, Function<Direction, Direction> p_153912_)
    {
        BlockState blockstate = p_153911_;

        for (Direction direction : DIRECTIONS)
        {
            if (this.isFaceSupported(direction))
            {
                blockstate = blockstate.setValue(getFaceProperty(p_153912_.apply(direction)), p_153911_.getValue(getFaceProperty(direction)));
            }
        }

        return blockstate;
    }

    public static boolean hasFace(BlockState p_153901_, Direction p_153902_)
    {
        BooleanProperty booleanproperty = getFaceProperty(p_153902_);
        return p_153901_.hasProperty(booleanproperty) && p_153901_.getValue(booleanproperty);
    }

    public static boolean canAttachTo(BlockGetter p_153830_, Direction p_153831_, BlockPos p_153832_, BlockState p_153833_)
    {
        return Block.isFaceFull(p_153833_.getBlockSupportShape(p_153830_, p_153832_), p_153831_.getOpposite()) || Block.isFaceFull(p_153833_.getCollisionShape(p_153830_, p_153832_), p_153831_.getOpposite());
    }

    private boolean isWaterloggable()
    {
        return this.stateDefinition.getProperties().contains(BlockStateProperties.WATERLOGGED);
    }

    private static BlockState removeFace(BlockState p_153898_, BooleanProperty p_153899_)
    {
        BlockState blockstate = p_153898_.setValue(p_153899_, Boolean.valueOf(false));
        return hasAnyFace(blockstate) ? blockstate : Blocks.AIR.defaultBlockState();
    }

    public static BooleanProperty getFaceProperty(Direction p_153934_)
    {
        return PROPERTY_BY_DIRECTION.get(p_153934_);
    }

    private static BlockState getDefaultMultifaceState(StateDefinition<Block, BlockState> p_153919_)
    {
        BlockState blockstate = p_153919_.any();

        for (BooleanProperty booleanproperty : PROPERTY_BY_DIRECTION.values())
        {
            if (blockstate.hasProperty(booleanproperty))
            {
                blockstate = blockstate.setValue(booleanproperty, Boolean.valueOf(false));
            }
        }

        return blockstate;
    }

    private static VoxelShape calculateMultifaceShape(BlockState p_153959_)
    {
        VoxelShape voxelshape = Shapes.empty();

        for (Direction direction : DIRECTIONS)
        {
            if (hasFace(p_153959_, direction))
            {
                voxelshape = Shapes.or(voxelshape, SHAPE_BY_DIRECTION.get(direction));
            }
        }

        return voxelshape.isEmpty() ? Shapes.block() : voxelshape;
    }

    protected static boolean hasAnyFace(BlockState p_153961_)
    {
        return Arrays.stream(DIRECTIONS).anyMatch((p_221583_) ->
        {
            return hasFace(p_153961_, p_221583_);
        });
    }

    private static boolean hasAnyVacantFace(BlockState p_153963_)
    {
        return Arrays.stream(DIRECTIONS).anyMatch((p_221580_) ->
        {
            return !hasFace(p_153963_, p_221580_);
        });
    }

    public abstract MultifaceSpreader getSpreader();
}
