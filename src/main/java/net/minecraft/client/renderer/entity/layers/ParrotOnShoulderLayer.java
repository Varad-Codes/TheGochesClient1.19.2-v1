package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.optifine.Config;
import net.optifine.shaders.Shaders;

public class ParrotOnShoulderLayer<T extends Player> extends RenderLayer<T, PlayerModel<T>>
{
    private final ParrotModel model;
    public static ParrotModel customParrotModel;

    public ParrotOnShoulderLayer(RenderLayerParent<T, PlayerModel<T>> p_174511_, EntityModelSet p_174512_)
    {
        super(p_174511_);
        this.model = new ParrotModel(p_174512_.bakeLayer(ModelLayers.PARROT));
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        this.render(pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pNetHeadYaw, pHeadPitch, true);
        this.render(pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pNetHeadYaw, pHeadPitch, false);
    }

    private void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pNetHeadYaw, float pHeadPitch, boolean pLeftShoulder)
    {
        CompoundTag compoundtag = pLeftShoulder ? pLivingEntity.getShoulderEntityLeft() : pLivingEntity.getShoulderEntityRight();
        EntityType.byString(compoundtag.getString("id")).filter((entityTypeIn) ->
        {
            return entityTypeIn == EntityType.PARROT;
        }).ifPresent((entityTypeIn) ->
        {
            Entity entity = Config.getEntityRenderDispatcher().getRenderedEntity();

            if (pLivingEntity instanceof AbstractClientPlayer abstractclientplayer)
            {
                Entity entity1 = pLeftShoulder ? abstractclientplayer.entityShoulderLeft : abstractclientplayer.entityShoulderRight;

                if (entity1 == null)
                {
                    entity1 = this.makeEntity(compoundtag, pLivingEntity);

                    if (entity1 instanceof ShoulderRidingEntity)
                    {
                        if (pLeftShoulder)
                        {
                            abstractclientplayer.entityShoulderLeft = (ShoulderRidingEntity)entity1;
                        }
                        else
                        {
                            abstractclientplayer.entityShoulderRight = (ShoulderRidingEntity)entity1;
                        }
                    }
                }

                if (entity1 != null)
                {
                    entity1.xo = entity.xo;
                    entity1.yo = entity.yo;
                    entity1.zo = entity.zo;
                    entity1.setPosRaw(entity.getX(), entity.getY(), entity.getZ());
                    entity1.xRotO = entity.xRotO;
                    entity1.yRotO = entity.yRotO;
                    entity1.setXRot(entity.getXRot());
                    entity1.setYRot(entity.getYRot());

                    if (entity1 instanceof LivingEntity && entity instanceof LivingEntity)
                    {
                        ((LivingEntity)entity1).yBodyRotO = ((LivingEntity)entity).yBodyRotO;
                        ((LivingEntity)entity1).yBodyRot = ((LivingEntity)entity).yBodyRot;
                    }

                    Config.getEntityRenderDispatcher().setRenderedEntity(entity1);

                    if (Config.isShaders())
                    {
                        Shaders.nextEntity(entity1);
                    }
                }
            }

            pMatrixStack.pushPose();
            pMatrixStack.translate(pLeftShoulder ? (double)0.4F : (double) - 0.4F, pLivingEntity.isCrouching() ? (double) - 1.3F : -1.5D, 0.0D);
            VertexConsumer vertexconsumer = pBuffer.getBuffer(this.model.renderType(ParrotRenderer.PARROT_LOCATIONS[compoundtag.getInt("Variant")]));
            this.getParrotModel().renderOnShoulder(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, pLimbSwing, pLimbSwingAmount, pNetHeadYaw, pHeadPitch, pLivingEntity.tickCount);
            pMatrixStack.popPose();
            Config.getEntityRenderDispatcher().setRenderedEntity(entity);

            if (Config.isShaders())
            {
                Shaders.nextEntity(entity);
            }
        });
    }

    private Entity makeEntity(CompoundTag compoundtag, Player player)
    {
        Optional < EntityType<? >> optional = EntityType.by(compoundtag);

        if (!optional.isPresent())
        {
            return null;
        }
        else
        {
            Entity entity = optional.get().create(player.getLevel());

            if (entity == null)
            {
                return null;
            }
            else
            {
                entity.load(compoundtag);
                SynchedEntityData synchedentitydata = entity.getEntityData();

                if (synchedentitydata != null)
                {
                    synchedentitydata.spawnPosition = player.blockPosition();
                    synchedentitydata.spawnBiome = player.getLevel().getBiome(synchedentitydata.spawnPosition).value();
                }

                return entity;
            }
        }
    }

    private ParrotModel getParrotModel()
    {
        return customParrotModel != null ? customParrotModel : this.model;
    }
}
