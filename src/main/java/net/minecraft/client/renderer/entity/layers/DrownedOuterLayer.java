package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Drowned;

public class DrownedOuterLayer<T extends Drowned> extends RenderLayer<T, DrownedModel<T>>
{
    private static final ResourceLocation DROWNED_OUTER_LAYER_LOCATION = new ResourceLocation("textures/entity/zombie/drowned_outer_layer.png");
    public DrownedModel<T> model;
    public ResourceLocation customTextureLocation;

    public DrownedOuterLayer(RenderLayerParent<T, DrownedModel<T>> p_174490_, EntityModelSet p_174491_)
    {
        super(p_174490_);
        this.model = new DrownedModel<>(p_174491_.bakeLayer(ModelLayers.DROWNED_OUTER_LAYER));
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        ResourceLocation resourcelocation = this.customTextureLocation != null ? this.customTextureLocation : DROWNED_OUTER_LAYER_LOCATION;
        coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, resourcelocation, pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, pPartialTicks, 1.0F, 1.0F, 1.0F);
    }
}
