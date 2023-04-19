package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener>
{
    private final ResourceKey<DimensionType> dimensionType;
    private final ResourceKey<Level> dimension;
    private final long seed;
    private final GameType playerGameType;
    @Nullable
    private final GameType previousPlayerGameType;
    private final boolean isDebug;
    private final boolean isFlat;
    private final boolean keepAllPlayerData;
    private final Optional<GlobalPos> lastDeathLocation;

    public ClientboundRespawnPacket(ResourceKey<DimensionType> p_238301_, ResourceKey<Level> p_238302_, long p_238303_, GameType p_238304_, @Nullable GameType p_238305_, boolean p_238306_, boolean p_238307_, boolean p_238308_, Optional<GlobalPos> p_238309_)
    {
        this.dimensionType = p_238301_;
        this.dimension = p_238302_;
        this.seed = p_238303_;
        this.playerGameType = p_238304_;
        this.previousPlayerGameType = p_238305_;
        this.isDebug = p_238306_;
        this.isFlat = p_238307_;
        this.keepAllPlayerData = p_238308_;
        this.lastDeathLocation = p_238309_;
    }

    public ClientboundRespawnPacket(FriendlyByteBuf pBuffer)
    {
        this.dimensionType = pBuffer.readResourceKey(Registry.DIMENSION_TYPE_REGISTRY);
        this.dimension = pBuffer.readResourceKey(Registry.DIMENSION_REGISTRY);
        this.seed = pBuffer.readLong();
        this.playerGameType = GameType.byId(pBuffer.readUnsignedByte());
        this.previousPlayerGameType = GameType.byNullableId(pBuffer.readByte());
        this.isDebug = pBuffer.readBoolean();
        this.isFlat = pBuffer.readBoolean();
        this.keepAllPlayerData = pBuffer.readBoolean();
        this.lastDeathLocation = pBuffer.readOptional(FriendlyByteBuf::readGlobalPos);
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeResourceKey(this.dimensionType);
        pBuffer.writeResourceKey(this.dimension);
        pBuffer.writeLong(this.seed);
        pBuffer.writeByte(this.playerGameType.getId());
        pBuffer.writeByte(GameType.getNullableId(this.previousPlayerGameType));
        pBuffer.writeBoolean(this.isDebug);
        pBuffer.writeBoolean(this.isFlat);
        pBuffer.writeBoolean(this.keepAllPlayerData);
        pBuffer.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleRespawn(this);
    }

    public ResourceKey<DimensionType> getDimensionType()
    {
        return this.dimensionType;
    }

    public ResourceKey<Level> getDimension()
    {
        return this.dimension;
    }

    public long getSeed()
    {
        return this.seed;
    }

    public GameType getPlayerGameType()
    {
        return this.playerGameType;
    }

    @Nullable
    public GameType getPreviousPlayerGameType()
    {
        return this.previousPlayerGameType;
    }

    public boolean isDebug()
    {
        return this.isDebug;
    }

    public boolean isFlat()
    {
        return this.isFlat;
    }

    public boolean shouldKeepAllPlayerData()
    {
        return this.keepAllPlayerData;
    }

    public Optional<GlobalPos> getLastDeathLocation()
    {
        return this.lastDeathLocation;
    }
}
