package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemEntityRenderer extends EntityRenderer<ItemEntity>
{
    private static final float ITEM_BUNDLE_OFFSET_SCALE = 0.15F;
    private static final int ITEM_COUNT_FOR_5_BUNDLE = 48;
    private static final int ITEM_COUNT_FOR_4_BUNDLE = 32;
    private static final int ITEM_COUNT_FOR_3_BUNDLE = 16;
    private static final int ITEM_COUNT_FOR_2_BUNDLE = 1;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_X = 0.0F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_Y = 0.0F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_Z = 0.09375F;
    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();

    public ItemEntityRenderer(EntityRendererProvider.Context p_174198_)
    {
        super(p_174198_);
        this.itemRenderer = p_174198_.getItemRenderer();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    private int getRenderAmount(ItemStack pStack)
    {
        int i = 1;

        if (pStack.getCount() > 48)
        {
            i = 5;
        }
        else if (pStack.getCount() > 32)
        {
            i = 4;
        }
        else if (pStack.getCount() > 16)
        {
            i = 3;
        }
        else if (pStack.getCount() > 1)
        {
            i = 2;
        }

        return i;
    }

    public void render(ItemEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        pMatrixStack.pushPose();
        ItemStack itemstack = pEntity.getItem();
        int i = itemstack.isEmpty() ? 187 : Item.getId(itemstack.getItem()) + itemstack.getDamageValue();
        this.random.setSeed((long)i);
        BakedModel bakedmodel = this.itemRenderer.getModel(itemstack, pEntity.level, (LivingEntity)null, pEntity.getId());
        boolean flag = bakedmodel.isGui3d();
        int j = this.getRenderAmount(itemstack);
        float f = 0.25F;
        float f1 = Mth.sin(((float)pEntity.getAge() + pPartialTicks) / 10.0F + pEntity.bobOffs) * 0.1F + 0.1F;

        if (!this.shouldBob())
        {
            f1 = 0.0F;
        }

        float f2 = bakedmodel.getTransforms().getTransform(ItemTransforms.TransformType.GROUND).scale.y();
        pMatrixStack.translate(0.0D, (double)(f1 + 0.25F * f2), 0.0D);
        float f3 = pEntity.getSpin(pPartialTicks);
        pMatrixStack.mulPose(Vector3f.YP.rotation(f3));
        float f4 = bakedmodel.getTransforms().ground.scale.x();
        float f5 = bakedmodel.getTransforms().ground.scale.y();
        float f6 = bakedmodel.getTransforms().ground.scale.z();

        if (!flag)
        {
            float f7 = -0.0F * (float)(j - 1) * 0.5F * f4;
            float f8 = -0.0F * (float)(j - 1) * 0.5F * f5;
            float f9 = -0.09375F * (float)(j - 1) * 0.5F * f6;
            pMatrixStack.translate((double)f7, (double)f8, (double)f9);
        }

        for (int k = 0; k < j; ++k)
        {
            pMatrixStack.pushPose();

            if (k > 0)
            {
                if (flag)
                {
                    float f11 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;

                    if (!this.shouldSpreadItems())
                    {
                        f11 = 0.0F;
                        f13 = 0.0F;
                        f10 = 0.0F;
                    }

                    pMatrixStack.translate((double)f11, (double)f13, (double)f10);
                }
                else
                {
                    float f12 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float f14 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;

                    if (!this.shouldSpreadItems())
                    {
                        f12 = 0.0F;
                        f14 = 0.0F;
                    }

                    pMatrixStack.translate((double)f12, (double)f14, 0.0D);
                }
            }

            this.itemRenderer.render(itemstack, ItemTransforms.TransformType.GROUND, false, pMatrixStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY, bakedmodel);
            pMatrixStack.popPose();

            if (!flag)
            {
                pMatrixStack.translate((double)(0.0F * f4), (double)(0.0F * f5), (double)(0.09375F * f6));
            }
        }

        pMatrixStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    public ResourceLocation getTextureLocation(ItemEntity pEntity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    public boolean shouldSpreadItems()
    {
        return true;
    }

    public boolean shouldBob()
    {
        return true;
    }
}
