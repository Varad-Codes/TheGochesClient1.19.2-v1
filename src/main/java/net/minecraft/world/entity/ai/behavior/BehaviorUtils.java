package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class BehaviorUtils
{
    private BehaviorUtils()
    {
    }

    public static void lockGazeAndWalkToEachOther(LivingEntity pFirstEntity, LivingEntity pSecondEntity, float pSpeed)
    {
        lookAtEachOther(pFirstEntity, pSecondEntity);
        setWalkAndLookTargetMemoriesToEachOther(pFirstEntity, pSecondEntity, pSpeed);
    }

    public static boolean entityIsVisible(Brain<?> pBrain, LivingEntity pTarget)
    {
        Optional<NearestVisibleLivingEntities> optional = pBrain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        return optional.isPresent() && optional.get().contains(pTarget);
    }

    public static boolean targetIsValid(Brain<?> pBrain, MemoryModuleType <? extends LivingEntity > pMemoryType, EntityType<?> pLivingPredicate)
    {
        return targetIsValid(pBrain, pMemoryType, (p_186022_) ->
        {
            return p_186022_.getType() == pLivingPredicate;
        });
    }

    private static boolean targetIsValid(Brain<?> pBrain, MemoryModuleType <? extends LivingEntity > pMemoryType, Predicate<LivingEntity> pLivingPredicate)
    {
        return pBrain.getMemory(pMemoryType).filter(pLivingPredicate).filter(LivingEntity::isAlive).filter((p_186037_) ->
        {
            return entityIsVisible(pBrain, p_186037_);
        }).isPresent();
    }

    private static void lookAtEachOther(LivingEntity pFirstEntity, LivingEntity pSecondEntity)
    {
        lookAtEntity(pFirstEntity, pSecondEntity);
        lookAtEntity(pSecondEntity, pFirstEntity);
    }

    public static void lookAtEntity(LivingEntity pEntity, LivingEntity pTarget)
    {
        pEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(pTarget, true));
    }

    private static void setWalkAndLookTargetMemoriesToEachOther(LivingEntity pFirstEntity, LivingEntity pSecondEntity, float pSpeed)
    {
        int i = 2;
        setWalkAndLookTargetMemories(pFirstEntity, pSecondEntity, pSpeed, 2);
        setWalkAndLookTargetMemories(pSecondEntity, pFirstEntity, pSpeed, 2);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity pLivingEntity, Entity pPos, float pSpeed, int pDistance)
    {
        setWalkAndLookTargetMemories(pLivingEntity, new EntityTracker(pPos, true), pSpeed, pDistance);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity pLivingEntity, BlockPos pPos, float pSpeed, int pDistance)
    {
        setWalkAndLookTargetMemories(pLivingEntity, new BlockPosTracker(pPos), pSpeed, pDistance);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity pLivingEntity, PositionTracker pPos, float pSpeed, int pDistance)
    {
        WalkTarget walktarget = new WalkTarget(pPos, pSpeed, pDistance);
        pLivingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, pPos);
        pLivingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walktarget);
    }

    public static void throwItem(LivingEntity pLivingEntity, ItemStack pStack, Vec3 pOffset)
    {
        Vec3 vec3 = new Vec3((double)0.3F, (double)0.3F, (double)0.3F);
        throwItem(pLivingEntity, pStack, pOffset, vec3, 0.3F);
    }

    public static void throwItem(LivingEntity p_217134_, ItemStack p_217135_, Vec3 p_217136_, Vec3 p_217137_, float p_217138_)
    {
        double d0 = p_217134_.getEyeY() - (double)p_217138_;
        ItemEntity itementity = new ItemEntity(p_217134_.level, p_217134_.getX(), d0, p_217134_.getZ(), p_217135_);
        itementity.setThrower(p_217134_.getUUID());
        Vec3 vec3 = p_217136_.subtract(p_217134_.position());
        vec3 = vec3.normalize().multiply(p_217137_.x, p_217137_.y, p_217137_.z);
        itementity.setDeltaMovement(vec3);
        itementity.setDefaultPickUpDelay();
        p_217134_.level.addFreshEntity(itementity);
    }

    public static SectionPos findSectionClosestToVillage(ServerLevel pServerLevel, SectionPos pSectionPos, int pRadius)
    {
        int i = pServerLevel.sectionsToVillage(pSectionPos);
        return SectionPos.cube(pSectionPos, pRadius).filter((p_186017_) ->
        {
            return pServerLevel.sectionsToVillage(p_186017_) < i;
        }).min(Comparator.comparingInt(pServerLevel::sectionsToVillage)).orElse(pSectionPos);
    }

    public static boolean isWithinAttackRange(Mob pMob, LivingEntity pTarget, int pCooldown)
    {
        Item item = pMob.getMainHandItem().getItem();

        if (item instanceof ProjectileWeaponItem projectileweaponitem)
        {
            if (pMob.canFireProjectileWeapon((ProjectileWeaponItem)item))
            {
                int i = projectileweaponitem.getDefaultProjectileRange() - pCooldown;
                return pMob.closerThan(pTarget, (double)i);
            }
        }

        return pMob.isWithinMeleeAttackRange(pTarget);
    }

    public static boolean isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(LivingEntity pLivingEntity, LivingEntity pTarget, double pDistance)
    {
        Optional<LivingEntity> optional = pLivingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);

        if (optional.isEmpty())
        {
            return false;
        }
        else
        {
            double d0 = pLivingEntity.distanceToSqr(optional.get().position());
            double d1 = pLivingEntity.distanceToSqr(pTarget.position());
            return d1 > d0 + pDistance * pDistance;
        }
    }

    public static boolean canSee(LivingEntity pLivingEntity, LivingEntity pTarget)
    {
        Brain<?> brain = pLivingEntity.getBrain();
        return !brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES) ? false : brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().contains(pTarget);
    }

    public static LivingEntity getNearestTarget(LivingEntity pCenterEntity, Optional<LivingEntity> pOptionalEntity, LivingEntity pLivingEntity)
    {
        return pOptionalEntity.isEmpty() ? pLivingEntity : getTargetNearestMe(pCenterEntity, pOptionalEntity.get(), pLivingEntity);
    }

    public static LivingEntity getTargetNearestMe(LivingEntity pCenterEntity, LivingEntity pLivingEntity1, LivingEntity pLivingEntity2)
    {
        Vec3 vec3 = pLivingEntity1.position();
        Vec3 vec31 = pLivingEntity2.position();
        return pCenterEntity.distanceToSqr(vec3) < pCenterEntity.distanceToSqr(vec31) ? pLivingEntity1 : pLivingEntity2;
    }

    public static Optional<LivingEntity> getLivingEntityFromUUIDMemory(LivingEntity pLivingEntity, MemoryModuleType<UUID> pTargetMemory)
    {
        Optional<UUID> optional = pLivingEntity.getBrain().getMemory(pTargetMemory);
        return optional.map((p_186027_) ->
        {
            return ((ServerLevel)pLivingEntity.level).getEntity(p_186027_);
        }).map((p_186019_) ->
        {
            LivingEntity livingentity1;

            if (p_186019_ instanceof LivingEntity livingentity)
            {
                livingentity1 = livingentity;
            }
            else {
                livingentity1 = null;
            }

            return livingentity1;
        });
    }

    public static Stream<Villager> getNearbyVillagersWithCondition(Villager pVillager, Predicate<Villager> pVillagerPredicate)
    {
        return pVillager.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).map((p_186034_) ->
        {
            return p_186034_.stream().filter((p_186030_) -> {
                return p_186030_ instanceof Villager && p_186030_ != pVillager;
            }).map((p_186024_) -> {
                return (Villager)p_186024_;
            }).filter(LivingEntity::isAlive).filter(pVillagerPredicate);
        }).orElseGet(Stream::empty);
    }

    @Nullable
    public static Vec3 getRandomSwimmablePos(PathfinderMob p_147445_, int p_147446_, int p_147447_)
    {
        Vec3 vec3 = DefaultRandomPos.getPos(p_147445_, p_147446_, p_147447_);

        for (int i = 0; vec3 != null && !p_147445_.level.getBlockState(new BlockPos(vec3)).isPathfindable(p_147445_.level, new BlockPos(vec3), PathComputationType.WATER) && i++ < 10; vec3 = DefaultRandomPos.getPos(p_147445_, p_147446_, p_147447_))
        {
        }

        return vec3;
    }

    public static boolean isBreeding(LivingEntity p_217127_)
    {
        return p_217127_.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }
}
