package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ClientboundAddEntityPacket implements Packet<ClientGamePacketListener>
{
    private static final double MAGICAL_QUANTIZATION = 8000.0D;
    private static final double LIMIT = 3.9D;
    private final int id;
    private final UUID uuid;
    private final EntityType<?> type;
    private final double x;
    private final double y;
    private final double z;
    private final int xa;
    private final int ya;
    private final int za;
    private final byte xRot;
    private final byte yRot;
    private final byte yHeadRot;
    private final int data;

    public ClientboundAddEntityPacket(LivingEntity pBuffer)
    {
        this(pBuffer, 0);
    }

    public ClientboundAddEntityPacket(LivingEntity pEntity, int pData)
    {
        this(pEntity.getId(), pEntity.getUUID(), pEntity.getX(), pEntity.getY(), pEntity.getZ(), pEntity.getXRot(), pEntity.getYRot(), pEntity.getType(), pData, pEntity.getDeltaMovement(), (double)pEntity.yHeadRot);
    }

    public ClientboundAddEntityPacket(Entity pBuffer)
    {
        this(pBuffer, 0);
    }

    public ClientboundAddEntityPacket(Entity pEntity, int pData)
    {
        this(pEntity.getId(), pEntity.getUUID(), pEntity.getX(), pEntity.getY(), pEntity.getZ(), pEntity.getXRot(), pEntity.getYRot(), pEntity.getType(), pData, pEntity.getDeltaMovement(), 0.0D);
    }

    public ClientboundAddEntityPacket(Entity p_237558_, int p_237559_, BlockPos p_237560_)
    {
        this(p_237558_.getId(), p_237558_.getUUID(), (double)p_237560_.getX(), (double)p_237560_.getY(), (double)p_237560_.getZ(), p_237558_.getXRot(), p_237558_.getYRot(), p_237558_.getType(), p_237559_, p_237558_.getDeltaMovement(), 0.0D);
    }

    public ClientboundAddEntityPacket(int p_237546_, UUID p_237547_, double p_237548_, double p_237549_, double p_237550_, float p_237551_, float p_237552_, EntityType<?> p_237553_, int p_237554_, Vec3 p_237555_, double p_237556_)
    {
        this.id = p_237546_;
        this.uuid = p_237547_;
        this.x = p_237548_;
        this.y = p_237549_;
        this.z = p_237550_;
        this.xRot = (byte)Mth.floor(p_237551_ * 256.0F / 360.0F);
        this.yRot = (byte)Mth.floor(p_237552_ * 256.0F / 360.0F);
        this.yHeadRot = (byte)Mth.floor(p_237556_ * 256.0D / 360.0D);
        this.type = p_237553_;
        this.data = p_237554_;
        this.xa = (int)(Mth.clamp(p_237555_.x, -3.9D, 3.9D) * 8000.0D);
        this.ya = (int)(Mth.clamp(p_237555_.y, -3.9D, 3.9D) * 8000.0D);
        this.za = (int)(Mth.clamp(p_237555_.z, -3.9D, 3.9D) * 8000.0D);
    }

    public ClientboundAddEntityPacket(FriendlyByteBuf pBuffer)
    {
        this.id = pBuffer.readVarInt();
        this.uuid = pBuffer.readUUID();
        this.type = pBuffer.readById(Registry.ENTITY_TYPE);
        this.x = pBuffer.readDouble();
        this.y = pBuffer.readDouble();
        this.z = pBuffer.readDouble();
        this.xRot = pBuffer.readByte();
        this.yRot = pBuffer.readByte();
        this.yHeadRot = pBuffer.readByte();
        this.data = pBuffer.readVarInt();
        this.xa = pBuffer.readShort();
        this.ya = pBuffer.readShort();
        this.za = pBuffer.readShort();
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeVarInt(this.id);
        pBuffer.writeUUID(this.uuid);
        pBuffer.writeId(Registry.ENTITY_TYPE, this.type);
        pBuffer.writeDouble(this.x);
        pBuffer.writeDouble(this.y);
        pBuffer.writeDouble(this.z);
        pBuffer.writeByte(this.xRot);
        pBuffer.writeByte(this.yRot);
        pBuffer.writeByte(this.yHeadRot);
        pBuffer.writeVarInt(this.data);
        pBuffer.writeShort(this.xa);
        pBuffer.writeShort(this.ya);
        pBuffer.writeShort(this.za);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleAddEntity(this);
    }

    public int getId()
    {
        return this.id;
    }

    public UUID getUUID()
    {
        return this.uuid;
    }

    public EntityType<?> getType()
    {
        return this.type;
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public double getZ()
    {
        return this.z;
    }

    public double getXa()
    {
        return (double)this.xa / 8000.0D;
    }

    public double getYa()
    {
        return (double)this.ya / 8000.0D;
    }

    public double getZa()
    {
        return (double)this.za / 8000.0D;
    }

    public float getXRot()
    {
        return (float)(this.xRot * 360) / 256.0F;
    }

    public float getYRot()
    {
        return (float)(this.yRot * 360) / 256.0F;
    }

    public float getYHeadRot()
    {
        return (float)(this.yHeadRot * 360) / 256.0F;
    }

    public int getData()
    {
        return this.data;
    }
}
