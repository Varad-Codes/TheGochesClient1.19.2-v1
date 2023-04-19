package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.StriderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.StriderRenderer;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterStriderSaddle extends ModelAdapterStrider
{
    public ModelAdapterStriderSaddle()
    {
        super(EntityType.STRIDER, "strider_saddle", 0.5F);
    }

    public Model makeModel()
    {
        return new StriderModel(bakeModelLayer(ModelLayers.STRIDER_SADDLE));
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer entityrenderer = entityrenderdispatcher.getEntityRenderMap().get(EntityType.STRIDER);

        if (!(entityrenderer instanceof StriderRenderer))
        {
            Config.warn("Not a StriderRenderer: " + entityrenderer);
            return null;
        }
        else
        {
            if (entityrenderer.getType() == null)
            {
                StriderRenderer striderrenderer = new StriderRenderer(entityrenderdispatcher.getContext());
                striderrenderer.model = new StriderModel<>(bakeModelLayer(ModelLayers.STRIDER_SADDLE));
                striderrenderer.shadowRadius = 0.5F;
                entityrenderer = striderrenderer;
            }

            StriderRenderer striderrenderer1 = (StriderRenderer)entityrenderer;
            SaddleLayer saddlelayer = new SaddleLayer<>(striderrenderer1, (StriderModel)modelBase, new ResourceLocation("textures/entity/strider/strider_saddle.png"));
            striderrenderer1.removeLayers(SaddleLayer.class);
            striderrenderer1.addLayer(saddlelayer);
            return striderrenderer1;
        }
    }

    public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation)
    {
        StriderRenderer striderrenderer = (StriderRenderer)er;

        for (SaddleLayer saddlelayer : striderrenderer.getLayers(SaddleLayer.class))
        {
            saddlelayer.textureLocation = textureLocation;
        }

        return true;
    }
}
