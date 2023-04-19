package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.Config;

public class MushroomCowMushroomLayer<T extends MushroomCow> extends RenderLayer<T, CowModel<T>>
{
    private ModelPart modelRendererMushroom;
    private static final ResourceLocation LOCATION_MUSHROOM_RED = new ResourceLocation("textures/entity/cow/red_mushroom.png");
    private static final ResourceLocation LOCATION_MUSHROOM_BROWN = new ResourceLocation("textures/entity/cow/brown_mushroom.png");
    private static boolean hasTextureMushroomRed = false;
    private static boolean hasTextureMushroomBrown = false;
    private final BlockRenderDispatcher blockRenderer;

    public MushroomCowMushroomLayer(RenderLayerParent<T, CowModel<T>> p_234850_, BlockRenderDispatcher p_234851_)
    {
        super(p_234850_);
        this.blockRenderer = p_234851_;
        this.modelRendererMushroom = new ModelPart(new ArrayList<>(), new HashMap<>());
        this.modelRendererMushroom.setTextureSize(16, 16);
        this.modelRendererMushroom.x = 8.0F;
        this.modelRendererMushroom.z = 8.0F;
        this.modelRendererMushroom.yRot = ((float)Math.PI / 4F);
        int[][] aint = new int[][] {null, null, {16, 16, 0, 0}, {16, 16, 0, 0}, null, null};
        this.modelRendererMushroom.addBox(aint, -10.0F, 0.0F, 0.0F, 20.0F, 16.0F, 0.0F, 0.0F);
        int[][] aint1 = new int[][] {null, null, null, null, {16, 16, 0, 0}, {16, 16, 0, 0}};
        this.modelRendererMushroom.addBox(aint1, 0.0F, 0.0F, -10.0F, 0.0F, 16.0F, 20.0F, 0.0F);
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        if (!pLivingEntity.isBaby())
        {
            Minecraft minecraft = Minecraft.getInstance();
            boolean flag = minecraft.shouldEntityAppearGlowing(pLivingEntity) && pLivingEntity.isInvisible();

            if (!pLivingEntity.isInvisible() || flag)
            {
                BlockState blockstate = pLivingEntity.getMushroomType().getBlockState();
                ResourceLocation resourcelocation = this.getCustomMushroom(blockstate);
                VertexConsumer vertexconsumer = null;

                if (resourcelocation != null)
                {
                    vertexconsumer = pBuffer.getBuffer(RenderType.entityCutout(resourcelocation));
                }

                int i = LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F);
                BakedModel bakedmodel = this.blockRenderer.getBlockModel(blockstate);
                pMatrixStack.pushPose();
                pMatrixStack.translate((double)0.2F, (double) - 0.35F, 0.5D);
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
                pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
                pMatrixStack.translate(-0.5D, -0.5D, -0.5D);

                if (resourcelocation != null)
                {
                    this.modelRendererMushroom.render(pMatrixStack, vertexconsumer, pPackedLight, i);
                }
                else
                {
                    this.renderMushroomBlock(pMatrixStack, pBuffer, pPackedLight, flag, blockstate, i, bakedmodel);
                }

                pMatrixStack.popPose();
                pMatrixStack.pushPose();
                pMatrixStack.translate((double)0.2F, (double) - 0.35F, 0.5D);
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(42.0F));
                pMatrixStack.translate((double)0.1F, 0.0D, (double) - 0.6F);
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
                pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
                pMatrixStack.translate(-0.5D, -0.5D, -0.5D);

                if (resourcelocation != null)
                {
                    this.modelRendererMushroom.render(pMatrixStack, vertexconsumer, pPackedLight, i);
                }
                else
                {
                    this.renderMushroomBlock(pMatrixStack, pBuffer, pPackedLight, flag, blockstate, i, bakedmodel);
                }

                pMatrixStack.popPose();
                pMatrixStack.pushPose();
                this.getParentModel().getHead().translateAndRotate(pMatrixStack);
                pMatrixStack.translate(0.0D, (double) - 0.7F, (double) - 0.2F);
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-78.0F));
                pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
                pMatrixStack.translate(-0.5D, -0.5D, -0.5D);

                if (resourcelocation != null)
                {
                    this.modelRendererMushroom.render(pMatrixStack, vertexconsumer, pPackedLight, i);
                }
                else
                {
                    this.renderMushroomBlock(pMatrixStack, pBuffer, pPackedLight, flag, blockstate, i, bakedmodel);
                }

                pMatrixStack.popPose();
            }
        }
    }

    private void renderMushroomBlock(PoseStack p_234853_, MultiBufferSource p_234854_, int p_234855_, boolean p_234856_, BlockState p_234857_, int p_234858_, BakedModel p_234859_)
    {
        if (p_234856_)
        {
            this.blockRenderer.getModelRenderer().renderModel(p_234853_.last(), p_234854_.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), p_234857_, p_234859_, 0.0F, 0.0F, 0.0F, p_234855_, p_234858_);
        }
        else
        {
            this.blockRenderer.renderSingleBlock(p_234857_, p_234853_, p_234854_, p_234855_, p_234858_);
        }
    }

    private ResourceLocation getCustomMushroom(BlockState iblockstate)
    {
        Block block = iblockstate.getBlock();

        if (block == Blocks.RED_MUSHROOM && hasTextureMushroomRed)
        {
            return LOCATION_MUSHROOM_RED;
        }
        else
        {
            return block == Blocks.BROWN_MUSHROOM && hasTextureMushroomBrown ? LOCATION_MUSHROOM_BROWN : null;
        }
    }

    public static void update()
    {
        hasTextureMushroomRed = Config.hasResource(LOCATION_MUSHROOM_RED);
        hasTextureMushroomBrown = Config.hasResource(LOCATION_MUSHROOM_BROWN);
    }
}
