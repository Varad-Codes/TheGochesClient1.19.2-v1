package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class SlimeOuterLayer<T extends LivingEntity> extends RenderLayer<T, SlimeModel<T>>
{
    public EntityModel<T> model;
    public ResourceLocation customTextureLocation;

    public SlimeOuterLayer(RenderLayerParent<T, SlimeModel<T>> p_174536_, EntityModelSet p_174537_)
    {
        super(p_174536_);
        this.model = new SlimeModel<>(p_174537_.bakeLayer(ModelLayers.SLIME_OUTER));
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = minecraft.shouldEntityAppearGlowing(pLivingEntity) && pLivingEntity.isInvisible();

        if (!pLivingEntity.isInvisible() || flag)
        {
            ResourceLocation resourcelocation = this.customTextureLocation != null ? this.customTextureLocation : this.getTextureLocation(pLivingEntity);
            VertexConsumer vertexconsumer;

            if (flag)
            {
                vertexconsumer = pBuffer.getBuffer(RenderType.outline(resourcelocation));
            }
            else
            {
                vertexconsumer = pBuffer.getBuffer(RenderType.entityTranslucent(resourcelocation));
            }

            this.getParentModel().copyPropertiesTo(this.model);
            this.model.prepareMobModel(pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks);
            this.model.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
            this.model.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
