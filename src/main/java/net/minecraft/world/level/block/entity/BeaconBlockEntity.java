package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public class BeaconBlockEntity extends BlockEntity implements MenuProvider
{
    private static final int MAX_LEVELS = 4;
    public static final MobEffect[][] BEACON_EFFECTS = new MobEffect[][] {{MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED}, {MobEffects.DAMAGE_RESISTANCE, MobEffects.JUMP}, {MobEffects.DAMAGE_BOOST}, {MobEffects.REGENERATION}};
    private static final Set<MobEffect> VALID_EFFECTS = Arrays.stream(BEACON_EFFECTS).flatMap(Arrays::stream).collect(Collectors.toSet());
    public static final int DATA_LEVELS = 0;
    public static final int DATA_PRIMARY = 1;
    public static final int DATA_SECONDARY = 2;
    public static final int NUM_DATA_VALUES = 3;
    private static final int BLOCKS_CHECK_PER_TICK = 10;
    List<BeaconBlockEntity.BeaconBeamSection> beamSections = Lists.newArrayList();
    private List<BeaconBlockEntity.BeaconBeamSection> checkingBeamSections = Lists.newArrayList();
    int levels;
    private int lastCheckY;
    @Nullable
    MobEffect primaryPower;
    @Nullable
    MobEffect secondaryPower;
    @Nullable
    private Component name;
    private LockCode lockKey = LockCode.NO_LOCK;
    private final ContainerData dataAccess = new ContainerData()
    {
        public int get(int p_58711_)
        {
            int i;

            switch (p_58711_)
            {
                case 0:
                    i = BeaconBlockEntity.this.levels;
                    break;

                case 1:
                    i = MobEffect.getIdFromNullable(BeaconBlockEntity.this.primaryPower);
                    break;

                case 2:
                    i = MobEffect.getIdFromNullable(BeaconBlockEntity.this.secondaryPower);
                    break;

                default:
                    i = 0;
            }

            return i;
        }
        public void set(int p_58713_, int p_58714_)
        {
            switch (p_58713_)
            {
                case 0:
                    BeaconBlockEntity.this.levels = p_58714_;
                    break;

                case 1:
                    if (!BeaconBlockEntity.this.level.isClientSide && !BeaconBlockEntity.this.beamSections.isEmpty())
                    {
                        BeaconBlockEntity.playSound(BeaconBlockEntity.this.level, BeaconBlockEntity.this.worldPosition, SoundEvents.BEACON_POWER_SELECT);
                    }

                    BeaconBlockEntity.this.primaryPower = BeaconBlockEntity.getValidEffectById(p_58714_);
                    break;

                case 2:
                    BeaconBlockEntity.this.secondaryPower = BeaconBlockEntity.getValidEffectById(p_58714_);
            }
        }
        public int getCount()
        {
            return 3;
        }
    };

    public BeaconBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(BlockEntityType.BEACON, pWorldPosition, pBlockState);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, BeaconBlockEntity pBlockEntity)
    {
        int i = pPos.getX();
        int j = pPos.getY();
        int k = pPos.getZ();
        BlockPos blockpos;

        if (pBlockEntity.lastCheckY < j)
        {
            blockpos = pPos;
            pBlockEntity.checkingBeamSections = Lists.newArrayList();
            pBlockEntity.lastCheckY = pPos.getY() - 1;
        }
        else
        {
            blockpos = new BlockPos(i, pBlockEntity.lastCheckY + 1, k);
        }

        BeaconBlockEntity.BeaconBeamSection beaconblockentity$beaconbeamsection = pBlockEntity.checkingBeamSections.isEmpty() ? null : pBlockEntity.checkingBeamSections.get(pBlockEntity.checkingBeamSections.size() - 1);
        int l = pLevel.getHeight(Heightmap.Types.WORLD_SURFACE, i, k);

        for (int i1 = 0; i1 < 10 && blockpos.getY() <= l; ++i1)
        {
            BlockState blockstate = pLevel.getBlockState(blockpos);
            Block block = blockstate.getBlock();

            if (block instanceof BeaconBeamBlock)
            {
                float[] afloat = ((BeaconBeamBlock)block).getColor().getTextureDiffuseColors();

                if (pBlockEntity.checkingBeamSections.size() <= 1)
                {
                    beaconblockentity$beaconbeamsection = new BeaconBlockEntity.BeaconBeamSection(afloat);
                    pBlockEntity.checkingBeamSections.add(beaconblockentity$beaconbeamsection);
                }
                else if (beaconblockentity$beaconbeamsection != null)
                {
                    if (Arrays.equals(afloat, beaconblockentity$beaconbeamsection.color))
                    {
                        beaconblockentity$beaconbeamsection.increaseHeight();
                    }
                    else
                    {
                        beaconblockentity$beaconbeamsection = new BeaconBlockEntity.BeaconBeamSection(new float[] {(beaconblockentity$beaconbeamsection.color[0] + afloat[0]) / 2.0F, (beaconblockentity$beaconbeamsection.color[1] + afloat[1]) / 2.0F, (beaconblockentity$beaconbeamsection.color[2] + afloat[2]) / 2.0F});
                        pBlockEntity.checkingBeamSections.add(beaconblockentity$beaconbeamsection);
                    }
                }
            }
            else
            {
                if (beaconblockentity$beaconbeamsection == null || blockstate.getLightBlock(pLevel, blockpos) >= 15 && !blockstate.is(Blocks.BEDROCK))
                {
                    pBlockEntity.checkingBeamSections.clear();
                    pBlockEntity.lastCheckY = l;
                    break;
                }

                beaconblockentity$beaconbeamsection.increaseHeight();
            }

            blockpos = blockpos.above();
            ++pBlockEntity.lastCheckY;
        }

        int j1 = pBlockEntity.levels;

        if (pLevel.getGameTime() % 80L == 0L)
        {
            if (!pBlockEntity.beamSections.isEmpty())
            {
                pBlockEntity.levels = updateBase(pLevel, i, j, k);
            }

            if (pBlockEntity.levels > 0 && !pBlockEntity.beamSections.isEmpty())
            {
                applyEffects(pLevel, pPos, pBlockEntity.levels, pBlockEntity.primaryPower, pBlockEntity.secondaryPower);
                playSound(pLevel, pPos, SoundEvents.BEACON_AMBIENT);
            }
        }

        if (pBlockEntity.lastCheckY >= l)
        {
            pBlockEntity.lastCheckY = pLevel.getMinBuildHeight() - 1;
            boolean flag = j1 > 0;
            pBlockEntity.beamSections = pBlockEntity.checkingBeamSections;

            if (!pLevel.isClientSide)
            {
                boolean flag1 = pBlockEntity.levels > 0;

                if (!flag && flag1)
                {
                    playSound(pLevel, pPos, SoundEvents.BEACON_ACTIVATE);

                    for (ServerPlayer serverplayer : pLevel.getEntitiesOfClass(ServerPlayer.class, (new AABB((double)i, (double)j, (double)k, (double)i, (double)(j - 4), (double)k)).inflate(10.0D, 5.0D, 10.0D)))
                    {
                        CriteriaTriggers.CONSTRUCT_BEACON.trigger(serverplayer, pBlockEntity.levels);
                    }
                }
                else if (flag && !flag1)
                {
                    playSound(pLevel, pPos, SoundEvents.BEACON_DEACTIVATE);
                }
            }
        }
    }

    private static int updateBase(Level p_155093_, int p_155094_, int p_155095_, int p_155096_)
    {
        int i = 0;

        for (int j = 1; j <= 4; i = j++)
        {
            int k = p_155095_ - j;

            if (k < p_155093_.getMinBuildHeight())
            {
                break;
            }

            boolean flag = true;

            for (int l = p_155094_ - j; l <= p_155094_ + j && flag; ++l)
            {
                for (int i1 = p_155096_ - j; i1 <= p_155096_ + j; ++i1)
                {
                    if (!p_155093_.getBlockState(new BlockPos(l, k, i1)).is(BlockTags.BEACON_BASE_BLOCKS))
                    {
                        flag = false;
                        break;
                    }
                }
            }

            if (!flag)
            {
                break;
            }
        }

        return i;
    }

    public void setRemoved()
    {
        playSound(this.level, this.worldPosition, SoundEvents.BEACON_DEACTIVATE);
        super.setRemoved();
    }

    private static void applyEffects(Level p_155098_, BlockPos p_155099_, int p_155100_, @Nullable MobEffect p_155101_, @Nullable MobEffect p_155102_)
    {
        if (!p_155098_.isClientSide && p_155101_ != null)
        {
            double d0 = (double)(p_155100_ * 10 + 10);
            int i = 0;

            if (p_155100_ >= 4 && p_155101_ == p_155102_)
            {
                i = 1;
            }

            int j = (9 + p_155100_ * 2) * 20;
            AABB aabb = (new AABB(p_155099_)).inflate(d0).expandTowards(0.0D, (double)p_155098_.getHeight(), 0.0D);
            List<Player> list = p_155098_.getEntitiesOfClass(Player.class, aabb);

            for (Player player : list)
            {
                player.addEffect(new MobEffectInstance(p_155101_, j, i, true, true));
            }

            if (p_155100_ >= 4 && p_155101_ != p_155102_ && p_155102_ != null)
            {
                for (Player player1 : list)
                {
                    player1.addEffect(new MobEffectInstance(p_155102_, j, 0, true, true));
                }
            }
        }
    }

    public static void playSound(Level pLevel, BlockPos pPos, SoundEvent pSound)
    {
        pLevel.playSound((Player)null, pPos, pSound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public List<BeaconBlockEntity.BeaconBeamSection> getBeamSections()
    {
        return (List<BeaconBlockEntity.BeaconBeamSection>)(this.levels == 0 ? ImmutableList.of() : this.beamSections);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag()
    {
        return this.saveWithoutMetadata();
    }

    @Nullable
    static MobEffect getValidEffectById(int pEffectId)
    {
        MobEffect mobeffect = MobEffect.byId(pEffectId);
        return VALID_EFFECTS.contains(mobeffect) ? mobeffect : null;
    }

    public void load(CompoundTag pTag)
    {
        super.load(pTag);
        this.primaryPower = getValidEffectById(pTag.getInt("Primary"));
        this.secondaryPower = getValidEffectById(pTag.getInt("Secondary"));

        if (pTag.contains("CustomName", 8))
        {
            this.name = Component.Serializer.fromJson(pTag.getString("CustomName"));
        }

        this.lockKey = LockCode.fromTag(pTag);
    }

    protected void saveAdditional(CompoundTag p_187463_)
    {
        super.saveAdditional(p_187463_);
        p_187463_.putInt("Primary", MobEffect.getIdFromNullable(this.primaryPower));
        p_187463_.putInt("Secondary", MobEffect.getIdFromNullable(this.secondaryPower));
        p_187463_.putInt("Levels", this.levels);

        if (this.name != null)
        {
            p_187463_.putString("CustomName", Component.Serializer.toJson(this.name));
        }

        this.lockKey.addToTag(p_187463_);
    }

    public void setCustomName(@Nullable Component pName)
    {
        this.name = pName;
    }

    @Nullable
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer)
    {
        return BaseContainerBlockEntity.canUnlock(pPlayer, this.lockKey, this.getDisplayName()) ? new BeaconMenu(pContainerId, pInventory, this.dataAccess, ContainerLevelAccess.create(this.level, this.getBlockPos())) : null;
    }

    public Component getDisplayName()
    {
        return (Component)(this.name != null ? this.name : Component.translatable("container.beacon"));
    }

    public void setLevel(Level pLevel)
    {
        super.setLevel(pLevel);
        this.lastCheckY = pLevel.getMinBuildHeight() - 1;
    }

    public static class BeaconBeamSection
    {
        final float[] color;
        private int height;

        public BeaconBeamSection(float[] pColor)
        {
            this.color = pColor;
            this.height = 1;
        }

        protected void increaseHeight()
        {
            ++this.height;
        }

        public float[] getColor()
        {
            return this.color;
        }

        public int getHeight()
        {
            return this.height;
        }
    }
}
