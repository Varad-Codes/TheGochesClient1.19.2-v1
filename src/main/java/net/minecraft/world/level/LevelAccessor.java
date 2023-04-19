package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

public interface LevelAccessor extends CommonLevelAccessor, LevelTimeAccess
{
default long dayTime()
    {
        return this.getLevelData().getDayTime();
    }

    long nextSubTickCount();

    LevelTickAccess<Block> getBlockTicks();

    private <T> ScheduledTick<T> createTick(BlockPos p_186483_, T p_186484_, int p_186485_, TickPriority p_186486_)
    {
        return new ScheduledTick<T>(p_186484_, p_186483_,((long) this.getLevelData().getGameTime() + p_186485_), p_186486_, this.nextSubTickCount()); 
    }

    private <T> ScheduledTick<T> createTick(BlockPos p_186479_, T p_186480_, int p_186481_)
    {
        return new ScheduledTick(p_186480_, p_186479_, this.getLevelData().getGameTime() + (long)p_186481_, this.nextSubTickCount());
    }

default void scheduleTick(BlockPos p_186465_, Block p_186466_, int p_186467_, TickPriority p_186468_)
    {
        this.getBlockTicks().schedule(this.createTick(p_186465_, p_186466_, p_186467_, p_186468_));
    }

default void scheduleTick(BlockPos p_186461_, Block p_186462_, int p_186463_)
    {
        this.getBlockTicks().schedule(this.createTick(p_186461_, p_186462_, p_186463_));
    }

    LevelTickAccess<Fluid> getFluidTicks();

default void scheduleTick(BlockPos p_186474_, Fluid p_186475_, int p_186476_, TickPriority p_186477_)
    {
        this.getFluidTicks().schedule(this.createTick(p_186474_, p_186475_, p_186476_, p_186477_));
    }

default void scheduleTick(BlockPos p_186470_, Fluid p_186471_, int p_186472_)
    {
        this.getFluidTicks().schedule(this.createTick(p_186470_, p_186471_, p_186472_));
    }

    LevelData getLevelData();

    DifficultyInstance getCurrentDifficultyAt(BlockPos pPos);

    @Nullable
    MinecraftServer getServer();

default Difficulty getDifficulty()
    {
        return this.getLevelData().getDifficulty();
    }

    ChunkSource getChunkSource();

default boolean hasChunk(int pChunkX, int pChunkZ)
    {
        return this.getChunkSource().hasChunk(pChunkX, pChunkZ);
    }

    RandomSource getRandom();

default void blockUpdated(BlockPos pPos, Block pBlock)
    {
    }

default void neighborShapeChanged(Direction p_220411_, BlockState p_220412_, BlockPos p_220413_, BlockPos p_220414_, int p_220415_, int p_220416_)
    {
        NeighborUpdater.executeShapeUpdate(this, p_220411_, p_220412_, p_220413_, p_220414_, p_220415_, p_220416_ - 1);
    }

    void playSound(@Nullable Player pPlayer, BlockPos pPos, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch);

    void addParticle(ParticleOptions pParticleData, double pX, double p_46785_, double pY, double p_46787_, double pZ, double p_46789_);

    void levelEvent(@Nullable Player pPlayer, int pType, BlockPos pPos, int pData);

default void levelEvent(int pType, BlockPos pPos, int pData)
    {
        this.levelEvent((Player)null, pType, pPos, pData);
    }

    void gameEvent(GameEvent pEntity, Vec3 pEvent, GameEvent.Context pPos);

default void gameEvent(@Nullable Entity pEntity, GameEvent pEvent, Vec3 pPos)
    {
        this.gameEvent(pEvent, pPos, new GameEvent.Context(pEntity, (BlockState)null));
    }

default void gameEvent(@Nullable Entity pEntity, GameEvent pEvent, BlockPos pPos)
    {
        this.gameEvent(pEvent, pPos, new GameEvent.Context(pEntity, (BlockState)null));
    }

default void gameEvent(GameEvent pEntity, BlockPos pEvent, GameEvent.Context pPos)
    {
        this.gameEvent(pEntity, Vec3.atCenterOf(pEvent), pPos);
    }
}
