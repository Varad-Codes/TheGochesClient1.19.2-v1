package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterPigSaddle extends ModelAdapterQuadruped
{
    public ModelAdapterPigSaddle()
    {
        super(EntityType.PIG, "pig_saddle", 0.7F);
    }

    public Model makeModel()
    {
        return new PigModel(bakeModelLayer(ModelLayers.PIG_SADDLE));
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer entityrenderer = entityrenderdispatcher.getEntityRenderMap().get(EntityType.PIG);

        if (!(entityrenderer instanceof PigRenderer))
        {
            Config.warn("Not a PigRenderer: " + entityrenderer);
            return null;
        }
        else
        {
            if (entityrenderer.getType() == null)
            {
                PigRenderer pigrenderer = new PigRenderer(entityrenderdispatcher.getContext());
                pigrenderer.model = new PigModel<>(bakeModelLayer(ModelLayers.PIG_SADDLE));
                pigrenderer.shadowRadius = 0.7F;
                entityrenderer = pigrenderer;
            }

            PigRenderer pigrenderer1 = (PigRenderer)entityrenderer;
            SaddleLayer saddlelayer = new SaddleLayer<>(pigrenderer1, (PigModel)modelBase, new ResourceLocation("textures/entity/pig/pig_saddle.png"));
            pigrenderer1.removeLayers(SaddleLayer.class);
            pigrenderer1.addLayer(saddlelayer);
            return pigrenderer1;
        }
    }

    public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation)
    {
        PigRenderer pigrenderer = (PigRenderer)er;

        for (SaddleLayer saddlelayer : pigrenderer.getLayers(SaddleLayer.class))
        {
            saddlelayer.textureLocation = textureLocation;
        }

        return true;
    }
}
