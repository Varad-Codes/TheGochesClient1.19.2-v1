package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Piglin extends AbstractPiglin implements CrossbowAttackMob, InventoryCarrier
{
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_DANCING = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("766bfa64-11f3-11ea-8d71-362b9e155667");
    private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(SPEED_MODIFIER_BABY_UUID, "Baby speed boost", (double)0.2F, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final int MAX_HEALTH = 16;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.35F;
    private static final int ATTACK_DAMAGE = 5;
    private static final float CROSSBOW_POWER = 1.6F;
    private static final float CHANCE_OF_WEARING_EACH_ARMOUR_ITEM = 0.1F;
    private static final int MAX_PASSENGERS_ON_ONE_HOGLIN = 3;
    private static final float PROBABILITY_OF_SPAWNING_AS_BABY = 0.2F;
    private static final float BABY_EYE_HEIGHT_ADJUSTMENT = 0.81F;
    private static final double PROBABILITY_OF_SPAWNING_WITH_CROSSBOW_INSTEAD_OF_SWORD = 0.5D;
    private final SimpleContainer inventory = new SimpleContainer(8);
    private boolean cannotHunt;
    protected static final ImmutableList < SensorType <? extends Sensor <? super Piglin >>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.PIGLIN_SPECIFIC_SENSOR);
    protected static final ImmutableList < MemoryModuleType<? >> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.PATH, MemoryModuleType.ANGRY_AT, MemoryModuleType.UNIVERSAL_ANGER, MemoryModuleType.AVOID_TARGET, MemoryModuleType.ADMIRING_ITEM, MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, MemoryModuleType.ADMIRING_DISABLED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryModuleType.CELEBRATE_LOCATION, MemoryModuleType.DANCING, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.RIDE_TARGET, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.NEAREST_REPELLENT);

    public Piglin(EntityType <? extends AbstractPiglin > p_34683_, Level p_34684_)
    {
        super(p_34683_, p_34684_);
        this.xpReward = 5;
    }

    public void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);

        if (this.isBaby())
        {
            pCompound.putBoolean("IsBaby", true);
        }

        if (this.cannotHunt)
        {
            pCompound.putBoolean("CannotHunt", true);
        }

        pCompound.put("Inventory", this.inventory.createTag());
    }

    public void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);
        this.setBaby(pCompound.getBoolean("IsBaby"));
        this.setCannotHunt(pCompound.getBoolean("CannotHunt"));
        this.inventory.fromTag(pCompound.getList("Inventory", 10));
    }

    @VisibleForDebug
    public SimpleContainer getInventory()
    {
        return this.inventory;
    }

    protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit)
    {
        super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
        this.inventory.removeAllItems().forEach(this::spawnAtLocation);
    }

    protected ItemStack addToInventory(ItemStack p_34779_)
    {
        return this.inventory.addItem(p_34779_);
    }

    protected boolean canAddToInventory(ItemStack p_34781_)
    {
        return this.inventory.canAddItem(p_34781_);
    }

    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_BABY_ID, false);
        this.entityData.define(DATA_IS_CHARGING_CROSSBOW, false);
        this.entityData.define(DATA_IS_DANCING, false);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey)
    {
        super.onSyncedDataUpdated(pKey);

        if (DATA_BABY_ID.equals(pKey))
        {
            this.refreshDimensions();
        }
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0D).add(Attributes.MOVEMENT_SPEED, (double)0.35F).add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    public static boolean checkPiglinSpawnRules(EntityType<Piglin> p_219198_, LevelAccessor p_219199_, MobSpawnType p_219200_, BlockPos p_219201_, RandomSource p_219202_)
    {
        return !p_219199_.getBlockState(p_219201_.below()).is(Blocks.NETHER_WART_BLOCK);
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag)
    {
        RandomSource randomsource = pLevel.getRandom();

        if (pReason != MobSpawnType.STRUCTURE)
        {
            if (randomsource.nextFloat() < 0.2F)
            {
                this.setBaby(true);
            }
            else if (this.isAdult())
            {
                this.setItemSlot(EquipmentSlot.MAINHAND, this.createSpawnWeapon());
            }
        }

        PiglinAi.initMemories(this, pLevel.getRandom());
        this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
        this.populateDefaultEquipmentEnchantments(randomsource, pDifficulty);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    protected boolean shouldDespawnInPeaceful()
    {
        return false;
    }

    public boolean removeWhenFarAway(double pDistanceToClosestPlayer)
    {
        return !this.isPersistenceRequired();
    }

    protected void populateDefaultEquipmentSlots(RandomSource p_219189_, DifficultyInstance p_219190_)
    {
        if (this.isAdult())
        {
            this.maybeWearArmor(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET), p_219189_);
            this.maybeWearArmor(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE), p_219189_);
            this.maybeWearArmor(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS), p_219189_);
            this.maybeWearArmor(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS), p_219189_);
        }
    }

    private void maybeWearArmor(EquipmentSlot p_219192_, ItemStack p_219193_, RandomSource p_219194_)
    {
        if (p_219194_.nextFloat() < 0.1F)
        {
            this.setItemSlot(p_219192_, p_219193_);
        }
    }

    protected Brain.Provider<Piglin> brainProvider()
    {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected Brain<?> makeBrain(Dynamic<?> pDynamic)
    {
        return PiglinAi.makeBrain(this, this.brainProvider().makeBrain(pDynamic));
    }

    public Brain<Piglin> getBrain()
    {
        return (Brain<Piglin>)super.getBrain();
    }

    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand)
    {
        InteractionResult interactionresult = super.mobInteract(pPlayer, pHand);

        if (interactionresult.consumesAction())
        {
            return interactionresult;
        }
        else if (!this.level.isClientSide)
        {
            return PiglinAi.mobInteract(this, pPlayer, pHand);
        }
        else
        {
            boolean flag = PiglinAi.canAdmire(this, pPlayer.getItemInHand(pHand)) && this.getArmPose() != PiglinArmPose.ADMIRING_ITEM;
            return flag ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
    }

    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize)
    {
        return this.isBaby() ? 0.93F : 1.74F;
    }

    public double getPassengersRidingOffset()
    {
        return (double)this.getBbHeight() * 0.92D;
    }

    public void setBaby(boolean pChildZombie)
    {
        this.getEntityData().set(DATA_BABY_ID, pChildZombie);

        if (!this.level.isClientSide)
        {
            AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            attributeinstance.removeModifier(SPEED_MODIFIER_BABY);

            if (pChildZombie)
            {
                attributeinstance.addTransientModifier(SPEED_MODIFIER_BABY);
            }
        }
    }

    public boolean isBaby()
    {
        return this.getEntityData().get(DATA_BABY_ID);
    }

    private void setCannotHunt(boolean p_34792_)
    {
        this.cannotHunt = p_34792_;
    }

    protected boolean canHunt()
    {
        return !this.cannotHunt;
    }

    protected void customServerAiStep()
    {
        this.level.getProfiler().push("piglinBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        PiglinAi.updateActivity(this);
        super.customServerAiStep();
    }

    public int getExperienceReward()
    {
        return this.xpReward;
    }

    protected void finishConversion(ServerLevel p_34756_)
    {
        PiglinAi.cancelAdmiring(this);
        this.inventory.removeAllItems().forEach(this::spawnAtLocation);
        super.finishConversion(p_34756_);
    }

    private ItemStack createSpawnWeapon()
    {
        return (double)this.random.nextFloat() < 0.5D ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD);
    }

    private boolean isChargingCrossbow()
    {
        return this.entityData.get(DATA_IS_CHARGING_CROSSBOW);
    }

    public void setChargingCrossbow(boolean pIsCharging)
    {
        this.entityData.set(DATA_IS_CHARGING_CROSSBOW, pIsCharging);
    }

    public void onCrossbowAttackPerformed()
    {
        this.noActionTime = 0;
    }

    public PiglinArmPose getArmPose()
    {
        if (this.isDancing())
        {
            return PiglinArmPose.DANCING;
        }
        else if (PiglinAi.isLovedItem(this.getOffhandItem()))
        {
            return PiglinArmPose.ADMIRING_ITEM;
        }
        else if (this.isAggressive() && this.isHoldingMeleeWeapon())
        {
            return PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON;
        }
        else if (this.isChargingCrossbow())
        {
            return PiglinArmPose.CROSSBOW_CHARGE;
        }
        else
        {
            return this.isAggressive() && this.isHolding(Items.CROSSBOW) ? PiglinArmPose.CROSSBOW_HOLD : PiglinArmPose.DEFAULT;
        }
    }

    public boolean isDancing()
    {
        return this.entityData.get(DATA_IS_DANCING);
    }

    public void setDancing(boolean p_34790_)
    {
        this.entityData.set(DATA_IS_DANCING, p_34790_);
    }

    public boolean hurt(DamageSource pSource, float pAmount)
    {
        boolean flag = super.hurt(pSource, pAmount);

        if (this.level.isClientSide)
        {
            return false;
        }
        else
        {
            if (flag && pSource.getEntity() instanceof LivingEntity)
            {
                PiglinAi.wasHurtBy(this, (LivingEntity)pSource.getEntity());
            }

            return flag;
        }
    }

    public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor)
    {
        this.performCrossbowAttack(this, 1.6F);
    }

    public void shootCrossbowProjectile(LivingEntity p_34707_, ItemStack p_34708_, Projectile p_34709_, float p_34710_)
    {
        this.shootCrossbowProjectile(this, p_34707_, p_34709_, p_34710_, 1.6F);
    }

    public boolean canFireProjectileWeapon(ProjectileWeaponItem p_34715_)
    {
        return p_34715_ == Items.CROSSBOW;
    }

    protected void holdInMainHand(ItemStack p_34784_)
    {
        this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, p_34784_);
    }

    protected void holdInOffHand(ItemStack p_34786_)
    {
        if (p_34786_.is(PiglinAi.BARTERING_ITEM))
        {
            this.setItemSlot(EquipmentSlot.OFFHAND, p_34786_);
            this.setGuaranteedDrop(EquipmentSlot.OFFHAND);
        }
        else
        {
            this.setItemSlotAndDropWhenKilled(EquipmentSlot.OFFHAND, p_34786_);
        }
    }

    public boolean wantsToPickUp(ItemStack p_34777_)
    {
        return this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.canPickUpLoot() && PiglinAi.wantsToPickup(this, p_34777_);
    }

    protected boolean canReplaceCurrentItem(ItemStack p_34788_)
    {
        EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(p_34788_);
        ItemStack itemstack = this.getItemBySlot(equipmentslot);
        return this.canReplaceCurrentItem(p_34788_, itemstack);
    }

    protected boolean canReplaceCurrentItem(ItemStack pCandidate, ItemStack pExisting)
    {
        if (EnchantmentHelper.hasBindingCurse(pExisting))
        {
            return false;
        }
        else
        {
            boolean flag = PiglinAi.isLovedItem(pCandidate) || pCandidate.is(Items.CROSSBOW);
            boolean flag1 = PiglinAi.isLovedItem(pExisting) || pExisting.is(Items.CROSSBOW);

            if (flag && !flag1)
            {
                return true;
            }
            else if (!flag && flag1)
            {
                return false;
            }
            else
            {
                return this.isAdult() && !pCandidate.is(Items.CROSSBOW) && pExisting.is(Items.CROSSBOW) ? false : super.canReplaceCurrentItem(pCandidate, pExisting);
            }
        }
    }

    protected void pickUpItem(ItemEntity pItemEntity)
    {
        this.onItemPickup(pItemEntity);
        PiglinAi.pickUpItem(this, pItemEntity);
    }

    public boolean startRiding(Entity pEntity, boolean pForce)
    {
        if (this.isBaby() && pEntity.getType() == EntityType.HOGLIN)
        {
            pEntity = this.getTopPassenger(pEntity, 3);
        }

        return super.startRiding(pEntity, pForce);
    }

    private Entity getTopPassenger(Entity p_34731_, int p_34732_)
    {
        List<Entity> list = p_34731_.getPassengers();
        return p_34732_ != 1 && !list.isEmpty() ? this.getTopPassenger(list.get(0), p_34732_ - 1) : p_34731_;
    }

    protected SoundEvent getAmbientSound()
    {
        return this.level.isClientSide ? null : PiglinAi.getSoundForCurrentActivity(this).orElse((SoundEvent)null);
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource)
    {
        return SoundEvents.PIGLIN_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.PIGLIN_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock)
    {
        this.playSound(SoundEvents.PIGLIN_STEP, 0.15F, 1.0F);
    }

    protected void playSoundEvent(SoundEvent p_219196_)
    {
        this.playSound(p_219196_, this.getSoundVolume(), this.getVoicePitch());
    }

    protected void playConvertedSound()
    {
        this.playSoundEvent(SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED);
    }
}
