package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class EmptyLevelChunk extends LevelChunk
{
    private final Holder<Biome> biome;

    public EmptyLevelChunk(Level p_204422_, ChunkPos p_204423_, Holder<Biome> p_204424_)
    {
        super(p_204422_, p_204423_);
        this.biome = p_204424_;
    }

    public BlockState getBlockState(BlockPos pPos)
    {
        return Blocks.VOID_AIR.defaultBlockState();
    }

    @Nullable
    public BlockState setBlockState(BlockPos pPos, BlockState pState, boolean pIsMoving)
    {
        return null;
    }

    public FluidState getFluidState(BlockPos pPos)
    {
        return Fluids.EMPTY.defaultFluidState();
    }

    public int getLightEmission(BlockPos pPos)
    {
        return 0;
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pPos, LevelChunk.EntityCreationType pCreationType)
    {
        return null;
    }

    public void addAndRegisterBlockEntity(BlockEntity pBlockEntity)
    {
    }

    public void setBlockEntity(BlockEntity pBlockEntity)
    {
    }

    public void removeBlockEntity(BlockPos pPos)
    {
    }

    public boolean isEmpty()
    {
        return true;
    }

    public boolean isYSpaceEmpty(int pStartY, int pEndY)
    {
        return true;
    }

    public ChunkHolder.FullChunkStatus getFullStatus()
    {
        return ChunkHolder.FullChunkStatus.BORDER;
    }

    public Holder<Biome> getNoiseBiome(int pX, int pY, int pZ)
    {
        return this.biome;
    }
}
