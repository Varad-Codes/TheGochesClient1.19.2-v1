package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;

public class ClientboundAwardStatsPacket implements Packet<ClientGamePacketListener>
{
    private final Object2IntMap < Stat<? >> stats;

    public ClientboundAwardStatsPacket(Object2IntMap < Stat<? >> pStats)
    {
        this.stats = pStats;
    }

    public ClientboundAwardStatsPacket(FriendlyByteBuf pStats)
    {
        this.stats = pStats.readMap(Object2IntOpenHashMap::new, (p_237577_) ->
        {
            StatType<?> stattype = p_237577_.readById(Registry.STAT_TYPE);
            return readStatCap(pStats, stattype);
        }, FriendlyByteBuf::readVarInt);
    }

    private static <T> Stat<T> readStatCap(FriendlyByteBuf p_237573_, StatType<T> p_237574_)
    {
        return p_237574_.get(p_237573_.readById(p_237574_.getRegistry()));
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleAwardStats(this);
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeMap(this.stats, ClientboundAwardStatsPacket::writeStatCap, FriendlyByteBuf::writeVarInt);
    }

    private static <T> void writeStatCap(FriendlyByteBuf p_237570_, Stat<T> p_237571_)
    {
        p_237570_.writeId(Registry.STAT_TYPE, p_237571_.getType());
        p_237570_.writeId(p_237571_.getType().getRegistry(), p_237571_.getValue());
    }

    public Map < Stat<?>, Integer > getStats()
    {
        return this.stats;
    }
}
