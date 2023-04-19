package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class ServerboundUseItemOnPacket implements Packet<ServerGamePacketListener>
{
    private final BlockHitResult blockHit;
    private final InteractionHand hand;
    private final int sequence;

    public ServerboundUseItemOnPacket(InteractionHand p_238005_, BlockHitResult p_238006_, int p_238007_)
    {
        this.hand = p_238005_;
        this.blockHit = p_238006_;
        this.sequence = p_238007_;
    }

    public ServerboundUseItemOnPacket(FriendlyByteBuf pBuffer)
    {
        this.hand = pBuffer.readEnum(InteractionHand.class);
        this.blockHit = pBuffer.readBlockHitResult();
        this.sequence = pBuffer.readVarInt();
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeEnum(this.hand);
        pBuffer.writeBlockHitResult(this.blockHit);
        pBuffer.writeVarInt(this.sequence);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleUseItemOn(this);
    }

    public InteractionHand getHand()
    {
        return this.hand;
    }

    public BlockHitResult getHitResult()
    {
        return this.blockHit;
    }

    public int getSequence()
    {
        return this.sequence;
    }
}
