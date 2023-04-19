package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterDrownedOuter extends ModelAdapterDrowned
{
    public ModelAdapterDrownedOuter()
    {
        super(EntityType.DROWNED, "drowned_outer", 0.5F);
    }

    public Model makeModel()
    {
        return new DrownedModel(bakeModelLayer(ModelLayers.DROWNED_OUTER_LAYER));
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer entityrenderer = entityrenderdispatcher.getEntityRenderMap().get(EntityType.DROWNED);

        if (!(entityrenderer instanceof DrownedRenderer))
        {
            Config.warn("Not a DrownedRenderer: " + entityrenderer);
            return null;
        }
        else
        {
            if (entityrenderer.getType() == null)
            {
                DrownedRenderer drownedrenderer = new DrownedRenderer(entityrenderdispatcher.getContext());
                drownedrenderer.model = new DrownedModel<>(bakeModelLayer(ModelLayers.DROWNED_OUTER_LAYER));
                drownedrenderer.shadowRadius = 0.75F;
                entityrenderer = drownedrenderer;
            }

            DrownedRenderer drownedrenderer1 = (DrownedRenderer)entityrenderer;
            DrownedOuterLayer drownedouterlayer = new DrownedOuterLayer<>(drownedrenderer1, entityrenderdispatcher.getContext().getModelSet());
            drownedouterlayer.model = (DrownedModel)modelBase;
            drownedrenderer1.removeLayers(DrownedOuterLayer.class);
            drownedrenderer1.addLayer(drownedouterlayer);
            return drownedrenderer1;
        }
    }

    public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation)
    {
        DrownedRenderer drownedrenderer = (DrownedRenderer)er;

        for (DrownedOuterLayer drownedouterlayer : drownedrenderer.getLayers(DrownedOuterLayer.class))
        {
            drownedouterlayer.customTextureLocation = textureLocation;
        }

        return true;
    }
}
