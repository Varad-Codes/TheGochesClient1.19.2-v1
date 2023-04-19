package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.OutgoingPlayerChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public abstract class PlayerList
{
    public static final File USERBANLIST_FILE = new File("banned-players.json");
    public static final File IPBANLIST_FILE = new File("banned-ips.json");
    public static final File OPLIST_FILE = new File("ops.json");
    public static final File WHITELIST_FILE = new File("whitelist.json");
    public static final Component CHAT_FILTERED_FULL = Component.translatable("chat.filtered_full");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SEND_PLAYER_INFO_INTERVAL = 600;
    private static final SimpleDateFormat BAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    private final MinecraftServer server;
    private final List<ServerPlayer> players = Lists.newArrayList();
    private final Map<UUID, ServerPlayer> playersByUUID = Maps.newHashMap();
    private final UserBanList bans = new UserBanList(USERBANLIST_FILE);
    private final IpBanList ipBans = new IpBanList(IPBANLIST_FILE);
    private final ServerOpList ops = new ServerOpList(OPLIST_FILE);
    private final UserWhiteList whitelist = new UserWhiteList(WHITELIST_FILE);
    private final Map<UUID, ServerStatsCounter> stats = Maps.newHashMap();
    private final Map<UUID, PlayerAdvancements> advancements = Maps.newHashMap();
    private final PlayerDataStorage playerIo;
    private boolean doWhiteList;
    private final RegistryAccess.Frozen registryHolder;
    protected final int maxPlayers;
    private int viewDistance;
    private int simulationDistance;
    private boolean allowCheatsForAllPlayers;
    private static final boolean ALLOW_LOGOUTIVATOR = false;
    private int sendAllPlayerInfoIn;

    public PlayerList(MinecraftServer p_203842_, RegistryAccess.Frozen p_203843_, PlayerDataStorage p_203844_, int p_203845_)
    {
        this.server = p_203842_;
        this.registryHolder = p_203843_;
        this.maxPlayers = p_203845_;
        this.playerIo = p_203844_;
    }

    public void placeNewPlayer(Connection pNetManager, ServerPlayer pPlayer)
    {
        GameProfile gameprofile = pPlayer.getGameProfile();
        GameProfileCache gameprofilecache = this.server.getProfileCache();
        Optional<GameProfile> optional = gameprofilecache.get(gameprofile.getId());
        String s = optional.map(GameProfile::getName).orElse(gameprofile.getName());
        gameprofilecache.add(gameprofile);
        CompoundTag compoundtag = this.load(pPlayer);
        ResourceKey<Level> resourcekey = compoundtag != null ? DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, compoundtag.get("Dimension"))).resultOrPartial(LOGGER::error).orElse(Level.OVERWORLD) : Level.OVERWORLD;
        ServerLevel serverlevel = this.server.getLevel(resourcekey);
        ServerLevel serverlevel1;

        if (serverlevel == null)
        {
            LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", (Object)resourcekey);
            serverlevel1 = this.server.overworld();
        }
        else
        {
            serverlevel1 = serverlevel;
        }

        pPlayer.setLevel(serverlevel1);
        String s1 = "local";

        if (pNetManager.getRemoteAddress() != null)
        {
            s1 = pNetManager.getRemoteAddress().toString();
        }

        LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", pPlayer.getName().getString(), s1, pPlayer.getId(), pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
        LevelData leveldata = serverlevel1.getLevelData();
        pPlayer.loadGameTypes(compoundtag);
        ServerGamePacketListenerImpl servergamepacketlistenerimpl = new ServerGamePacketListenerImpl(this.server, pNetManager, pPlayer);
        GameRules gamerules = serverlevel1.getGameRules();
        boolean flag = gamerules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
        boolean flag1 = gamerules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
        servergamepacketlistenerimpl.send(new ClientboundLoginPacket(pPlayer.getId(), leveldata.isHardcore(), pPlayer.gameMode.getGameModeForPlayer(), pPlayer.gameMode.getPreviousGameModeForPlayer(), this.server.levelKeys(), this.registryHolder, serverlevel1.dimensionTypeId(), serverlevel1.dimension(), BiomeManager.obfuscateSeed(serverlevel1.getSeed()), this.getMaxPlayers(), this.viewDistance, this.simulationDistance, flag1, !flag, serverlevel1.isDebug(), serverlevel1.isFlat(), pPlayer.getLastDeathLocation()));
        servergamepacketlistenerimpl.send(new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.BRAND, (new FriendlyByteBuf(Unpooled.buffer())).writeUtf(this.getServer().getServerModName())));
        servergamepacketlistenerimpl.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
        servergamepacketlistenerimpl.send(new ClientboundPlayerAbilitiesPacket(pPlayer.getAbilities()));
        servergamepacketlistenerimpl.send(new ClientboundSetCarriedItemPacket(pPlayer.getInventory().selected));
        servergamepacketlistenerimpl.send(new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes()));
        servergamepacketlistenerimpl.send(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registryHolder)));
        this.sendPlayerPermissionLevel(pPlayer);
        pPlayer.getStats().markAllDirty();
        pPlayer.getRecipeBook().sendInitialRecipeBook(pPlayer);
        this.updateEntireScoreboard(serverlevel1.getScoreboard(), pPlayer);
        this.server.invalidateStatus();
        MutableComponent mutablecomponent;

        if (pPlayer.getGameProfile().getName().equalsIgnoreCase(s))
        {
            mutablecomponent = Component.a("multiplayer.player.joined", pPlayer.getDisplayName());
        }
        else
        {
            mutablecomponent = Component.a("multiplayer.player.joined.renamed", pPlayer.getDisplayName(), s);
        }

        this.broadcastSystemMessage(mutablecomponent.withStyle(ChatFormatting.YELLOW), false);
        servergamepacketlistenerimpl.teleport(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), pPlayer.getYRot(), pPlayer.getXRot());
        this.players.add(pPlayer);
        this.playersByUUID.put(pPlayer.getUUID(), pPlayer);
        this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, pPlayer));

        for (int i = 0; i < this.players.size(); ++i)
        {
            pPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this.players.get(i)));
        }

        serverlevel1.addNewPlayer(pPlayer);
        this.server.getCustomBossEvents().onPlayerConnect(pPlayer);
        this.sendLevelInfo(pPlayer, serverlevel1);
        this.server.getServerResourcePack().ifPresent((p_215606_) ->
        {
            pPlayer.sendTexturePack(p_215606_.url(), p_215606_.hash(), p_215606_.isRequired(), p_215606_.prompt());
        });
        pPlayer.sendServerStatus(this.server.getStatus());

        for (MobEffectInstance mobeffectinstance : pPlayer.getActiveEffects())
        {
            servergamepacketlistenerimpl.send(new ClientboundUpdateMobEffectPacket(pPlayer.getId(), mobeffectinstance));
        }

        if (compoundtag != null && compoundtag.contains("RootVehicle", 10))
        {
            CompoundTag compoundtag1 = compoundtag.getCompound("RootVehicle");
            Entity entity1 = EntityType.loadEntityRecursive(compoundtag1.getCompound("Entity"), serverlevel1, (p_215603_) ->
            {
                return !serverlevel1.addWithUUID(p_215603_) ? null : p_215603_;
            });

            if (entity1 != null)
            {
                UUID uuid;

                if (compoundtag1.hasUUID("Attach"))
                {
                    uuid = compoundtag1.getUUID("Attach");
                }
                else
                {
                    uuid = null;
                }

                if (entity1.getUUID().equals(uuid))
                {
                    pPlayer.startRiding(entity1, true);
                }
                else
                {
                    for (Entity entity : entity1.getIndirectPassengers())
                    {
                        if (entity.getUUID().equals(uuid))
                        {
                            pPlayer.startRiding(entity, true);
                            break;
                        }
                    }
                }

                if (!pPlayer.isPassenger())
                {
                    LOGGER.warn("Couldn't reattach entity to player");
                    entity1.discard();

                    for (Entity entity2 : entity1.getIndirectPassengers())
                    {
                        entity2.discard();
                    }
                }
            }
        }

        pPlayer.initInventoryMenu();
    }

    protected void updateEntireScoreboard(ServerScoreboard pScoreboard, ServerPlayer pPlayer)
    {
        Set<Objective> set = Sets.newHashSet();

        for (PlayerTeam playerteam : pScoreboard.getPlayerTeams())
        {
            pPlayer.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerteam, true));
        }

        for (int i = 0; i < 19; ++i)
        {
            Objective objective = pScoreboard.getDisplayObjective(i);

            if (objective != null && !set.contains(objective))
            {
                for (Packet<?> packet : pScoreboard.getStartTrackingPackets(objective))
                {
                    pPlayer.connection.send(packet);
                }

                set.add(objective);
            }
        }
    }

    public void addWorldborderListener(ServerLevel p_184210_)
    {
        p_184210_.getWorldBorder().addListener(new BorderChangeListener()
        {
            public void onBorderSizeSet(WorldBorder p_11321_, double p_11322_)
            {
                PlayerList.this.broadcastAll(new ClientboundSetBorderSizePacket(p_11321_));
            }
            public void onBorderSizeLerping(WorldBorder p_11328_, double p_11329_, double p_11330_, long p_11331_)
            {
                PlayerList.this.broadcastAll(new ClientboundSetBorderLerpSizePacket(p_11328_));
            }
            public void onBorderCenterSet(WorldBorder p_11324_, double p_11325_, double p_11326_)
            {
                PlayerList.this.broadcastAll(new ClientboundSetBorderCenterPacket(p_11324_));
            }
            public void onBorderSetWarningTime(WorldBorder p_11333_, int p_11334_)
            {
                PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDelayPacket(p_11333_));
            }
            public void onBorderSetWarningBlocks(WorldBorder p_11339_, int p_11340_)
            {
                PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDistancePacket(p_11339_));
            }
            public void onBorderSetDamagePerBlock(WorldBorder p_11336_, double p_11337_)
            {
            }
            public void onBorderSetDamageSafeZOne(WorldBorder p_11342_, double p_11343_)
            {
            }
        });
    }

    @Nullable
    public CompoundTag load(ServerPlayer pPlayer)
    {
        CompoundTag compoundtag = this.server.getWorldData().getLoadedPlayerTag();
        CompoundTag compoundtag1;

        if (this.server.isSingleplayerOwner(pPlayer.getGameProfile()) && compoundtag != null)
        {
            compoundtag1 = compoundtag;
            pPlayer.load(compoundtag);
            LOGGER.debug("loading single player");
        }
        else
        {
            compoundtag1 = this.playerIo.load(pPlayer);
        }

        return compoundtag1;
    }

    protected void save(ServerPlayer pPlayer)
    {
        this.playerIo.save(pPlayer);
        ServerStatsCounter serverstatscounter = this.stats.get(pPlayer.getUUID());

        if (serverstatscounter != null)
        {
            serverstatscounter.save();
        }

        PlayerAdvancements playeradvancements = this.advancements.get(pPlayer.getUUID());

        if (playeradvancements != null)
        {
            playeradvancements.save();
        }
    }

    public void remove(ServerPlayer pPlayer)
    {
        ServerLevel serverlevel = pPlayer.getLevel();
        pPlayer.awardStat(Stats.LEAVE_GAME);
        this.save(pPlayer);

        if (pPlayer.isPassenger())
        {
            Entity entity = pPlayer.getRootVehicle();

            if (entity.hasExactlyOnePlayerPassenger())
            {
                LOGGER.debug("Removing player mount");
                pPlayer.stopRiding();
                entity.getPassengersAndSelf().forEach((p_215620_) ->
                {
                    p_215620_.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
                });
            }
        }

        pPlayer.unRide();
        serverlevel.removePlayerImmediately(pPlayer, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        pPlayer.getAdvancements().stopListening();
        this.players.remove(pPlayer);
        this.server.getCustomBossEvents().onPlayerDisconnect(pPlayer);
        UUID uuid = pPlayer.getUUID();
        ServerPlayer serverplayer = this.playersByUUID.get(uuid);

        if (serverplayer == pPlayer)
        {
            this.playersByUUID.remove(uuid);
            this.stats.remove(uuid);
            this.advancements.remove(uuid);
        }

        this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, pPlayer));
    }

    @Nullable
    public Component canPlayerLogin(SocketAddress pSocketAddress, GameProfile pGameProfile)
    {
        if (this.bans.isBanned(pGameProfile))
        {
            UserBanListEntry userbanlistentry = this.bans.get(pGameProfile);
            MutableComponent mutablecomponent1 = Component.a("multiplayer.disconnect.banned.reason", userbanlistentry.getReason());

            if (userbanlistentry.getExpires() != null)
            {
                mutablecomponent1.append(Component.a("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(userbanlistentry.getExpires())));
            }

            return mutablecomponent1;
        }
        else if (!this.isWhiteListed(pGameProfile))
        {
            return Component.translatable("multiplayer.disconnect.not_whitelisted");
        }
        else if (this.ipBans.isBanned(pSocketAddress))
        {
            IpBanListEntry ipbanlistentry = this.ipBans.get(pSocketAddress);
            MutableComponent mutablecomponent = Component.a("multiplayer.disconnect.banned_ip.reason", ipbanlistentry.getReason());

            if (ipbanlistentry.getExpires() != null)
            {
                mutablecomponent.append(Component.a("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(ipbanlistentry.getExpires())));
            }

            return mutablecomponent;
        }
        else
        {
            return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(pGameProfile) ? Component.translatable("multiplayer.disconnect.server_full") : null;
        }
    }

    public ServerPlayer getPlayerForLogin(GameProfile p_215625_, @Nullable ProfilePublicKey p_215626_)
    {
        UUID uuid = UUIDUtil.getOrCreatePlayerUUID(p_215625_);
        List<ServerPlayer> list = Lists.newArrayList();

        for (int i = 0; i < this.players.size(); ++i)
        {
            ServerPlayer serverplayer = this.players.get(i);

            if (serverplayer.getUUID().equals(uuid))
            {
                list.add(serverplayer);
            }
        }

        ServerPlayer serverplayer2 = this.playersByUUID.get(p_215625_.getId());

        if (serverplayer2 != null && !list.contains(serverplayer2))
        {
            list.add(serverplayer2);
        }

        for (ServerPlayer serverplayer1 : list)
        {
            serverplayer1.connection.disconnect(Component.translatable("multiplayer.disconnect.duplicate_login"));
        }

        return new ServerPlayer(this.server, this.server.overworld(), p_215625_, p_215626_);
    }

    public ServerPlayer respawn(ServerPlayer p_11237_, boolean p_11238_)
    {
        this.players.remove(p_11237_);
        p_11237_.getLevel().removePlayerImmediately(p_11237_, Entity.RemovalReason.DISCARDED);
        BlockPos blockpos = p_11237_.getRespawnPosition();
        float f = p_11237_.getRespawnAngle();
        boolean flag = p_11237_.isRespawnForced();
        ServerLevel serverlevel = this.server.getLevel(p_11237_.getRespawnDimension());
        Optional<Vec3> optional;

        if (serverlevel != null && blockpos != null)
        {
            optional = Player.findRespawnPositionAndUseSpawnBlock(serverlevel, blockpos, f, flag, p_11238_);
        }
        else
        {
            optional = Optional.empty();
        }

        ServerLevel serverlevel1 = serverlevel != null && optional.isPresent() ? serverlevel : this.server.overworld();
        ServerPlayer serverplayer = new ServerPlayer(this.server, serverlevel1, p_11237_.getGameProfile(), p_11237_.getProfilePublicKey());
        serverplayer.connection = p_11237_.connection;
        serverplayer.restoreFrom(p_11237_, p_11238_);
        serverplayer.setId(p_11237_.getId());
        serverplayer.setMainArm(p_11237_.getMainArm());

        for (String s : p_11237_.getTags())
        {
            serverplayer.addTag(s);
        }

        boolean flag2 = false;

        if (optional.isPresent())
        {
            BlockState blockstate = serverlevel1.getBlockState(blockpos);
            boolean flag1 = blockstate.is(Blocks.RESPAWN_ANCHOR);
            Vec3 vec3 = optional.get();
            float f1;

            if (!blockstate.is(BlockTags.BEDS) && !flag1)
            {
                f1 = f;
            }
            else
            {
                Vec3 vec31 = Vec3.atBottomCenterOf(blockpos).subtract(vec3).normalize();
                f1 = (float)Mth.wrapDegrees(Mth.atan2(vec31.z, vec31.x) * (double)(180F / (float)Math.PI) - 90.0D);
            }

            serverplayer.moveTo(vec3.x, vec3.y, vec3.z, f1, 0.0F);
            serverplayer.setRespawnPosition(serverlevel1.dimension(), blockpos, f, flag, false);
            flag2 = !p_11238_ && flag1;
        }
        else if (blockpos != null)
        {
            serverplayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
        }

        while (!serverlevel1.noCollision(serverplayer) && serverplayer.getY() < (double)serverlevel1.getMaxBuildHeight())
        {
            serverplayer.setPos(serverplayer.getX(), serverplayer.getY() + 1.0D, serverplayer.getZ());
        }

        LevelData leveldata = serverplayer.level.getLevelData();
        serverplayer.connection.send(new ClientboundRespawnPacket(serverplayer.level.dimensionTypeId(), serverplayer.level.dimension(), BiomeManager.obfuscateSeed(serverplayer.getLevel().getSeed()), serverplayer.gameMode.getGameModeForPlayer(), serverplayer.gameMode.getPreviousGameModeForPlayer(), serverplayer.getLevel().isDebug(), serverplayer.getLevel().isFlat(), p_11238_, serverplayer.getLastDeathLocation()));
        serverplayer.connection.teleport(serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), serverplayer.getYRot(), serverplayer.getXRot());
        serverplayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverlevel1.getSharedSpawnPos(), serverlevel1.getSharedSpawnAngle()));
        serverplayer.connection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
        serverplayer.connection.send(new ClientboundSetExperiencePacket(serverplayer.experienceProgress, serverplayer.totalExperience, serverplayer.experienceLevel));
        this.sendLevelInfo(serverplayer, serverlevel1);
        this.sendPlayerPermissionLevel(serverplayer);
        serverlevel1.addRespawnedPlayer(serverplayer);
        this.players.add(serverplayer);
        this.playersByUUID.put(serverplayer.getUUID(), serverplayer);
        serverplayer.initInventoryMenu();
        serverplayer.setHealth(serverplayer.getHealth());

        if (flag2)
        {
            serverplayer.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), 1.0F, 1.0F, serverlevel1.getRandom().nextLong()));
        }

        return serverplayer;
    }

    public void sendPlayerPermissionLevel(ServerPlayer pPlayer)
    {
        GameProfile gameprofile = pPlayer.getGameProfile();
        int i = this.server.getProfilePermissions(gameprofile);
        this.sendPlayerPermissionLevel(pPlayer, i);
    }

    public void tick()
    {
        if (++this.sendAllPlayerInfoIn > 600)
        {
            this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY, this.players));
            this.sendAllPlayerInfoIn = 0;
        }
    }

    public void broadcastAll(Packet<?> pPacket)
    {
        for (ServerPlayer serverplayer : this.players)
        {
            serverplayer.connection.send(pPacket);
        }
    }

    public void broadcastAll(Packet<?> p_11271_, ResourceKey<Level> p_11272_)
    {
        for (ServerPlayer serverplayer : this.players)
        {
            if (serverplayer.level.dimension() == p_11272_)
            {
                serverplayer.connection.send(p_11271_);
            }
        }
    }

    public void broadcastSystemToTeam(Player p_215622_, Component p_215623_)
    {
        Team team = p_215622_.getTeam();

        if (team != null)
        {
            for (String s : team.getPlayers())
            {
                ServerPlayer serverplayer = this.getPlayerByName(s);

                if (serverplayer != null && serverplayer != p_215622_)
                {
                    serverplayer.sendSystemMessage(p_215623_);
                }
            }
        }
    }

    public void broadcastSystemToAllExceptTeam(Player p_215650_, Component p_215651_)
    {
        Team team = p_215650_.getTeam();

        if (team == null)
        {
            this.broadcastSystemMessage(p_215651_, false);
        }
        else
        {
            for (int i = 0; i < this.players.size(); ++i)
            {
                ServerPlayer serverplayer = this.players.get(i);

                if (serverplayer.getTeam() != team)
                {
                    serverplayer.sendSystemMessage(p_215651_);
                }
            }
        }
    }

    public String[] getPlayerNamesArray()
    {
        String[] astring = new String[this.players.size()];

        for (int i = 0; i < this.players.size(); ++i)
        {
            astring[i] = this.players.get(i).getGameProfile().getName();
        }

        return astring;
    }

    public UserBanList getBans()
    {
        return this.bans;
    }

    public IpBanList getIpBans()
    {
        return this.ipBans;
    }

    public void op(GameProfile pProfile)
    {
        this.ops.add(new ServerOpListEntry(pProfile, this.server.getOperatorUserPermissionLevel(), this.ops.canBypassPlayerLimit(pProfile)));
        ServerPlayer serverplayer = this.getPlayer(pProfile.getId());

        if (serverplayer != null)
        {
            this.sendPlayerPermissionLevel(serverplayer);
        }
    }

    public void deop(GameProfile pProfile)
    {
        this.ops.remove(pProfile);
        ServerPlayer serverplayer = this.getPlayer(pProfile.getId());

        if (serverplayer != null)
        {
            this.sendPlayerPermissionLevel(serverplayer);
        }
    }

    private void sendPlayerPermissionLevel(ServerPlayer pPlayer, int pPermLevel)
    {
        if (pPlayer.connection != null)
        {
            byte b0;

            if (pPermLevel <= 0)
            {
                b0 = 24;
            }
            else if (pPermLevel >= 4)
            {
                b0 = 28;
            }
            else
            {
                b0 = (byte)(24 + pPermLevel);
            }

            pPlayer.connection.send(new ClientboundEntityEventPacket(pPlayer, b0));
        }

        this.server.getCommands().sendCommands(pPlayer);
    }

    public boolean isWhiteListed(GameProfile pProfile)
    {
        return !this.doWhiteList || this.ops.contains(pProfile) || this.whitelist.contains(pProfile);
    }

    public boolean isOp(GameProfile pProfile)
    {
        return this.ops.contains(pProfile) || this.server.isSingleplayerOwner(pProfile) && this.server.getWorldData().getAllowCommands() || this.allowCheatsForAllPlayers;
    }

    @Nullable
    public ServerPlayer getPlayerByName(String pUsername)
    {
        for (ServerPlayer serverplayer : this.players)
        {
            if (serverplayer.getGameProfile().getName().equalsIgnoreCase(pUsername))
            {
                return serverplayer;
            }
        }

        return null;
    }

    public void broadcast(@Nullable Player pExcept, double pX, double p_11244_, double pY, double p_11246_, ResourceKey<Level> pZ, Packet<?> p_11248_)
    {
        for (int i = 0; i < this.players.size(); ++i)
        {
            ServerPlayer serverplayer = this.players.get(i);

            if (serverplayer != pExcept && serverplayer.level.dimension() == pZ)
            {
                double d0 = pX - serverplayer.getX();
                double d1 = p_11244_ - serverplayer.getY();
                double d2 = pY - serverplayer.getZ();

                if (d0 * d0 + d1 * d1 + d2 * d2 < p_11246_ * p_11246_)
                {
                    serverplayer.connection.send(p_11248_);
                }
            }
        }
    }

    public void saveAll()
    {
        for (int i = 0; i < this.players.size(); ++i)
        {
            this.save(this.players.get(i));
        }
    }

    public UserWhiteList getWhiteList()
    {
        return this.whitelist;
    }

    public String[] getWhiteListNames()
    {
        return this.whitelist.getUserList();
    }

    public ServerOpList getOps()
    {
        return this.ops;
    }

    public String[] getOpNames()
    {
        return this.ops.getUserList();
    }

    public void reloadWhiteList()
    {
    }

    public void sendLevelInfo(ServerPlayer pPlayer, ServerLevel pLevel)
    {
        WorldBorder worldborder = this.server.overworld().getWorldBorder();
        pPlayer.connection.send(new ClientboundInitializeBorderPacket(worldborder));
        pPlayer.connection.send(new ClientboundSetTimePacket(pLevel.getGameTime(), pLevel.getDayTime(), pLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
        pPlayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(pLevel.getSharedSpawnPos(), pLevel.getSharedSpawnAngle()));

        if (pLevel.isRaining())
        {
            pPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
            pPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, pLevel.getRainLevel(1.0F)));
            pPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, pLevel.getThunderLevel(1.0F)));
        }
    }

    public void sendAllPlayerInfo(ServerPlayer pPlayer)
    {
        pPlayer.inventoryMenu.sendAllDataToRemote();
        pPlayer.resetSentInfo();
        pPlayer.connection.send(new ClientboundSetCarriedItemPacket(pPlayer.getInventory().selected));
    }

    public int getPlayerCount()
    {
        return this.players.size();
    }

    public int getMaxPlayers()
    {
        return this.maxPlayers;
    }

    public boolean isUsingWhitelist()
    {
        return this.doWhiteList;
    }

    public void setUsingWhiteList(boolean pWhitelistEnabled)
    {
        this.doWhiteList = pWhitelistEnabled;
    }

    public List<ServerPlayer> getPlayersWithAddress(String pAddress)
    {
        List<ServerPlayer> list = Lists.newArrayList();

        for (ServerPlayer serverplayer : this.players)
        {
            if (serverplayer.getIpAddress().equals(pAddress))
            {
                list.add(serverplayer);
            }
        }

        return list;
    }

    public int getViewDistance()
    {
        return this.viewDistance;
    }

    public int getSimulationDistance()
    {
        return this.simulationDistance;
    }

    public MinecraftServer getServer()
    {
        return this.server;
    }

    @Nullable
    public CompoundTag getSingleplayerData()
    {
        return null;
    }

    public void setAllowCheatsForAllPlayers(boolean p_11285_)
    {
        this.allowCheatsForAllPlayers = p_11285_;
    }

    public void removeAll()
    {
        for (int i = 0; i < this.players.size(); ++i)
        {
            (this.players.get(i)).connection.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
        }
    }

    public void broadcastSystemMessage(Component p_240618_, boolean p_240644_)
    {
        this.broadcastSystemMessage(p_240618_, (p_215639_) ->
        {
            return p_240618_;
        }, p_240644_);
    }

    public void broadcastSystemMessage(Component p_240526_, Function<ServerPlayer, Component> p_240594_, boolean p_240648_)
    {
        this.server.sendSystemMessage(p_240526_);

        for (ServerPlayer serverplayer : this.players)
        {
            Component component = p_240594_.apply(serverplayer);

            if (component != null)
            {
                serverplayer.sendSystemMessage(component, p_240648_);
            }
        }
    }

    public void broadcastChatMessage(PlayerChatMessage p_243229_, CommandSourceStack p_243254_, ChatType.Bound p_243255_)
    {
        this.broadcastChatMessage(p_243229_, p_243254_::shouldFilterMessageTo, p_243254_.getPlayer(), p_243254_.asChatSender(), p_243255_);
    }

    public void broadcastChatMessage(PlayerChatMessage p_243264_, ServerPlayer p_243234_, ChatType.Bound p_243204_)
    {
        this.broadcastChatMessage(p_243264_, p_243234_::shouldFilterMessageTo, p_243234_, p_243234_.asChatSender(), p_243204_);
    }

    private void broadcastChatMessage(PlayerChatMessage p_243322_, Predicate<ServerPlayer> p_243313_, @Nullable ServerPlayer p_243233_, ChatSender p_243228_, ChatType.Bound p_243291_)
    {
        boolean flag = this.verifyChatTrusted(p_243322_, p_243228_);
        this.server.logChatMessage(p_243322_.serverContent(), p_243291_, flag ? null : "Not Secure");
        OutgoingPlayerChatMessage outgoingplayerchatmessage = OutgoingPlayerChatMessage.create(p_243322_);
        boolean flag1 = p_243322_.isFullyFiltered();
        boolean flag2 = false;

        for (ServerPlayer serverplayer : this.players)
        {
            boolean flag3 = p_243313_.test(serverplayer);
            serverplayer.sendChatMessage(outgoingplayerchatmessage, flag3, p_243291_);

            if (p_243233_ != serverplayer)
            {
                flag2 |= flag1 && flag3;
            }
        }

        if (flag2 && p_243233_ != null)
        {
            p_243233_.sendSystemMessage(CHAT_FILTERED_FULL);
        }

        outgoingplayerchatmessage.sendHeadersToRemainingPlayers(this);
    }

    public void broadcastMessageHeader(PlayerChatMessage p_241532_, Set<ServerPlayer> p_241429_)
    {
        byte[] abyte = p_241532_.signedBody().hash().asBytes();

        for (ServerPlayer serverplayer : this.players)
        {
            if (!p_241429_.contains(serverplayer))
            {
                serverplayer.a(p_241532_.signedHeader(), p_241532_.headerSignature(), abyte);
            }
        }
    }

    private boolean verifyChatTrusted(PlayerChatMessage p_241287_, ChatSender p_241412_)
    {
        return !p_241287_.hasExpiredServer(Instant.now()) && p_241287_.verify(p_241412_);
    }

    public ServerStatsCounter getPlayerStats(Player pPlayer)
    {
        UUID uuid = pPlayer.getUUID();
        ServerStatsCounter serverstatscounter = this.stats.get(uuid);

        if (serverstatscounter == null)
        {
            File file1 = this.server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
            File file2 = new File(file1, uuid + ".json");

            if (!file2.exists())
            {
                File file3 = new File(file1, pPlayer.getName().getString() + ".json");
                Path path = file3.toPath();

                if (FileUtil.isPathNormalized(path) && FileUtil.isPathPortable(path) && path.startsWith(file1.getPath()) && file3.isFile())
                {
                    file3.renameTo(file2);
                }
            }

            serverstatscounter = new ServerStatsCounter(this.server, file2);
            this.stats.put(uuid, serverstatscounter);
        }

        return serverstatscounter;
    }

    public PlayerAdvancements getPlayerAdvancements(ServerPlayer p_11297_)
    {
        UUID uuid = p_11297_.getUUID();
        PlayerAdvancements playeradvancements = this.advancements.get(uuid);

        if (playeradvancements == null)
        {
            File file1 = this.server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).toFile();
            File file2 = new File(file1, uuid + ".json");
            playeradvancements = new PlayerAdvancements(this.server.getFixerUpper(), this, this.server.getAdvancements(), file2, p_11297_);
            this.advancements.put(uuid, playeradvancements);
        }

        playeradvancements.setPlayer(p_11297_);
        return playeradvancements;
    }

    public void setViewDistance(int pViewDistance)
    {
        this.viewDistance = pViewDistance;
        this.broadcastAll(new ClientboundSetChunkCacheRadiusPacket(pViewDistance));

        for (ServerLevel serverlevel : this.server.getAllLevels())
        {
            if (serverlevel != null)
            {
                serverlevel.getChunkSource().setViewDistance(pViewDistance);
            }
        }
    }

    public void setSimulationDistance(int p_184212_)
    {
        this.simulationDistance = p_184212_;
        this.broadcastAll(new ClientboundSetSimulationDistancePacket(p_184212_));

        for (ServerLevel serverlevel : this.server.getAllLevels())
        {
            if (serverlevel != null)
            {
                serverlevel.getChunkSource().setSimulationDistance(p_184212_);
            }
        }
    }

    public List<ServerPlayer> getPlayers()
    {
        return this.players;
    }

    @Nullable
    public ServerPlayer getPlayer(UUID pPlayerUUID)
    {
        return this.playersByUUID.get(pPlayerUUID);
    }

    public boolean canBypassPlayerLimit(GameProfile pProfile)
    {
        return false;
    }

    public void reloadResources()
    {
        for (PlayerAdvancements playeradvancements : this.advancements.values())
        {
            playeradvancements.reload(this.server.getAdvancements());
        }

        this.broadcastAll(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registryHolder)));
        ClientboundUpdateRecipesPacket clientboundupdaterecipespacket = new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes());

        for (ServerPlayer serverplayer : this.players)
        {
            serverplayer.connection.send(clientboundupdaterecipespacket);
            serverplayer.getRecipeBook().sendInitialRecipeBook(serverplayer);
        }
    }

    public boolean isAllowCheatsForAllPlayers()
    {
        return this.allowCheatsForAllPlayers;
    }
}
