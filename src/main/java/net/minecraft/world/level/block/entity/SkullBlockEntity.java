package net.minecraft.world.level.block.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.Services;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SkullBlockEntity extends BlockEntity
{
    public static final String TAG_SKULL_OWNER = "SkullOwner";
    @Nullable
    private static GameProfileCache profileCache;
    @Nullable
    private static MinecraftSessionService sessionService;
    @Nullable
    private static Executor mainThreadExecutor;
    @Nullable
    private GameProfile owner;
    private int mouthTickCount;
    private boolean isMovingMouth;

    public SkullBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(BlockEntityType.SKULL, pWorldPosition, pBlockState);
    }

    public static void setup(Services p_222886_, Executor p_222887_)
    {
        profileCache = p_222886_.profileCache();
        sessionService = p_222886_.sessionService();
        mainThreadExecutor = p_222887_;
    }

    public static void clear()
    {
        profileCache = null;
        sessionService = null;
        mainThreadExecutor = null;
    }

    protected void saveAdditional(CompoundTag p_187518_)
    {
        super.saveAdditional(p_187518_);

        if (this.owner != null)
        {
            CompoundTag compoundtag = new CompoundTag();
            NbtUtils.writeGameProfile(compoundtag, this.owner);
            p_187518_.put("SkullOwner", compoundtag);
        }
    }

    public void load(CompoundTag pTag)
    {
        super.load(pTag);

        if (pTag.contains("SkullOwner", 10))
        {
            this.setOwner(NbtUtils.readGameProfile(pTag.getCompound("SkullOwner")));
        }
        else if (pTag.contains("ExtraType", 8))
        {
            String s = pTag.getString("ExtraType");

            if (!StringUtil.isNullOrEmpty(s))
            {
                this.setOwner(new GameProfile((UUID)null, s));
            }
        }
    }

    public static void dragonHeadAnimation(Level pLevel, BlockPos pPos, BlockState pState, SkullBlockEntity pBlockEntity)
    {
        if (pLevel.hasNeighborSignal(pPos))
        {
            pBlockEntity.isMovingMouth = true;
            ++pBlockEntity.mouthTickCount;
        }
        else
        {
            pBlockEntity.isMovingMouth = false;
        }
    }

    public float getMouthAnimation(float pPartialTicks)
    {
        return this.isMovingMouth ? (float)this.mouthTickCount + pPartialTicks : (float)this.mouthTickCount;
    }

    @Nullable
    public GameProfile getOwnerProfile()
    {
        return this.owner;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag()
    {
        return this.saveWithoutMetadata();
    }

    public void setOwner(@Nullable GameProfile pOwner)
    {
        synchronized (this)
        {
            this.owner = pOwner;
        }

        this.updateOwnerProfile();
    }

    private void updateOwnerProfile()
    {
        updateGameprofile(this.owner, (p_155747_) ->
        {
            this.owner = p_155747_;
            this.setChanged();
        });
    }

    public static void updateGameprofile(@Nullable GameProfile p_155739_, Consumer<GameProfile> p_155740_)
    {
        if (p_155739_ != null && !StringUtil.isNullOrEmpty(p_155739_.getName()) && (!p_155739_.isComplete() || !p_155739_.getProperties().containsKey("textures")) && profileCache != null && sessionService != null)
        {
            profileCache.getAsync(p_155739_.getName(), (p_182470_) ->
            {
                Util.backgroundExecutor().execute(() -> {
                    Util.ifElse(p_182470_, (p_182479_) -> {
                        Property property = Iterables.getFirst(p_182479_.getProperties().get("textures"), (Property)null);

                        if (property == null)
                        {
                            p_182479_ = sessionService.fillProfileProperties(p_182479_, true);
                        }

                        GameProfile gameprofile = p_182479_;
                        mainThreadExecutor.execute(() -> {
                            profileCache.add(gameprofile);
                            p_155740_.accept(gameprofile);
                        });
                    }, () -> {
                        mainThreadExecutor.execute(() -> {
                            p_155740_.accept(p_155739_);
                        });
                    });
                });
            });
        }
        else
        {
            p_155740_.accept(p_155739_);
        }
    }
}
