package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.ClientTelemetryManager;
import net.minecraft.client.DebugQueryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DemoIntroScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.multiplayer.ChatPreviewWarningScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.client.resources.sounds.BeeAggressiveSoundInstance;
import net.minecraft.client.resources.sounds.BeeFlyingSoundInstance;
import net.minecraft.client.resources.sounds.BeeSoundInstance;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChatPreviewPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatHeaderPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayChatPreviewPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

public class ClientPacketListener implements ClientGamePacketListener
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component GENERIC_DISCONNECT_MESSAGE = Component.translatable("disconnect.lost");
    private static final Component UNSECURE_SERVER_TOAST_TITLE = Component.translatable("multiplayer.unsecureserver.toast.title");
    private static final Component UNSERURE_SERVER_TOAST = Component.translatable("multiplayer.unsecureserver.toast");
    private static final int UNACKNOWLEDGED_MESSAGES_THRESHOLD = 64;
    private final Connection connection;
    private final GameProfile localGameProfile;
    private final Screen callbackScreen;
    private final Minecraft minecraft;
    private ClientLevel level;
    private ClientLevel.ClientLevelData levelData;
    private final Map<UUID, PlayerInfo> playerInfoMap = Maps.newHashMap();
    private final ClientAdvancements advancements;
    private final ClientSuggestionProvider suggestionsProvider;
    private final DebugQueryHandler debugQueryHandler = new DebugQueryHandler(this);
    private int serverChunkRadius = 3;
    private int serverSimulationDistance = 3;
    private final RandomSource random = RandomSource.createThreadSafe();
    private CommandDispatcher<SharedSuggestionProvider> commands = new CommandDispatcher<>();
    private final RecipeManager recipeManager = new RecipeManager();
    private final UUID id = UUID.randomUUID();
    private Set<ResourceKey<Level>> levels;
    private RegistryAccess.Frozen registryAccess = RegistryAccess.BUILTIN.get();
    private final ClientTelemetryManager telemetryManager;
    private final SignedMessageChain.Encoder signedMessageEncoder = (new SignedMessageChain()).encoder();
    private final LastSeenMessagesTracker lastSeenMessagesTracker = new LastSeenMessagesTracker(5);
    private Optional<LastSeenMessages.Entry> lastUnacknowledgedReceivedMessage = Optional.empty();
    private int unacknowledgedReceivedMessageCount;

    public ClientPacketListener(Minecraft p_194193_, Screen p_194194_, Connection p_194195_, GameProfile p_194196_, ClientTelemetryManager p_194197_)
    {
        this.minecraft = p_194193_;
        this.callbackScreen = p_194194_;
        this.connection = p_194195_;
        this.localGameProfile = p_194196_;
        this.advancements = new ClientAdvancements(p_194193_);
        this.suggestionsProvider = new ClientSuggestionProvider(this, p_194193_);
        this.telemetryManager = p_194197_;
    }

    public ClientSuggestionProvider getSuggestionsProvider()
    {
        return this.suggestionsProvider;
    }

    public void cleanup()
    {
        this.level = null;
    }

    public RecipeManager getRecipeManager()
    {
        return this.recipeManager;
    }

    public void handleLogin(ClientboundLoginPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.gameMode = new MultiPlayerGameMode(this.minecraft, this);
        this.registryAccess = pPacket.registryHolder();

        if (!this.connection.isMemoryConnection())
        {
            this.registryAccess.registries().forEach((p_205542_) ->
            {
                p_205542_.value().resetTags();
            });
        }

        List<ResourceKey<Level>> list = Lists.newArrayList(pPacket.levels());
        Collections.shuffle(list);
        this.levels = Sets.newLinkedHashSet(list);
        ResourceKey<Level> resourcekey = pPacket.dimension();
        Holder<DimensionType> holder = this.registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).getHolderOrThrow(pPacket.dimensionType());
        this.serverChunkRadius = pPacket.chunkRadius();
        this.serverSimulationDistance = pPacket.simulationDistance();
        boolean flag = pPacket.isDebug();
        boolean flag1 = pPacket.isFlat();
        ClientLevel.ClientLevelData clientlevel$clientleveldata = new ClientLevel.ClientLevelData(Difficulty.NORMAL, pPacket.hardcore(), flag1);
        this.levelData = clientlevel$clientleveldata;
        this.level = new ClientLevel(this, clientlevel$clientleveldata, resourcekey, holder, this.serverChunkRadius, this.serverSimulationDistance, this.minecraft::getProfiler, this.minecraft.levelRenderer, flag, pPacket.seed());
        this.minecraft.setLevel(this.level);

        if (this.minecraft.player == null)
        {
            this.minecraft.player = this.minecraft.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook());
            this.minecraft.player.setYRot(-180.0F);

            if (this.minecraft.getSingleplayerServer() != null)
            {
                this.minecraft.getSingleplayerServer().setUUID(this.minecraft.player.getUUID());
            }
        }

        this.minecraft.debugRenderer.clear();
        this.minecraft.player.resetPos();
        int i = pPacket.playerId();
        this.minecraft.player.setId(i);
        this.level.addPlayer(i, this.minecraft.player);
        this.minecraft.player.input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(this.minecraft.player);
        this.minecraft.cameraEntity = this.minecraft.player;
        this.minecraft.setScreen(new ReceivingLevelScreen());
        this.minecraft.player.setReducedDebugInfo(pPacket.reducedDebugInfo());
        this.minecraft.player.setShowDeathScreen(pPacket.showDeathScreen());
        this.minecraft.player.setLastDeathLocation(pPacket.lastDeathLocation());
        this.minecraft.gameMode.setLocalMode(pPacket.gameType(), pPacket.previousGameType());
        this.minecraft.options.setServerRenderDistance(pPacket.chunkRadius());
        this.minecraft.options.broadcastOptions();
        this.connection.send(new ServerboundCustomPayloadPacket(ServerboundCustomPayloadPacket.BRAND, (new FriendlyByteBuf(Unpooled.buffer())).writeUtf(ClientBrandRetriever.getClientModName())));
        this.minecraft.getGame().onStartGameSession();
        this.telemetryManager.onPlayerInfoReceived(pPacket.gameType(), pPacket.hardcore());
    }

    public void handleAddEntity(ClientboundAddEntityPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        EntityType<?> entitytype = pPacket.getType();
        Entity entity = entitytype.create(this.level);

        if (entity != null)
        {
            entity.recreateFromPacket(pPacket);
            int i = pPacket.getId();
            this.level.putNonPlayerEntity(i, entity);
            this.postAddEntitySoundInstance(entity);
        }
        else
        {
            LOGGER.warn("Skipping Entity with id {}", (Object)entitytype);
        }
    }

    private void postAddEntitySoundInstance(Entity p_233664_)
    {
        if (p_233664_ instanceof AbstractMinecart)
        {
            this.minecraft.getSoundManager().play(new MinecartSoundInstance((AbstractMinecart)p_233664_));
        }
        else if (p_233664_ instanceof Bee)
        {
            boolean flag = ((Bee)p_233664_).isAngry();
            BeeSoundInstance beesoundinstance;

            if (flag)
            {
                beesoundinstance = new BeeAggressiveSoundInstance((Bee)p_233664_);
            }
            else
            {
                beesoundinstance = new BeeFlyingSoundInstance((Bee)p_233664_);
            }

            this.minecraft.getSoundManager().queueTickingSound(beesoundinstance);
        }
    }

    public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        double d0 = pPacket.getX();
        double d1 = pPacket.getY();
        double d2 = pPacket.getZ();
        Entity entity = new ExperienceOrb(this.level, d0, d1, d2, pPacket.getValue());
        entity.syncPacketPositionCodec(d0, d1, d2);
        entity.setYRot(0.0F);
        entity.setXRot(0.0F);
        entity.setId(pPacket.getId());
        this.level.putNonPlayerEntity(pPacket.getId(), entity);
    }

    public void handleSetEntityMotion(ClientboundSetEntityMotionPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getId());

        if (entity != null)
        {
            entity.lerpMotion((double)pPacket.getXa() / 8000.0D, (double)pPacket.getYa() / 8000.0D, (double)pPacket.getZa() / 8000.0D);
        }
    }

    public void handleSetEntityData(ClientboundSetEntityDataPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getId());

        if (entity != null && pPacket.getUnpackedData() != null)
        {
            entity.getEntityData().assignValues(pPacket.getUnpackedData());
        }
    }

    public void handleAddPlayer(ClientboundAddPlayerPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        PlayerInfo playerinfo = this.getPlayerInfo(pPacket.getPlayerId());

        if (playerinfo == null)
        {
            LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", (Object)pPacket.getPlayerId());
        }
        else
        {
            double d0 = pPacket.getX();
            double d1 = pPacket.getY();
            double d2 = pPacket.getZ();
            float f = (float)(pPacket.getyRot() * 360) / 256.0F;
            float f1 = (float)(pPacket.getxRot() * 360) / 256.0F;
            int i = pPacket.getEntityId();
            RemotePlayer remoteplayer = new RemotePlayer(this.minecraft.level, playerinfo.getProfile(), playerinfo.getProfilePublicKey());
            remoteplayer.setId(i);
            remoteplayer.syncPacketPositionCodec(d0, d1, d2);
            remoteplayer.absMoveTo(d0, d1, d2, f, f1);
            remoteplayer.setOldPosAndRot();
            this.level.addPlayer(i, remoteplayer);
        }
    }

    public void handleTeleportEntity(ClientboundTeleportEntityPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getId());

        if (entity != null)
        {
            double d0 = pPacket.getX();
            double d1 = pPacket.getY();
            double d2 = pPacket.getZ();
            entity.syncPacketPositionCodec(d0, d1, d2);

            if (!entity.isControlledByLocalInstance())
            {
                float f = (float)(pPacket.getyRot() * 360) / 256.0F;
                float f1 = (float)(pPacket.getxRot() * 360) / 256.0F;
                entity.lerpTo(d0, d1, d2, f, f1, 3, true);
                entity.setOnGround(pPacket.isOnGround());
            }
        }
    }

    public void handleSetCarriedItem(ClientboundSetCarriedItemPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        if (Inventory.isHotbarSlot(pPacket.getSlot()))
        {
            this.minecraft.player.getInventory().selected = pPacket.getSlot();
        }
    }

    public void handleMoveEntity(ClientboundMoveEntityPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = pPacket.getEntity(this.level);

        if (entity != null)
        {
            if (!entity.isControlledByLocalInstance())
            {
                if (pPacket.hasPosition())
                {
                    VecDeltaCodec vecdeltacodec = entity.getPositionCodec();
                    Vec3 vec3 = vecdeltacodec.decode((long)pPacket.getXa(), (long)pPacket.getYa(), (long)pPacket.getZa());
                    vecdeltacodec.setBase(vec3);
                    float f = pPacket.hasRotation() ? (float)(pPacket.getyRot() * 360) / 256.0F : entity.getYRot();
                    float f1 = pPacket.hasRotation() ? (float)(pPacket.getxRot() * 360) / 256.0F : entity.getXRot();
                    entity.lerpTo(vec3.x(), vec3.y(), vec3.z(), f, f1, 3, false);
                }
                else if (pPacket.hasRotation())
                {
                    float f2 = (float)(pPacket.getyRot() * 360) / 256.0F;
                    float f3 = (float)(pPacket.getxRot() * 360) / 256.0F;
                    entity.lerpTo(entity.getX(), entity.getY(), entity.getZ(), f2, f3, 3, false);
                }

                entity.setOnGround(pPacket.isOnGround());
            }
        }
    }

    public void handleRotateMob(ClientboundRotateHeadPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = pPacket.getEntity(this.level);

        if (entity != null)
        {
            float f = (float)(pPacket.getYHeadRot() * 360) / 256.0F;
            entity.lerpHeadTo(f, 3);
        }
    }

    public void handleRemoveEntities(ClientboundRemoveEntitiesPacket p_182633_)
    {
        PacketUtils.ensureRunningOnSameThread(p_182633_, this, this.minecraft);
        p_182633_.getEntityIds().forEach((int p_205521_) ->
        {
            this.level.removeEntity(p_205521_, Entity.RemovalReason.DISCARDED);
        });
    }

    public void handleMovePlayer(ClientboundPlayerPositionPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Player player = this.minecraft.player;

        if (pPacket.requestDismountVehicle())
        {
            player.removeVehicle();
        }

        Vec3 vec3 = player.getDeltaMovement();
        boolean flag = pPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.X);
        boolean flag1 = pPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Y);
        boolean flag2 = pPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Z);
        double d0;
        double d1;

        if (flag)
        {
            d0 = vec3.x();
            d1 = player.getX() + pPacket.getX();
            player.xOld += pPacket.getX();
        }
        else
        {
            d0 = 0.0D;
            d1 = pPacket.getX();
            player.xOld = d1;
        }

        double d2;
        double d3;

        if (flag1)
        {
            d2 = vec3.y();
            d3 = player.getY() + pPacket.getY();
            player.yOld += pPacket.getY();
        }
        else
        {
            d2 = 0.0D;
            d3 = pPacket.getY();
            player.yOld = d3;
        }

        double d4;
        double d5;

        if (flag2)
        {
            d4 = vec3.z();
            d5 = player.getZ() + pPacket.getZ();
            player.zOld += pPacket.getZ();
        }
        else
        {
            d4 = 0.0D;
            d5 = pPacket.getZ();
            player.zOld = d5;
        }

        player.setPosRaw(d1, d3, d5);
        player.xo = d1;
        player.yo = d3;
        player.zo = d5;
        player.setDeltaMovement(d0, d2, d4);
        float f = pPacket.getYRot();
        float f1 = pPacket.getXRot();

        if (pPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT))
        {
            f1 += player.getXRot();
        }

        if (pPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT))
        {
            f += player.getYRot();
        }

        player.absMoveTo(d1, d3, d5, f, f1);
        this.connection.send(new ServerboundAcceptTeleportationPacket(pPacket.getId()));
        this.connection.send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), false));
    }

    public void handleChatPreview(ClientboundChatPreviewPacket p_233700_)
    {
        PacketUtils.ensureRunningOnSameThread(p_233700_, this, this.minecraft);
        ChatScreen chatscreen = this.minecraft.gui.getChat().getFocusedChat();

        if (chatscreen != null)
        {
            chatscreen.getChatPreview().handleResponse(p_233700_.queryId(), p_233700_.preview());
        }
    }

    public void handleSetDisplayChatPreview(ClientboundSetDisplayChatPreviewPacket p_233706_)
    {
        PacketUtils.ensureRunningOnSameThread(p_233706_, this, this.minecraft);
        ServerData serverdata = this.minecraft.getCurrentServer();

        if (serverdata != null)
        {
            serverdata.setChatPreviewEnabled(p_233706_.enabled());
        }
    }

    public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        int i = 19 | (pPacket.shouldSuppressLightUpdates() ? 128 : 0);
        pPacket.runUpdates((p_205524_, p_205525_) ->
        {
            this.level.setServerVerifiedBlockState(p_205524_, p_205525_, i);
        });
    }

    public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket p_194241_)
    {
        PacketUtils.ensureRunningOnSameThread(p_194241_, this, this.minecraft);
        this.updateLevelChunk(p_194241_.getX(), p_194241_.getZ(), p_194241_.getChunkData());
        this.queueLightUpdate(p_194241_.getX(), p_194241_.getZ(), p_194241_.getLightData());
    }

    private void updateLevelChunk(int p_194199_, int p_194200_, ClientboundLevelChunkPacketData p_194201_)
    {
        this.level.getChunkSource().replaceWithPacketData(p_194199_, p_194200_, p_194201_.getReadBuffer(), p_194201_.getHeightmaps(), p_194201_.getBlockEntitiesTagsConsumer(p_194199_, p_194200_));
    }

    private void queueLightUpdate(int p_194203_, int p_194204_, ClientboundLightUpdatePacketData p_194205_)
    {
        this.level.queueLightUpdate(() ->
        {
            this.applyLightData(p_194203_, p_194204_, p_194205_);
            LevelChunk levelchunk = this.level.getChunkSource().getChunk(p_194203_, p_194204_, false);

            if (levelchunk != null)
            {
                this.enableChunkLight(levelchunk, p_194203_, p_194204_);
            }
        });
    }

    private void enableChunkLight(LevelChunk p_194213_, int p_194214_, int p_194215_)
    {
        LevelLightEngine levellightengine = this.level.getChunkSource().getLightEngine();
        LevelChunkSection[] alevelchunksection = p_194213_.getSections();
        ChunkPos chunkpos = p_194213_.getPos();
        levellightengine.enableLightSources(chunkpos, true);

        for (int i = 0; i < alevelchunksection.length; ++i)
        {
            LevelChunkSection levelchunksection = alevelchunksection[i];
            int j = this.level.getSectionYFromSectionIndex(i);
            levellightengine.updateSectionStatus(SectionPos.of(chunkpos, j), levelchunksection.hasOnlyAir());
            this.level.setSectionDirtyWithNeighbors(p_194214_, j, p_194215_);
        }

        this.level.setLightReady(p_194214_, p_194215_);
    }

    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        int i = pPacket.getX();
        int j = pPacket.getZ();
        ClientChunkCache clientchunkcache = this.level.getChunkSource();
        clientchunkcache.drop(i, j);
        this.queueLightUpdate(pPacket);
    }

    private void queueLightUpdate(ClientboundForgetLevelChunkPacket p_194253_)
    {
        this.level.queueLightUpdate(() ->
        {
            LevelLightEngine levellightengine = this.level.getLightEngine();

            for (int i = this.level.getMinSection(); i < this.level.getMaxSection(); ++i)
            {
                levellightengine.updateSectionStatus(SectionPos.of(p_194253_.getX(), i, p_194253_.getZ()), true);
            }

            levellightengine.enableLightSources(new ChunkPos(p_194253_.getX(), p_194253_.getZ()), false);
            this.level.setLightReady(p_194253_.getX(), p_194253_.getZ());
        });
    }

    public void handleBlockUpdate(ClientboundBlockUpdatePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.level.setServerVerifiedBlockState(pPacket.getPos(), pPacket.getBlockState(), 19);
    }

    public void handleDisconnect(ClientboundDisconnectPacket pPacket)
    {
        this.connection.disconnect(pPacket.getReason());
    }

    public void onDisconnect(Component pReason)
    {
        this.minecraft.clearLevel();
        this.telemetryManager.onDisconnect();

        if (this.callbackScreen != null)
        {
            if (this.callbackScreen instanceof RealmsScreen)
            {
                this.minecraft.setScreen(new DisconnectedRealmsScreen(this.callbackScreen, GENERIC_DISCONNECT_MESSAGE, pReason));
            }
            else
            {
                this.minecraft.setScreen(new DisconnectedScreen(this.callbackScreen, GENERIC_DISCONNECT_MESSAGE, pReason));
            }
        }
        else
        {
            this.minecraft.setScreen(new DisconnectedScreen(new JoinMultiplayerScreen(new TitleScreen()), GENERIC_DISCONNECT_MESSAGE, pReason));
        }
    }

    public void send(Packet<?> pPacket)
    {
        this.connection.send(pPacket);
    }

    public void handleTakeItemEntity(ClientboundTakeItemEntityPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getItemId());
        LivingEntity livingentity = (LivingEntity)this.level.getEntity(pPacket.getPlayerId());

        if (livingentity == null)
        {
            livingentity = this.minecraft.player;
        }

        if (entity != null)
        {
            if (entity instanceof ExperienceOrb)
            {
                this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, (this.random.nextFloat() - this.random.nextFloat()) * 0.35F + 0.9F, false);
            }
            else
            {
                this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 1.4F + 2.0F, false);
            }

            this.minecraft.particleEngine.add(new ItemPickupParticle(this.minecraft.getEntityRenderDispatcher(), this.minecraft.renderBuffers(), this.level, entity, livingentity));

            if (entity instanceof ItemEntity)
            {
                ItemEntity itementity = (ItemEntity)entity;
                ItemStack itemstack = itementity.getItem();
                itemstack.shrink(pPacket.getAmount());

                if (itemstack.isEmpty())
                {
                    this.level.removeEntity(pPacket.getItemId(), Entity.RemovalReason.DISCARDED);
                }
            }
            else if (!(entity instanceof ExperienceOrb))
            {
                this.level.removeEntity(pPacket.getItemId(), Entity.RemovalReason.DISCARDED);
            }
        }
    }

    public void handleSystemChat(ClientboundSystemChatPacket p_233708_)
    {
        PacketUtils.ensureRunningOnSameThread(p_233708_, this, this.minecraft);
        this.minecraft.getChatListener().handleSystemMessage(p_233708_.content(), p_233708_.overlay());
    }

    public void handlePlayerChat(ClientboundPlayerChatPacket p_233702_)
    {
        PacketUtils.ensureRunningOnSameThread(p_233702_, this, this.minecraft);
        Optional<ChatType.Bound> optional = p_233702_.resolveChatType(this.registryAccess);

        if (!optional.isPresent())
        {
            this.connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_packet"));
        }
        else
        {
            this.minecraft.getChatListener().handleChatMessage(p_233702_.message(), (ChatType.Bound)optional.get());
        }
    }

    public void handlePlayerChatHeader(ClientboundPlayerChatHeaderPacket p_241547_)
    {
        PacketUtils.ensureRunningOnSameThread(p_241547_, this, this.minecraft);
        this.minecraft.getChatListener().a(p_241547_.header(), p_241547_.headerSignature(), p_241547_.bodyDigest());
    }

    public void handleDeleteChat(ClientboundDeleteChatPacket p_241325_)
    {
        PacketUtils.ensureRunningOnSameThread(p_241325_, this, this.minecraft);
        MessageSignature messagesignature = p_241325_.messageSignature();

        if (!this.minecraft.getChatListener().removeFromDelayedMessageQueue(messagesignature))
        {
            this.minecraft.gui.getChat().deleteMessage(messagesignature);
        }
    }

    public void handleAnimate(ClientboundAnimatePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getId());

        if (entity != null)
        {
            if (pPacket.getAction() == 0)
            {
                LivingEntity livingentity = (LivingEntity)entity;
                livingentity.swing(InteractionHand.MAIN_HAND);
            }
            else if (pPacket.getAction() == 3)
            {
                LivingEntity livingentity1 = (LivingEntity)entity;
                livingentity1.swing(InteractionHand.OFF_HAND);
            }
            else if (pPacket.getAction() == 1)
            {
                entity.animateHurt();
            }
            else if (pPacket.getAction() == 2)
            {
                Player player = (Player)entity;
                player.stopSleepInBed(false, false);
            }
            else if (pPacket.getAction() == 4)
            {
                this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.CRIT);
            }
            else if (pPacket.getAction() == 5)
            {
                this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.ENCHANTED_HIT);
            }
        }
    }

    public void handleSetTime(ClientboundSetTimePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.level.setGameTime(pPacket.getGameTime());
        this.minecraft.level.setDayTime(pPacket.getDayTime());
    }

    public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.level.setDefaultSpawnPos(pPacket.getPos(), pPacket.getAngle());
        Screen screen = this.minecraft.screen;

        if (screen instanceof ReceivingLevelScreen receivinglevelscreen)
        {
            receivinglevelscreen.loadingPacketsReceived();
        }
    }

    public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getVehicle());

        if (entity == null)
        {
            LOGGER.warn("Received passengers for unknown entity");
        }
        else
        {
            boolean flag = entity.hasIndirectPassenger(this.minecraft.player);
            entity.ejectPassengers();

            for (int i : pPacket.getPassengers())
            {
                Entity entity1 = this.level.getEntity(i);

                if (entity1 != null)
                {
                    entity1.startRiding(entity, true);

                    if (entity1 == this.minecraft.player && !flag)
                    {
                        if (entity instanceof Boat)
                        {
                            this.minecraft.player.yRotO = entity.getYRot();
                            this.minecraft.player.setYRot(entity.getYRot());
                            this.minecraft.player.setYHeadRot(entity.getYRot());
                        }

                        Component component = Component.a("mount.onboard", this.minecraft.options.keyShift.getTranslatedKeyMessage());
                        this.minecraft.gui.setOverlayMessage(component, false);
                        this.minecraft.getNarrator().sayNow(component);
                    }
                }
            }
        }
    }

    public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getSourceId());

        if (entity instanceof Mob)
        {
            ((Mob)entity).setDelayedLeashHolderId(pPacket.getDestId());
        }
    }

    private static ItemStack findTotem(Player pPlayer)
    {
        for (InteractionHand interactionhand : InteractionHand.values())
        {
            ItemStack itemstack = pPlayer.getItemInHand(interactionhand);

            if (itemstack.is(Items.TOTEM_OF_UNDYING))
            {
                return itemstack;
            }
        }

        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }

    public void handleEntityEvent(ClientboundEntityEventPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = pPacket.getEntity(this.level);

        if (entity != null)
        {
            if (pPacket.getEventId() == 21)
            {
                this.minecraft.getSoundManager().play(new GuardianAttackSoundInstance((Guardian)entity));
            }
            else if (pPacket.getEventId() == 35)
            {
                int i = 40;
                this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);

                if (entity == this.minecraft.player)
                {
                    this.minecraft.gameRenderer.displayItemActivation(findTotem(this.minecraft.player));
                }
            }
            else
            {
                entity.handleEntityEvent(pPacket.getEventId());
            }
        }
    }

    public void handleSetHealth(ClientboundSetHealthPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.player.hurtTo(pPacket.getHealth());
        this.minecraft.player.getFoodData().setFoodLevel(pPacket.getFood());
        this.minecraft.player.getFoodData().setSaturation(pPacket.getSaturation());
    }

    public void handleSetExperience(ClientboundSetExperiencePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.player.setExperienceValues(pPacket.getExperienceProgress(), pPacket.getTotalExperience(), pPacket.getExperienceLevel());
    }

    public void handleRespawn(ClientboundRespawnPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        ResourceKey<Level> resourcekey = pPacket.getDimension();
        Holder<DimensionType> holder = this.registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).getHolderOrThrow(pPacket.getDimensionType());
        LocalPlayer localplayer = this.minecraft.player;
        int i = localplayer.getId();

        if (resourcekey != localplayer.level.dimension())
        {
            Scoreboard scoreboard = this.level.getScoreboard();
            Map<String, MapItemSavedData> map = this.level.getAllMapData();
            boolean flag = pPacket.isDebug();
            boolean flag1 = pPacket.isFlat();
            ClientLevel.ClientLevelData clientlevel$clientleveldata = new ClientLevel.ClientLevelData(this.levelData.getDifficulty(), this.levelData.isHardcore(), flag1);
            this.levelData = clientlevel$clientleveldata;
            this.level = new ClientLevel(this, clientlevel$clientleveldata, resourcekey, holder, this.serverChunkRadius, this.serverSimulationDistance, this.minecraft::getProfiler, this.minecraft.levelRenderer, flag, pPacket.getSeed());
            this.level.setScoreboard(scoreboard);
            this.level.addMapData(map);
            this.minecraft.setLevel(this.level);
            this.minecraft.setScreen(new ReceivingLevelScreen());
        }

        String s = localplayer.getServerBrand();
        this.minecraft.cameraEntity = null;

        if (localplayer.hasContainerOpen())
        {
            localplayer.closeContainer();
        }

        LocalPlayer localplayer1 = this.minecraft.gameMode.createPlayer(this.level, localplayer.getStats(), localplayer.getRecipeBook(), localplayer.isShiftKeyDown(), localplayer.isSprinting());
        localplayer1.setId(i);
        this.minecraft.player = localplayer1;

        if (resourcekey != localplayer.level.dimension())
        {
            this.minecraft.getMusicManager().stopPlaying();
        }

        this.minecraft.cameraEntity = localplayer1;
        localplayer1.getEntityData().assignValues(localplayer.getEntityData().getAll());

        if (pPacket.shouldKeepAllPlayerData())
        {
            localplayer1.getAttributes().assignValues(localplayer.getAttributes());
        }

        localplayer1.resetPos();
        localplayer1.setServerBrand(s);
        this.level.addPlayer(i, localplayer1);
        localplayer1.setYRot(-180.0F);
        localplayer1.input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(localplayer1);
        localplayer1.setReducedDebugInfo(localplayer.isReducedDebugInfo());
        localplayer1.setShowDeathScreen(localplayer.shouldShowDeathScreen());
        localplayer1.setLastDeathLocation(pPacket.getLastDeathLocation());

        if (this.minecraft.screen instanceof DeathScreen)
        {
            this.minecraft.setScreen((Screen)null);
        }

        this.minecraft.gameMode.setLocalMode(pPacket.getPlayerGameType(), pPacket.getPreviousPlayerGameType());
    }

    public void handleExplosion(ClientboundExplodePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Explosion explosion = new Explosion(this.minecraft.level, (Entity)null, pPacket.getX(), pPacket.getY(), pPacket.getZ(), pPacket.getPower(), pPacket.getToBlow());
        explosion.finalizeExplosion(true);
        this.minecraft.player.setDeltaMovement(this.minecraft.player.getDeltaMovement().add((double)pPacket.getKnockbackX(), (double)pPacket.getKnockbackY(), (double)pPacket.getKnockbackZ()));
    }

    public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getEntityId());

        if (entity instanceof AbstractHorse)
        {
            LocalPlayer localplayer = this.minecraft.player;
            AbstractHorse abstracthorse = (AbstractHorse)entity;
            SimpleContainer simplecontainer = new SimpleContainer(pPacket.getSize());
            HorseInventoryMenu horseinventorymenu = new HorseInventoryMenu(pPacket.getContainerId(), localplayer.getInventory(), simplecontainer, abstracthorse);
            localplayer.containerMenu = horseinventorymenu;
            this.minecraft.setScreen(new HorseInventoryScreen(horseinventorymenu, localplayer.getInventory(), abstracthorse));
        }
    }

    public void handleOpenScreen(ClientboundOpenScreenPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        MenuScreens.create(pPacket.getType(), this.minecraft, pPacket.getContainerId(), pPacket.getTitle());
    }

    public void handleContainerSetSlot(ClientboundContainerSetSlotPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Player player = this.minecraft.player;
        ItemStack itemstack = pPacket.getItem();
        int i = pPacket.getSlot();
        this.minecraft.getTutorial().onGetItem(itemstack);

        if (pPacket.getContainerId() == -1)
        {
            if (!(this.minecraft.screen instanceof CreativeModeInventoryScreen))
            {
                player.containerMenu.setCarried(itemstack);
            }
        }
        else if (pPacket.getContainerId() == -2)
        {
            player.getInventory().setItem(i, itemstack);
        }
        else
        {
            boolean flag = false;

            if (this.minecraft.screen instanceof CreativeModeInventoryScreen)
            {
                CreativeModeInventoryScreen creativemodeinventoryscreen = (CreativeModeInventoryScreen)this.minecraft.screen;
                flag = creativemodeinventoryscreen.getSelectedTab() != CreativeModeTab.TAB_INVENTORY.getId();
            }

            if (pPacket.getContainerId() == 0 && InventoryMenu.isHotbarSlot(i))
            {
                if (!itemstack.isEmpty())
                {
                    ItemStack itemstack1 = player.inventoryMenu.getSlot(i).getItem();

                    if (itemstack1.isEmpty() || itemstack1.getCount() < itemstack.getCount())
                    {
                        itemstack.setPopTime(5);
                    }
                }

                player.inventoryMenu.setItem(i, pPacket.getStateId(), itemstack);
            }
            else if (pPacket.getContainerId() == player.containerMenu.containerId && (pPacket.getContainerId() != 0 || !flag))
            {
                player.containerMenu.setItem(i, pPacket.getStateId(), itemstack);
            }
        }
    }

    public void handleContainerContent(ClientboundContainerSetContentPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Player player = this.minecraft.player;

        if (pPacket.getContainerId() == 0)
        {
            player.inventoryMenu.initializeContents(pPacket.getStateId(), pPacket.getItems(), pPacket.getCarriedItem());
        }
        else if (pPacket.getContainerId() == player.containerMenu.containerId)
        {
            player.containerMenu.initializeContents(pPacket.getStateId(), pPacket.getItems(), pPacket.getCarriedItem());
        }
    }

    public void handleOpenSignEditor(ClientboundOpenSignEditorPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        BlockPos blockpos = pPacket.getPos();
        BlockEntity blockentity = this.level.getBlockEntity(blockpos);

        if (!(blockentity instanceof SignBlockEntity))
        {
            BlockState blockstate = this.level.getBlockState(blockpos);
            blockentity = new SignBlockEntity(blockpos, blockstate);
            blockentity.setLevel(this.level);
        }

        this.minecraft.player.openTextEdit((SignBlockEntity)blockentity);
    }

    public void handleBlockEntityData(ClientboundBlockEntityDataPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        BlockPos blockpos = pPacket.getPos();
        this.minecraft.level.getBlockEntity(blockpos, pPacket.getType()).ifPresent((p_205557_) ->
        {
            CompoundTag compoundtag = pPacket.getTag();

            if (compoundtag != null)
            {
                p_205557_.load(compoundtag);
            }

            if (p_205557_ instanceof CommandBlockEntity && this.minecraft.screen instanceof CommandBlockEditScreen)
            {
                ((CommandBlockEditScreen)this.minecraft.screen).updateGui();
            }
        });
    }

    public void handleContainerSetData(ClientboundContainerSetDataPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Player player = this.minecraft.player;

        if (player.containerMenu != null && player.containerMenu.containerId == pPacket.getContainerId())
        {
            player.containerMenu.setData(pPacket.getId(), pPacket.getValue());
        }
    }

    public void handleSetEquipment(ClientboundSetEquipmentPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getEntity());

        if (entity != null)
        {
            pPacket.getSlots().forEach((p_205528_) ->
            {
                entity.setItemSlot(p_205528_.getFirst(), p_205528_.getSecond());
            });
        }
    }

    public void handleContainerClose(ClientboundContainerClosePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.player.clientSideCloseContainer();
    }

    public void handleBlockEvent(ClientboundBlockEventPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.level.blockEvent(pPacket.getPos(), pPacket.getBlock(), pPacket.getB0(), pPacket.getB1());
    }

    public void handleBlockDestruction(ClientboundBlockDestructionPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.level.destroyBlockProgress(pPacket.getId(), pPacket.getPos(), pPacket.getProgress());
    }

    public void handleGameEvent(ClientboundGameEventPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Player player = this.minecraft.player;
        ClientboundGameEventPacket.Type clientboundgameeventpacket$type = pPacket.getEvent();
        float f = pPacket.getParam();
        int i = Mth.floor(f + 0.5F);

        if (clientboundgameeventpacket$type == ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE)
        {
            player.displayClientMessage(Component.translatable("block.minecraft.spawn.not_valid"), false);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.START_RAINING)
        {
            this.level.getLevelData().setRaining(true);
            this.level.setRainLevel(0.0F);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.STOP_RAINING)
        {
            this.level.getLevelData().setRaining(false);
            this.level.setRainLevel(1.0F);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.CHANGE_GAME_MODE)
        {
            this.minecraft.gameMode.setLocalMode(GameType.byId(i));
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.WIN_GAME)
        {
            if (i == 0)
            {
                this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
                this.minecraft.setScreen(new ReceivingLevelScreen());
            }
            else if (i == 1)
            {
                this.minecraft.setScreen(new WinScreen(true, () ->
                {
                    this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
                }));
            }
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.DEMO_EVENT)
        {
            Options options = this.minecraft.options;

            if (f == 0.0F)
            {
                this.minecraft.setScreen(new DemoIntroScreen());
            }
            else if (f == 101.0F)
            {
                this.minecraft.gui.getChat().addMessage(Component.a("demo.help.movement", options.keyUp.getTranslatedKeyMessage(), options.keyLeft.getTranslatedKeyMessage(), options.keyDown.getTranslatedKeyMessage(), options.keyRight.getTranslatedKeyMessage()));
            }
            else if (f == 102.0F)
            {
                this.minecraft.gui.getChat().addMessage(Component.a("demo.help.jump", options.keyJump.getTranslatedKeyMessage()));
            }
            else if (f == 103.0F)
            {
                this.minecraft.gui.getChat().addMessage(Component.a("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage()));
            }
            else if (f == 104.0F)
            {
                this.minecraft.gui.getChat().addMessage(Component.a("demo.day.6", options.keyScreenshot.getTranslatedKeyMessage()));
            }
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.ARROW_HIT_PLAYER)
        {
            this.level.playSound(player, player.getX(), player.getEyeY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.18F, 0.45F);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE)
        {
            this.level.setRainLevel(f);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE)
        {
            this.level.setThunderLevel(f);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.PUFFER_FISH_STING)
        {
            this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.PUFFER_FISH_STING, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT)
        {
            this.level.addParticle(ParticleTypes.ELDER_GUARDIAN, player.getX(), player.getY(), player.getZ(), 0.0D, 0.0D, 0.0D);

            if (i == 1)
            {
                this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.IMMEDIATE_RESPAWN)
        {
            this.minecraft.player.setShowDeathScreen(f == 0.0F);
        }
    }

    public void handleMapItemData(ClientboundMapItemDataPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        MapRenderer maprenderer = this.minecraft.gameRenderer.getMapRenderer();
        int i = pPacket.getMapId();
        String s = MapItem.makeKey(i);
        MapItemSavedData mapitemsaveddata = this.minecraft.level.getMapData(s);

        if (mapitemsaveddata == null)
        {
            mapitemsaveddata = MapItemSavedData.createForClient(pPacket.getScale(), pPacket.isLocked(), this.minecraft.level.dimension());
            this.minecraft.level.setMapData(s, mapitemsaveddata);
        }

        pPacket.applyToMap(mapitemsaveddata);
        maprenderer.update(i, mapitemsaveddata);
    }

    public void handleLevelEvent(ClientboundLevelEventPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        if (pPacket.isGlobalEvent())
        {
            this.minecraft.level.globalLevelEvent(pPacket.getType(), pPacket.getPos(), pPacket.getData());
        }
        else
        {
            this.minecraft.level.levelEvent(pPacket.getType(), pPacket.getPos(), pPacket.getData());
        }
    }

    public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.advancements.update(pPacket);
    }

    public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        ResourceLocation resourcelocation = pPacket.getTab();

        if (resourcelocation == null)
        {
            this.advancements.setSelectedTab((Advancement)null, false);
        }
        else
        {
            Advancement advancement = this.advancements.getAdvancements().get(resourcelocation);
            this.advancements.setSelectedTab(advancement, false);
        }
    }

    public void handleCommands(ClientboundCommandsPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.commands = new CommandDispatcher<>(pPacket.getRoot(new CommandBuildContext(this.registryAccess)));
    }

    public void handleStopSoundEvent(ClientboundStopSoundPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.getSoundManager().stop(pPacket.getName(), pPacket.getSource());
    }

    public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.suggestionsProvider.completeCustomSuggestions(pPacket.getId(), pPacket.getSuggestions());
    }

    public void handleUpdateRecipes(ClientboundUpdateRecipesPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.recipeManager.replaceRecipes(pPacket.getRecipes());
        ClientRecipeBook clientrecipebook = this.minecraft.player.getRecipeBook();
        clientrecipebook.setupCollections(this.recipeManager.getRecipes());
        this.minecraft.populateSearchTree(SearchRegistry.RECIPE_COLLECTIONS, clientrecipebook.getCollections());
    }

    public void handleLookAt(ClientboundPlayerLookAtPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Vec3 vec3 = pPacket.getPosition(this.level);

        if (vec3 != null)
        {
            this.minecraft.player.lookAt(pPacket.getFromAnchor(), vec3);
        }
    }

    public void handleTagQueryPacket(ClientboundTagQueryPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        if (!this.debugQueryHandler.handleResponse(pPacket.getTransactionId(), pPacket.getTag()))
        {
            LOGGER.debug("Got unhandled response to tag query {}", (int)pPacket.getTransactionId());
        }
    }

    public void handleAwardStats(ClientboundAwardStatsPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        for (Map.Entry < Stat<?>, Integer > entry : pPacket.getStats().entrySet())
        {
            Stat<?> stat = entry.getKey();
            int i = entry.getValue();
            this.minecraft.player.getStats().setValue(this.minecraft.player, stat, i);
        }

        if (this.minecraft.screen instanceof StatsUpdateListener)
        {
            ((StatsUpdateListener)this.minecraft.screen).onStatsUpdated();
        }
    }

    public void handleAddOrRemoveRecipes(ClientboundRecipePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        ClientRecipeBook clientrecipebook = this.minecraft.player.getRecipeBook();
        clientrecipebook.setBookSettings(pPacket.getBookSettings());
        ClientboundRecipePacket.State clientboundrecipepacket$state = pPacket.getState();

        switch (clientboundrecipepacket$state)
        {
            case REMOVE:
                for (ResourceLocation resourcelocation3 : pPacket.getRecipes())
                {
                    this.recipeManager.byKey(resourcelocation3).ifPresent(clientrecipebook::remove);
                }

                break;

            case INIT:
                for (ResourceLocation resourcelocation1 : pPacket.getRecipes())
                {
                    this.recipeManager.byKey(resourcelocation1).ifPresent(clientrecipebook::add);
                }

                for (ResourceLocation resourcelocation2 : pPacket.getHighlights())
                {
                    this.recipeManager.byKey(resourcelocation2).ifPresent(clientrecipebook::addHighlight);
                }

                break;

            case ADD:
                for (ResourceLocation resourcelocation : pPacket.getRecipes())
                {
                    this.recipeManager.byKey(resourcelocation).ifPresent((p_205537_) ->
                    {
                        clientrecipebook.add(p_205537_);
                        clientrecipebook.addHighlight(p_205537_);
                        RecipeToast.addOrUpdate(this.minecraft.getToasts(), p_205537_);
                    });
                }
        }

        clientrecipebook.getCollections().forEach((p_205540_) ->
        {
            p_205540_.updateKnownRecipes(clientrecipebook);
        });

        if (this.minecraft.screen instanceof RecipeUpdateListener)
        {
            ((RecipeUpdateListener)this.minecraft.screen).recipesUpdated();
        }
    }

    public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getEntityId());

        if (entity instanceof LivingEntity)
        {
            MobEffect mobeffect = pPacket.getEffect();

            if (mobeffect != null)
            {
                MobEffectInstance mobeffectinstance = new MobEffectInstance(mobeffect, pPacket.getEffectDurationTicks(), pPacket.getEffectAmplifier(), pPacket.isEffectAmbient(), pPacket.isEffectVisible(), pPacket.effectShowsIcon(), (MobEffectInstance)null, Optional.ofNullable(pPacket.getFactorData()));
                mobeffectinstance.setNoCounter(pPacket.isSuperLongDuration());
                ((LivingEntity)entity).forceAddEffect(mobeffectinstance, (Entity)null);
            }
        }
    }

    public void handleUpdateTags(ClientboundUpdateTagsPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        pPacket.getTags().forEach(this::updateTagsForRegistry);

        if (!this.connection.isMemoryConnection())
        {
            Blocks.rebuildCache();
        }

        NonNullList<ItemStack> nonnulllist = NonNullList.create();

        for (Item item : Registry.ITEM)
        {
            item.fillItemCategory(CreativeModeTab.TAB_SEARCH, nonnulllist);
        }

        this.minecraft.populateSearchTree(SearchRegistry.CREATIVE_NAMES, nonnulllist);
        this.minecraft.populateSearchTree(SearchRegistry.CREATIVE_TAGS, nonnulllist);
    }

    private <T> void updateTagsForRegistry(ResourceKey <? extends Registry <? extends T >> p_205561_, TagNetworkSerialization.NetworkPayload p_205562_)
    {
        if (!p_205562_.isEmpty())
        {
            Registry<T> registry = this.registryAccess.<T>registry(p_205561_).orElseThrow(() ->
            {
                return new IllegalStateException("Unknown registry " + p_205561_);
            });
            Map<TagKey<T>, List<Holder<T>>> map = new HashMap<>();
            TagNetworkSerialization.deserializeTagsFromNetwork((ResourceKey <? extends Registry<T >>)p_205561_, registry, p_205562_, map::put);
            registry.bindTags(map);
        }
    }

    public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket pPacket)
    {
    }

    public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket pPacket)
    {
    }

    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getPlayerId());

        if (entity == this.minecraft.player)
        {
            if (this.minecraft.player.shouldShowDeathScreen())
            {
                this.minecraft.setScreen(new DeathScreen(pPacket.getMessage(), this.level.getLevelData().isHardcore()));
            }
            else
            {
                this.minecraft.player.respawn();
            }
        }
    }

    public void handleChangeDifficulty(ClientboundChangeDifficultyPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.levelData.setDifficulty(pPacket.getDifficulty());
        this.levelData.setDifficultyLocked(pPacket.isLocked());
    }

    public void handleSetCamera(ClientboundSetCameraPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = pPacket.getEntity(this.level);

        if (entity != null)
        {
            this.minecraft.setCameraEntity(entity);
        }
    }

    public void handleInitializeBorder(ClientboundInitializeBorderPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        WorldBorder worldborder = this.level.getWorldBorder();
        worldborder.setCenter(pPacket.getNewCenterX(), pPacket.getNewCenterZ());
        long i = pPacket.getLerpTime();

        if (i > 0L)
        {
            worldborder.lerpSizeBetween(pPacket.getOldSize(), pPacket.getNewSize(), i);
        }
        else
        {
            worldborder.setSize(pPacket.getNewSize());
        }

        worldborder.setAbsoluteMaxSize(pPacket.getNewAbsoluteMaxSize());
        worldborder.setWarningBlocks(pPacket.getWarningBlocks());
        worldborder.setWarningTime(pPacket.getWarningTime());
    }

    public void handleSetBorderCenter(ClientboundSetBorderCenterPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.level.getWorldBorder().setCenter(pPacket.getNewCenterX(), pPacket.getNewCenterZ());
    }

    public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.level.getWorldBorder().lerpSizeBetween(pPacket.getOldSize(), pPacket.getNewSize(), pPacket.getLerpTime());
    }

    public void handleSetBorderSize(ClientboundSetBorderSizePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.level.getWorldBorder().setSize(pPacket.getSize());
    }

    public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.level.getWorldBorder().setWarningBlocks(pPacket.getWarningBlocks());
    }

    public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.level.getWorldBorder().setWarningTime(pPacket.getWarningDelay());
    }

    public void handleTitlesClear(ClientboundClearTitlesPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.gui.clear();

        if (pPacket.shouldResetTimes())
        {
            this.minecraft.gui.resetTitleTimes();
        }
    }

    public void handleServerData(ClientboundServerDataPacket p_233704_)
    {
        PacketUtils.ensureRunningOnSameThread(p_233704_, this, this.minecraft);
        ServerData serverdata = this.minecraft.getCurrentServer();

        if (serverdata != null)
        {
            p_233704_.getMotd().ifPresent((p_233678_) ->
            {
                serverdata.motd = p_233678_;
            });
            p_233704_.getIconBase64().ifPresent((p_233675_) ->
            {
                try {
                    serverdata.setIconB64(ServerData.parseFavicon(p_233675_));
                }
                catch (ParseException parseexception)
                {
                    LOGGER.error("Invalid server icon", (Throwable)parseexception);
                }
            });
            serverdata.setPreviewsChat(p_233704_.previewsChat());
            serverdata.setEnforcesSecureChat(p_233704_.enforcesSecureChat());
            ServerList.saveSingleServer(serverdata);

            if (!p_233704_.enforcesSecureChat())
            {
                SystemToast systemtoast = SystemToast.multiline(this.minecraft, SystemToast.SystemToastIds.UNSECURE_SERVER_WARNING, UNSECURE_SERVER_TOAST_TITLE, UNSERURE_SERVER_TOAST);
                this.minecraft.getToasts().addToast(systemtoast);
            }

            ServerData.ChatPreview serverdata$chatpreview = serverdata.getChatPreview();

            if (serverdata$chatpreview != null && !serverdata$chatpreview.isAcknowledged())
            {
                this.minecraft.execute(() ->
                {
                    this.minecraft.setScreen(new ChatPreviewWarningScreen(this.minecraft.screen, serverdata));
                });
            }
        }
    }

    public void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket p_240832_)
    {
        PacketUtils.ensureRunningOnSameThread(p_240832_, this, this.minecraft);
        this.suggestionsProvider.modifyCustomCompletions(p_240832_.action(), p_240832_.entries());
    }

    public void setActionBarText(ClientboundSetActionBarTextPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.gui.setOverlayMessage(pPacket.getText(), false);
    }

    public void setTitleText(ClientboundSetTitleTextPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.gui.setTitle(pPacket.getText());
    }

    public void setSubtitleText(ClientboundSetSubtitleTextPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.gui.setSubtitle(pPacket.getText());
    }

    public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.gui.setTimes(pPacket.getFadeIn(), pPacket.getStay(), pPacket.getFadeOut());
    }

    public void handleTabListCustomisation(ClientboundTabListPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.gui.getTabList().setHeader(pPacket.getHeader().getString().isEmpty() ? null : pPacket.getHeader());
        this.minecraft.gui.getTabList().setFooter(pPacket.getFooter().getString().isEmpty() ? null : pPacket.getFooter());
    }

    public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = pPacket.getEntity(this.level);

        if (entity instanceof LivingEntity)
        {
            ((LivingEntity)entity).removeEffectNoUpdate(pPacket.getEffect());
        }
    }

    public void handlePlayerInfo(ClientboundPlayerInfoPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        for (ClientboundPlayerInfoPacket.PlayerUpdate clientboundplayerinfopacket$playerupdate : pPacket.getEntries())
        {
            if (pPacket.getAction() == ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER)
            {
                this.minecraft.getPlayerSocialManager().removePlayer(clientboundplayerinfopacket$playerupdate.getProfile().getId());
                this.playerInfoMap.remove(clientboundplayerinfopacket$playerupdate.getProfile().getId());
            }
            else
            {
                PlayerInfo playerinfo = this.playerInfoMap.get(clientboundplayerinfopacket$playerupdate.getProfile().getId());

                if (pPacket.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER && playerinfo == null)
                {
                    boolean flag = Util.mapNullable(this.minecraft.getCurrentServer(), ServerData::enforcesSecureChat, false);
                    playerinfo = new PlayerInfo(clientboundplayerinfopacket$playerupdate, this.minecraft.getServiceSignatureValidator(), flag);
                    this.playerInfoMap.put(playerinfo.getProfile().getId(), playerinfo);
                    this.minecraft.getPlayerSocialManager().addPlayer(playerinfo);
                }

                if (playerinfo != null)
                {
                    switch (pPacket.getAction())
                    {
                        case ADD_PLAYER:
                            playerinfo.setGameMode(clientboundplayerinfopacket$playerupdate.getGameMode());
                            playerinfo.setLatency(clientboundplayerinfopacket$playerupdate.getLatency());
                            playerinfo.setTabListDisplayName(clientboundplayerinfopacket$playerupdate.getDisplayName());
                            break;

                        case UPDATE_GAME_MODE:
                            playerinfo.setGameMode(clientboundplayerinfopacket$playerupdate.getGameMode());
                            break;

                        case UPDATE_LATENCY:
                            playerinfo.setLatency(clientboundplayerinfopacket$playerupdate.getLatency());
                            break;

                        case UPDATE_DISPLAY_NAME:
                            playerinfo.setTabListDisplayName(clientboundplayerinfopacket$playerupdate.getDisplayName());
                    }
                }
            }
        }
    }

    public void handleKeepAlive(ClientboundKeepAlivePacket pPacket)
    {
        this.send(new ServerboundKeepAlivePacket(pPacket.getId()));
    }

    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Player player = this.minecraft.player;
        player.getAbilities().flying = pPacket.isFlying();
        player.getAbilities().instabuild = pPacket.canInstabuild();
        player.getAbilities().invulnerable = pPacket.isInvulnerable();
        player.getAbilities().mayfly = pPacket.canFly();
        player.getAbilities().setFlyingSpeed(pPacket.getFlyingSpeed());
        player.getAbilities().setWalkingSpeed(pPacket.getWalkingSpeed());
    }

    public void handleSoundEvent(ClientboundSoundPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.level.playSeededSound(this.minecraft.player, pPacket.getX(), pPacket.getY(), pPacket.getZ(), pPacket.getSound(), pPacket.getSource(), pPacket.getVolume(), pPacket.getPitch(), pPacket.getSeed());
    }

    public void handleSoundEntityEvent(ClientboundSoundEntityPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getId());

        if (entity != null)
        {
            this.minecraft.level.playSeededSound(this.minecraft.player, entity, pPacket.getSound(), pPacket.getSource(), pPacket.getVolume(), pPacket.getPitch(), pPacket.getSeed());
        }
    }

    public void handleCustomSoundEvent(ClientboundCustomSoundPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.getSoundManager().play(new SimpleSoundInstance(pPacket.getName(), pPacket.getSource(), pPacket.getVolume(), pPacket.getPitch(), RandomSource.create(pPacket.getSeed()), false, 0, SoundInstance.Attenuation.LINEAR, pPacket.getX(), pPacket.getY(), pPacket.getZ(), false));
    }

    public void handleResourcePack(ClientboundResourcePackPacket pPacket)
    {
        URL url = parseResourcePackUrl(pPacket.getUrl());

        if (url == null)
        {
            this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
        }
        else
        {
            String s = pPacket.getHash();
            boolean flag = pPacket.isRequired();
            ServerData serverdata = this.minecraft.getCurrentServer();

            if (serverdata != null && serverdata.getResourcePackStatus() == ServerData.ServerPackStatus.ENABLED)
            {
                this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                this.downloadCallback(this.minecraft.getClientPackSource().downloadAndSelectResourcePack(url, s, true));
            }
            else if (serverdata != null && serverdata.getResourcePackStatus() != ServerData.ServerPackStatus.PROMPT && (!flag || serverdata.getResourcePackStatus() != ServerData.ServerPackStatus.DISABLED))
            {
                this.send(ServerboundResourcePackPacket.Action.DECLINED);

                if (flag)
                {
                    this.connection.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                }
            }
            else
            {
                this.minecraft.execute(() ->
                {
                    this.minecraft.setScreen(new ConfirmScreen((p_233690_) -> {
                        this.minecraft.setScreen((Screen)null);
                        ServerData serverdata1 = this.minecraft.getCurrentServer();

                        if (p_233690_)
                        {
                            if (serverdata1 != null)
                            {
                                serverdata1.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
                            }

                            this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                            this.downloadCallback(this.minecraft.getClientPackSource().downloadAndSelectResourcePack(url, s, true));
                        }
                        else {
                            this.send(ServerboundResourcePackPacket.Action.DECLINED);

                            if (flag)
                            {
                                this.connection.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                            }
                            else if (serverdata1 != null)
                            {
                                serverdata1.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
                            }
                        }

                        if (serverdata1 != null)
                        {
                            ServerList.saveSingleServer(serverdata1);
                        }
                    }, flag ? Component.translatable("multiplayer.requiredTexturePrompt.line1") : Component.translatable("multiplayer.texturePrompt.line1"), preparePackPrompt(flag ? Component.translatable("multiplayer.requiredTexturePrompt.line2").a(ChatFormatting.YELLOW, ChatFormatting.BOLD) : Component.translatable("multiplayer.texturePrompt.line2"), pPacket.getPrompt()), flag ? CommonComponents.GUI_PROCEED : CommonComponents.GUI_YES, (Component)(flag ? Component.translatable("menu.disconnect") : CommonComponents.GUI_NO)));
                });
            }
        }
    }

    private static Component preparePackPrompt(Component p_171760_, @Nullable Component p_171761_)
    {
        return (Component)(p_171761_ == null ? p_171760_ : Component.a("multiplayer.texturePrompt.serverPrompt", p_171760_, p_171761_));
    }

    @Nullable
    private static URL parseResourcePackUrl(String p_233710_)
    {
        try
        {
            URL url = new URL(p_233710_);
            String s = url.getProtocol();
            return !"http".equals(s) && !"https".equals(s) ? null : url;
        }
        catch (MalformedURLException malformedurlexception)
        {
            return null;
        }
    }

    private void downloadCallback(CompletableFuture<?> pFuture)
    {
        pFuture.thenRun(() ->
        {
            this.send(ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED);
        }).exceptionally((p_233680_) ->
        {
            this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
            return null;
        });
    }

    private void send(ServerboundResourcePackPacket.Action pPacket)
    {
        this.connection.send(new ServerboundResourcePackPacket(pPacket));
    }

    public void handleBossUpdate(ClientboundBossEventPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.gui.getBossOverlay().update(pPacket);
    }

    public void handleItemCooldown(ClientboundCooldownPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        if (pPacket.getDuration() == 0)
        {
            this.minecraft.player.getCooldowns().removeCooldown(pPacket.getItem());
        }
        else
        {
            this.minecraft.player.getCooldowns().addCooldown(pPacket.getItem(), pPacket.getDuration());
        }
    }

    public void handleMoveVehicle(ClientboundMoveVehiclePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.minecraft.player.getRootVehicle();

        if (entity != this.minecraft.player && entity.isControlledByLocalInstance())
        {
            entity.absMoveTo(pPacket.getX(), pPacket.getY(), pPacket.getZ(), pPacket.getYRot(), pPacket.getXRot());
            this.connection.send(new ServerboundMoveVehiclePacket(entity));
        }
    }

    public void handleOpenBook(ClientboundOpenBookPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        ItemStack itemstack = this.minecraft.player.getItemInHand(pPacket.getHand());

        if (itemstack.is(Items.WRITTEN_BOOK))
        {
            this.minecraft.setScreen(new BookViewScreen(new BookViewScreen.WrittenBookAccess(itemstack)));
        }
    }

    public void handleCustomPayload(ClientboundCustomPayloadPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        ResourceLocation resourcelocation = pPacket.getIdentifier();
        FriendlyByteBuf friendlybytebuf = null;

        try
        {
            friendlybytebuf = pPacket.getData();

            if (ClientboundCustomPayloadPacket.BRAND.equals(resourcelocation))
            {
                String s = friendlybytebuf.readUtf();
                this.minecraft.player.setServerBrand(s);
                this.telemetryManager.onServerBrandReceived(s);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_PATHFINDING_PACKET.equals(resourcelocation))
            {
                int k1 = friendlybytebuf.readInt();
                float f = friendlybytebuf.readFloat();
                Path path = Path.createFromStream(friendlybytebuf);
                this.minecraft.debugRenderer.pathfindingRenderer.addPath(k1, path, f);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_NEIGHBORSUPDATE_PACKET.equals(resourcelocation))
            {
                long l1 = friendlybytebuf.readVarLong();
                BlockPos blockpos8 = friendlybytebuf.readBlockPos();
                ((NeighborsUpdateRenderer)this.minecraft.debugRenderer.neighborsUpdateRenderer).addUpdate(l1, blockpos8);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_STRUCTURES_PACKET.equals(resourcelocation))
            {
                DimensionType dimensiontype = (DimensionType)this.registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).get(friendlybytebuf.readResourceLocation());
                BoundingBox boundingbox = new BoundingBox(friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt());
                int l3 = friendlybytebuf.readInt();
                List<BoundingBox> list = Lists.newArrayList();
                List<Boolean> list1 = Lists.newArrayList();

                for (int i = 0; i < l3; ++i)
                {
                    list.add(new BoundingBox(friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt()));
                    list1.add(friendlybytebuf.readBoolean());
                }

                this.minecraft.debugRenderer.structureRenderer.addBoundingBox(boundingbox, list, list1, dimensiontype);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_WORLDGENATTEMPT_PACKET.equals(resourcelocation))
            {
                ((WorldGenAttemptRenderer)this.minecraft.debugRenderer.worldGenAttemptRenderer).addPos(friendlybytebuf.readBlockPos(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat());
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_VILLAGE_SECTIONS.equals(resourcelocation))
            {
                int i2 = friendlybytebuf.readInt();

                for (int k2 = 0; k2 < i2; ++k2)
                {
                    this.minecraft.debugRenderer.villageSectionsDebugRenderer.setVillageSection(friendlybytebuf.readSectionPos());
                }

                int l2 = friendlybytebuf.readInt();

                for (int i4 = 0; i4 < l2; ++i4)
                {
                    this.minecraft.debugRenderer.villageSectionsDebugRenderer.setNotVillageSection(friendlybytebuf.readSectionPos());
                }
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_POI_ADDED_PACKET.equals(resourcelocation))
            {
                BlockPos blockpos2 = friendlybytebuf.readBlockPos();
                String s9 = friendlybytebuf.readUtf();
                int j4 = friendlybytebuf.readInt();
                BrainDebugRenderer.PoiInfo braindebugrenderer$poiinfo = new BrainDebugRenderer.PoiInfo(blockpos2, s9, j4);
                this.minecraft.debugRenderer.brainDebugRenderer.addPoi(braindebugrenderer$poiinfo);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_POI_REMOVED_PACKET.equals(resourcelocation))
            {
                BlockPos blockpos3 = friendlybytebuf.readBlockPos();
                this.minecraft.debugRenderer.brainDebugRenderer.removePoi(blockpos3);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_POI_TICKET_COUNT_PACKET.equals(resourcelocation))
            {
                BlockPos blockpos4 = friendlybytebuf.readBlockPos();
                int i3 = friendlybytebuf.readInt();
                this.minecraft.debugRenderer.brainDebugRenderer.setFreeTicketCount(blockpos4, i3);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_GOAL_SELECTOR.equals(resourcelocation))
            {
                BlockPos blockpos5 = friendlybytebuf.readBlockPos();
                int j3 = friendlybytebuf.readInt();
                int k4 = friendlybytebuf.readInt();
                List<GoalSelectorDebugRenderer.DebugGoal> list2 = Lists.newArrayList();

                for (int i6 = 0; i6 < k4; ++i6)
                {
                    int j6 = friendlybytebuf.readInt();
                    boolean flag = friendlybytebuf.readBoolean();
                    String s1 = friendlybytebuf.readUtf(255);
                    list2.add(new GoalSelectorDebugRenderer.DebugGoal(blockpos5, j6, s1, flag));
                }

                this.minecraft.debugRenderer.goalSelectorRenderer.addGoalSelector(j3, list2);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_RAIDS.equals(resourcelocation))
            {
                int j2 = friendlybytebuf.readInt();
                Collection<BlockPos> collection = Lists.newArrayList();

                for (int l4 = 0; l4 < j2; ++l4)
                {
                    collection.add(friendlybytebuf.readBlockPos());
                }

                this.minecraft.debugRenderer.raidDebugRenderer.setRaidCenters(collection);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_BRAIN.equals(resourcelocation))
            {
                double d0 = friendlybytebuf.readDouble();
                double d2 = friendlybytebuf.readDouble();
                double d4 = friendlybytebuf.readDouble();
                Position position = new PositionImpl(d0, d2, d4);
                UUID uuid = friendlybytebuf.readUUID();
                int j = friendlybytebuf.readInt();
                String s2 = friendlybytebuf.readUtf();
                String s3 = friendlybytebuf.readUtf();
                int k = friendlybytebuf.readInt();
                float f1 = friendlybytebuf.readFloat();
                float f2 = friendlybytebuf.readFloat();
                String s4 = friendlybytebuf.readUtf();
                Path path1 = friendlybytebuf.readNullable(Path::createFromStream);
                boolean flag1 = friendlybytebuf.readBoolean();
                int l = friendlybytebuf.readInt();
                BrainDebugRenderer.BrainDump braindebugrenderer$braindump = new BrainDebugRenderer.BrainDump(uuid, j, s2, s3, k, f1, f2, position, s4, path1, flag1, l);
                int i1 = friendlybytebuf.readVarInt();

                for (int j1 = 0; j1 < i1; ++j1)
                {
                    String s5 = friendlybytebuf.readUtf();
                    braindebugrenderer$braindump.activities.add(s5);
                }

                int i8 = friendlybytebuf.readVarInt();

                for (int j8 = 0; j8 < i8; ++j8)
                {
                    String s6 = friendlybytebuf.readUtf();
                    braindebugrenderer$braindump.behaviors.add(s6);
                }

                int k8 = friendlybytebuf.readVarInt();

                for (int l8 = 0; l8 < k8; ++l8)
                {
                    String s7 = friendlybytebuf.readUtf();
                    braindebugrenderer$braindump.memories.add(s7);
                }

                int i9 = friendlybytebuf.readVarInt();

                for (int j9 = 0; j9 < i9; ++j9)
                {
                    BlockPos blockpos = friendlybytebuf.readBlockPos();
                    braindebugrenderer$braindump.pois.add(blockpos);
                }

                int k9 = friendlybytebuf.readVarInt();

                for (int l9 = 0; l9 < k9; ++l9)
                {
                    BlockPos blockpos1 = friendlybytebuf.readBlockPos();
                    braindebugrenderer$braindump.potentialPois.add(blockpos1);
                }

                int i10 = friendlybytebuf.readVarInt();

                for (int j10 = 0; j10 < i10; ++j10)
                {
                    String s8 = friendlybytebuf.readUtf();
                    braindebugrenderer$braindump.gossips.add(s8);
                }

                this.minecraft.debugRenderer.brainDebugRenderer.addOrUpdateBrainDump(braindebugrenderer$braindump);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_BEE.equals(resourcelocation))
            {
                double d1 = friendlybytebuf.readDouble();
                double d3 = friendlybytebuf.readDouble();
                double d5 = friendlybytebuf.readDouble();
                Position position1 = new PositionImpl(d1, d3, d5);
                UUID uuid1 = friendlybytebuf.readUUID();
                int k6 = friendlybytebuf.readInt();
                BlockPos blockpos9 = friendlybytebuf.readNullable(FriendlyByteBuf::readBlockPos);
                BlockPos blockpos10 = friendlybytebuf.readNullable(FriendlyByteBuf::readBlockPos);
                int l6 = friendlybytebuf.readInt();
                Path path2 = friendlybytebuf.readNullable(Path::createFromStream);
                BeeDebugRenderer.BeeInfo beedebugrenderer$beeinfo = new BeeDebugRenderer.BeeInfo(uuid1, k6, position1, path2, blockpos9, blockpos10, l6);
                int i7 = friendlybytebuf.readVarInt();

                for (int j7 = 0; j7 < i7; ++j7)
                {
                    String s12 = friendlybytebuf.readUtf();
                    beedebugrenderer$beeinfo.goals.add(s12);
                }

                int k7 = friendlybytebuf.readVarInt();

                for (int l7 = 0; l7 < k7; ++l7)
                {
                    BlockPos blockpos11 = friendlybytebuf.readBlockPos();
                    beedebugrenderer$beeinfo.blacklistedHives.add(blockpos11);
                }

                this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateBeeInfo(beedebugrenderer$beeinfo);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_HIVE.equals(resourcelocation))
            {
                BlockPos blockpos6 = friendlybytebuf.readBlockPos();
                String s10 = friendlybytebuf.readUtf();
                int i5 = friendlybytebuf.readInt();
                int k5 = friendlybytebuf.readInt();
                boolean flag2 = friendlybytebuf.readBoolean();
                BeeDebugRenderer.HiveInfo beedebugrenderer$hiveinfo = new BeeDebugRenderer.HiveInfo(blockpos6, s10, i5, k5, flag2, this.level.getGameTime());
                this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateHiveInfo(beedebugrenderer$hiveinfo);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_CLEAR.equals(resourcelocation))
            {
                this.minecraft.debugRenderer.gameTestDebugRenderer.clear();
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER.equals(resourcelocation))
            {
                BlockPos blockpos7 = friendlybytebuf.readBlockPos();
                int k3 = friendlybytebuf.readInt();
                String s11 = friendlybytebuf.readUtf();
                int l5 = friendlybytebuf.readInt();
                this.minecraft.debugRenderer.gameTestDebugRenderer.addMarker(blockpos7, k3, s11, l5);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT.equals(resourcelocation))
            {
                GameEvent gameevent = Registry.GAME_EVENT.get(new ResourceLocation(friendlybytebuf.readUtf()));
                Vec3 vec3 = new Vec3(friendlybytebuf.readDouble(), friendlybytebuf.readDouble(), friendlybytebuf.readDouble());
                this.minecraft.debugRenderer.gameEventListenerRenderer.trackGameEvent(gameevent, vec3);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT_LISTENER.equals(resourcelocation))
            {
                ResourceLocation resourcelocation1 = friendlybytebuf.readResourceLocation();
                PositionSource positionsource = Registry.POSITION_SOURCE_TYPE.getOptional(resourcelocation1).orElseThrow(() ->
                {
                    return new IllegalArgumentException("Unknown position source type " + resourcelocation1);
                }).read(friendlybytebuf);
                int j5 = friendlybytebuf.readVarInt();
                this.minecraft.debugRenderer.gameEventListenerRenderer.trackListener(positionsource, j5);
            }
            else
            {
                LOGGER.warn("Unknown custom packed identifier: {}", (Object)resourcelocation);
            }
        }
        finally
        {
            if (friendlybytebuf != null)
            {
                friendlybytebuf.release();
            }
        }
    }

    public void handleAddObjective(ClientboundSetObjectivePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Scoreboard scoreboard = this.level.getScoreboard();
        String s = pPacket.getObjectiveName();

        if (pPacket.getMethod() == 0)
        {
            scoreboard.addObjective(s, ObjectiveCriteria.DUMMY, pPacket.getDisplayName(), pPacket.getRenderType());
        }
        else if (scoreboard.hasObjective(s))
        {
            Objective objective = scoreboard.getObjective(s);

            if (pPacket.getMethod() == 1)
            {
                scoreboard.removeObjective(objective);
            }
            else if (pPacket.getMethod() == 2)
            {
                objective.setRenderType(pPacket.getRenderType());
                objective.setDisplayName(pPacket.getDisplayName());
            }
        }
    }

    public void handleSetScore(ClientboundSetScorePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Scoreboard scoreboard = this.level.getScoreboard();
        String s = pPacket.getObjectiveName();

        switch (pPacket.getMethod())
        {
            case CHANGE:
                Objective objective = scoreboard.getOrCreateObjective(s);
                Score score = scoreboard.getOrCreatePlayerScore(pPacket.getOwner(), objective);
                score.setScore(pPacket.getScore());
                break;

            case REMOVE:
                scoreboard.resetPlayerScore(pPacket.getOwner(), scoreboard.getObjective(s));
        }
    }

    public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Scoreboard scoreboard = this.level.getScoreboard();
        String s = pPacket.getObjectiveName();
        Objective objective = s == null ? null : scoreboard.getOrCreateObjective(s);
        scoreboard.setDisplayObjective(pPacket.getSlot(), objective);
    }

    public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Scoreboard scoreboard = this.level.getScoreboard();
        ClientboundSetPlayerTeamPacket.Action clientboundsetplayerteampacket$action = pPacket.getTeamAction();
        PlayerTeam playerteam;

        if (clientboundsetplayerteampacket$action == ClientboundSetPlayerTeamPacket.Action.ADD)
        {
            playerteam = scoreboard.addPlayerTeam(pPacket.getName());
        }
        else
        {
            playerteam = scoreboard.getPlayerTeam(pPacket.getName());

            if (playerteam == null)
            {
                LOGGER.warn("Received packet for unknown team {}: team action: {}, player action: {}", pPacket.getName(), pPacket.getTeamAction(), pPacket.getPlayerAction());
                return;
            }
        }

        Optional<ClientboundSetPlayerTeamPacket.Parameters> optional = pPacket.getParameters();
        optional.ifPresent((p_233670_) ->
        {
            playerteam.setDisplayName(p_233670_.getDisplayName());
            playerteam.setColor(p_233670_.getColor());
            playerteam.unpackOptions(p_233670_.getOptions());
            Team.Visibility team$visibility = Team.Visibility.byName(p_233670_.getNametagVisibility());

            if (team$visibility != null)
            {
                playerteam.setNameTagVisibility(team$visibility);
            }

            Team.CollisionRule team$collisionrule = Team.CollisionRule.byName(p_233670_.getCollisionRule());

            if (team$collisionrule != null)
            {
                playerteam.setCollisionRule(team$collisionrule);
            }

            playerteam.setPlayerPrefix(p_233670_.getPlayerPrefix());
            playerteam.setPlayerSuffix(p_233670_.getPlayerSuffix());
        });
        ClientboundSetPlayerTeamPacket.Action clientboundsetplayerteampacket$action1 = pPacket.getPlayerAction();

        if (clientboundsetplayerteampacket$action1 == ClientboundSetPlayerTeamPacket.Action.ADD)
        {
            for (String s : pPacket.getPlayers())
            {
                scoreboard.addPlayerToTeam(s, playerteam);
            }
        }
        else if (clientboundsetplayerteampacket$action1 == ClientboundSetPlayerTeamPacket.Action.REMOVE)
        {
            for (String s1 : pPacket.getPlayers())
            {
                scoreboard.removePlayerFromTeam(s1, playerteam);
            }
        }

        if (clientboundsetplayerteampacket$action == ClientboundSetPlayerTeamPacket.Action.REMOVE)
        {
            scoreboard.removePlayerTeam(playerteam);
        }
    }

    public void handleParticleEvent(ClientboundLevelParticlesPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        if (pPacket.getCount() == 0)
        {
            double d0 = (double)(pPacket.getMaxSpeed() * pPacket.getXDist());
            double d2 = (double)(pPacket.getMaxSpeed() * pPacket.getYDist());
            double d4 = (double)(pPacket.getMaxSpeed() * pPacket.getZDist());

            try
            {
                this.level.addParticle(pPacket.getParticle(), pPacket.isOverrideLimiter(), pPacket.getX(), pPacket.getY(), pPacket.getZ(), d0, d2, d4);
            }
            catch (Throwable throwable1)
            {
                LOGGER.warn("Could not spawn particle effect {}", (Object)pPacket.getParticle());
            }
        }
        else
        {
            for (int i = 0; i < pPacket.getCount(); ++i)
            {
                double d1 = this.random.nextGaussian() * (double)pPacket.getXDist();
                double d3 = this.random.nextGaussian() * (double)pPacket.getYDist();
                double d5 = this.random.nextGaussian() * (double)pPacket.getZDist();
                double d6 = this.random.nextGaussian() * (double)pPacket.getMaxSpeed();
                double d7 = this.random.nextGaussian() * (double)pPacket.getMaxSpeed();
                double d8 = this.random.nextGaussian() * (double)pPacket.getMaxSpeed();

                try
                {
                    this.level.addParticle(pPacket.getParticle(), pPacket.isOverrideLimiter(), pPacket.getX() + d1, pPacket.getY() + d3, pPacket.getZ() + d5, d6, d7, d8);
                }
                catch (Throwable throwable)
                {
                    LOGGER.warn("Could not spawn particle effect {}", (Object)pPacket.getParticle());
                    return;
                }
            }
        }
    }

    public void handlePing(ClientboundPingPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.send(new ServerboundPongPacket(pPacket.getId()));
    }

    public void handleUpdateAttributes(ClientboundUpdateAttributesPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getEntityId());

        if (entity != null)
        {
            if (!(entity instanceof LivingEntity))
            {
                throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
            }
            else
            {
                AttributeMap attributemap = ((LivingEntity)entity).getAttributes();

                for (ClientboundUpdateAttributesPacket.AttributeSnapshot clientboundupdateattributespacket$attributesnapshot : pPacket.getValues())
                {
                    AttributeInstance attributeinstance = attributemap.getInstance(clientboundupdateattributespacket$attributesnapshot.getAttribute());

                    if (attributeinstance == null)
                    {
                        LOGGER.warn("Entity {} does not have attribute {}", entity, Registry.ATTRIBUTE.getKey(clientboundupdateattributespacket$attributesnapshot.getAttribute()));
                    }
                    else
                    {
                        attributeinstance.setBaseValue(clientboundupdateattributespacket$attributesnapshot.getBase());
                        attributeinstance.removeModifiers();

                        for (AttributeModifier attributemodifier : clientboundupdateattributespacket$attributesnapshot.getModifiers())
                        {
                            attributeinstance.addTransientModifier(attributemodifier);
                        }
                    }
                }
            }
        }
    }

    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        AbstractContainerMenu abstractcontainermenu = this.minecraft.player.containerMenu;

        if (abstractcontainermenu.containerId == pPacket.getContainerId())
        {
            this.recipeManager.byKey(pPacket.getRecipe()).ifPresent((p_233667_) ->
            {
                if (this.minecraft.screen instanceof RecipeUpdateListener)
                {
                    RecipeBookComponent recipebookcomponent = ((RecipeUpdateListener)this.minecraft.screen).getRecipeBookComponent();
                    recipebookcomponent.setupGhostRecipe(p_233667_, abstractcontainermenu.slots);
                }
            });
        }
    }

    public void handleLightUpdatePacket(ClientboundLightUpdatePacket p_194243_)
    {
        PacketUtils.ensureRunningOnSameThread(p_194243_, this, this.minecraft);
        int i = p_194243_.getX();
        int j = p_194243_.getZ();
        ClientboundLightUpdatePacketData clientboundlightupdatepacketdata = p_194243_.getLightData();
        this.level.queueLightUpdate(() ->
        {
            this.applyLightData(i, j, clientboundlightupdatepacketdata);
        });
    }

    private void applyLightData(int p_194249_, int p_194250_, ClientboundLightUpdatePacketData p_194251_)
    {
        LevelLightEngine levellightengine = this.level.getChunkSource().getLightEngine();
        BitSet bitset = p_194251_.getSkyYMask();
        BitSet bitset1 = p_194251_.getEmptySkyYMask();
        Iterator<byte[]> iterator = p_194251_.getSkyUpdates().iterator();
        this.readSectionList(p_194249_, p_194250_, levellightengine, LightLayer.SKY, bitset, bitset1, iterator, p_194251_.getTrustEdges());
        BitSet bitset2 = p_194251_.getBlockYMask();
        BitSet bitset3 = p_194251_.getEmptyBlockYMask();
        Iterator<byte[]> iterator1 = p_194251_.getBlockUpdates().iterator();
        this.readSectionList(p_194249_, p_194250_, levellightengine, LightLayer.BLOCK, bitset2, bitset3, iterator1, p_194251_.getTrustEdges());
        this.level.setLightReady(p_194249_, p_194250_);
    }

    public void handleMerchantOffers(ClientboundMerchantOffersPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        AbstractContainerMenu abstractcontainermenu = this.minecraft.player.containerMenu;

        if (pPacket.getContainerId() == abstractcontainermenu.containerId && abstractcontainermenu instanceof MerchantMenu merchantmenu)
        {
            merchantmenu.setOffers(new MerchantOffers(pPacket.getOffers().createTag()));
            merchantmenu.setXp(pPacket.getVillagerXp());
            merchantmenu.setMerchantLevel(pPacket.getVillagerLevel());
            merchantmenu.setShowProgressBar(pPacket.showProgress());
            merchantmenu.setCanRestock(pPacket.canRestock());
        }
    }

    public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.serverChunkRadius = pPacket.getRadius();
        this.minecraft.options.setServerRenderDistance(this.serverChunkRadius);
        this.level.getChunkSource().updateViewRadius(pPacket.getRadius());
    }

    public void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket p_194245_)
    {
        PacketUtils.ensureRunningOnSameThread(p_194245_, this, this.minecraft);
        this.serverSimulationDistance = p_194245_.simulationDistance();
        this.level.setServerSimulationDistance(this.serverSimulationDistance);
    }

    public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.level.getChunkSource().updateViewCenter(pPacket.getX(), pPacket.getZ());
    }

    public void handleBlockChangedAck(ClientboundBlockChangedAckPacket p_233698_)
    {
        PacketUtils.ensureRunningOnSameThread(p_233698_, this, this.minecraft);
        this.level.handleBlockChangedAck(p_233698_.sequence());
    }

    private void readSectionList(int p_171735_, int p_171736_, LevelLightEngine p_171737_, LightLayer p_171738_, BitSet p_171739_, BitSet p_171740_, Iterator<byte[]> p_171741_, boolean p_171742_)
    {
        for (int i = 0; i < p_171737_.getLightSectionCount(); ++i)
        {
            int j = p_171737_.getMinLightSection() + i;
            boolean flag = p_171739_.get(i);
            boolean flag1 = p_171740_.get(i);

            if (flag || flag1)
            {
                p_171737_.queueSectionData(p_171738_, SectionPos.of(p_171735_, j, p_171736_), flag ? new DataLayer((byte[])p_171741_.next().clone()) : new DataLayer(), p_171742_);
                this.level.setSectionDirtyWithNeighbors(p_171735_, j, p_171736_);
            }
        }
    }

    public Connection getConnection()
    {
        return this.connection;
    }

    public Collection<PlayerInfo> getOnlinePlayers()
    {
        return this.playerInfoMap.values();
    }

    public Collection<UUID> getOnlinePlayerIds()
    {
        return this.playerInfoMap.keySet();
    }

    @Nullable
    public PlayerInfo getPlayerInfo(UUID pName)
    {
        return this.playerInfoMap.get(pName);
    }

    @Nullable
    public PlayerInfo getPlayerInfo(String pName)
    {
        for (PlayerInfo playerinfo : this.playerInfoMap.values())
        {
            if (playerinfo.getProfile().getName().equals(pName))
            {
                return playerinfo;
            }
        }

        return null;
    }

    public GameProfile getLocalGameProfile()
    {
        return this.localGameProfile;
    }

    public ClientAdvancements getAdvancements()
    {
        return this.advancements;
    }

    public CommandDispatcher<SharedSuggestionProvider> getCommands()
    {
        return this.commands;
    }

    public ClientLevel getLevel()
    {
        return this.level;
    }

    public DebugQueryHandler getDebugQueryHandler()
    {
        return this.debugQueryHandler;
    }

    public UUID getId()
    {
        return this.id;
    }

    public Set<ResourceKey<Level>> levels()
    {
        return this.levels;
    }

    public RegistryAccess registryAccess()
    {
        return this.registryAccess;
    }

    public SignedMessageChain.Encoder signedMessageEncoder()
    {
        return this.signedMessageEncoder;
    }

    public LastSeenMessages.Update generateMessageAcknowledgements()
    {
        this.unacknowledgedReceivedMessageCount = 0;
        return new LastSeenMessages.Update(this.lastSeenMessagesTracker.get(), this.lastUnacknowledgedReceivedMessage);
    }

    public void markMessageAsProcessed(PlayerChatMessage p_242356_, boolean p_242455_)
    {
        LastSeenMessages.Entry lastseenmessages$entry = p_242356_.toLastSeenEntry();

        if (lastseenmessages$entry != null)
        {
            if (p_242455_)
            {
                this.lastSeenMessagesTracker.push(lastseenmessages$entry);
                this.lastUnacknowledgedReceivedMessage = Optional.empty();
            }
            else
            {
                this.lastUnacknowledgedReceivedMessage = Optional.of(lastseenmessages$entry);
            }

            if (this.unacknowledgedReceivedMessageCount++ > 64)
            {
                this.send(new ServerboundChatAckPacket(this.generateMessageAcknowledgements()));
            }
        }
    }
}
