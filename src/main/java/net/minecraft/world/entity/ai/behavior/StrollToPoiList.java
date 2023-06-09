package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;

public class StrollToPoiList extends Behavior<Villager>
{
    private final MemoryModuleType<List<GlobalPos>> strollToMemoryType;
    private final MemoryModuleType<GlobalPos> mustBeCloseToMemoryType;
    private final float speedModifier;
    private final int closeEnoughDist;
    private final int maxDistanceFromPoi;
    private long nextOkStartTime;
    @Nullable
    private GlobalPos targetPos;

    public StrollToPoiList(MemoryModuleType<List<GlobalPos>> p_24362_, float p_24363_, int p_24364_, int p_24365_, MemoryModuleType<GlobalPos> p_24366_)
    {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, p_24362_, MemoryStatus.VALUE_PRESENT, p_24366_, MemoryStatus.VALUE_PRESENT));
        this.strollToMemoryType = p_24362_;
        this.speedModifier = p_24363_;
        this.closeEnoughDist = p_24364_;
        this.maxDistanceFromPoi = p_24365_;
        this.mustBeCloseToMemoryType = p_24366_;
    }

    protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner)
    {
        Optional<List<GlobalPos>> optional = pOwner.getBrain().getMemory(this.strollToMemoryType);
        Optional<GlobalPos> optional1 = pOwner.getBrain().getMemory(this.mustBeCloseToMemoryType);

        if (optional.isPresent() && optional1.isPresent())
        {
            List<GlobalPos> list = optional.get();

            if (!list.isEmpty())
            {
                this.targetPos = list.get(pLevel.getRandom().nextInt(list.size()));
                return this.targetPos != null && pLevel.dimension() == this.targetPos.dimension() && optional1.get().pos().closerToCenterThan(pOwner.position(), (double)this.maxDistanceFromPoi);
            }
        }

        return false;
    }

    protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime)
    {
        if (pGameTime > this.nextOkStartTime && this.targetPos != null)
        {
            pEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPos.pos(), this.speedModifier, this.closeEnoughDist));
            this.nextOkStartTime = pGameTime + 100L;
        }
    }
}
