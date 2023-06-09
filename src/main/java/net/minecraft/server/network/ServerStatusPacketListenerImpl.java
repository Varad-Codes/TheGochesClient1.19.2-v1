package net.minecraft.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatusPacketListener;
import net.minecraft.network.protocol.status.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraft.server.MinecraftServer;

public class ServerStatusPacketListenerImpl implements ServerStatusPacketListener
{
    private static final Component DISCONNECT_REASON = Component.translatable("multiplayer.status.request_handled");
    private final MinecraftServer server;
    private final Connection connection;
    private boolean hasRequestedStatus;

    public ServerStatusPacketListenerImpl(MinecraftServer p_10087_, Connection p_10088_)
    {
        this.server = p_10087_;
        this.connection = p_10088_;
    }

    public void onDisconnect(Component pReason)
    {
    }

    public Connection getConnection()
    {
        return this.connection;
    }

    public void handleStatusRequest(ServerboundStatusRequestPacket pPacket)
    {
        if (this.hasRequestedStatus)
        {
            this.connection.disconnect(DISCONNECT_REASON);
        }
        else
        {
            this.hasRequestedStatus = true;
            this.connection.send(new ClientboundStatusResponsePacket(this.server.getStatus()));
        }
    }

    public void handlePingRequest(ServerboundPingRequestPacket pPacket)
    {
        this.connection.send(new ClientboundPongResponsePacket(pPacket.getTime()));
        this.connection.disconnect(DISCONNECT_REASON);
    }
}
