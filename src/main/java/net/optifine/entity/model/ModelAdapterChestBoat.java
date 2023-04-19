package net.optifine.entity.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.optifine.util.ArrayUtils;

public class ModelAdapterChestBoat extends ModelAdapterBoat
{
    public ModelAdapterChestBoat()
    {
        super(EntityType.CHEST_BOAT, "chest_boat", 0.5F);
    }

    public Model makeModel()
    {
        return new BoatModel(bakeModelLayer(ModelLayers.createChestBoatModelName(Boat.Type.OAK)), true);
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof BoatModel boatmodel))
        {
            return null;
        }
        else
        {
            ImmutableList<ModelPart> immutablelist = boatmodel.parts();

            if (immutablelist != null)
            {
                if (modelPart.equals("chest_base"))
                {
                    return ModelRendererUtils.getModelRenderer(immutablelist, 7);
                }

                if (modelPart.equals("chest_lid"))
                {
                    return ModelRendererUtils.getModelRenderer(immutablelist, 8);
                }

                if (modelPart.equals("chest_knob"))
                {
                    return ModelRendererUtils.getModelRenderer(immutablelist, 9);
                }
            }

            return super.getModelRenderer(boatmodel, modelPart);
        }
    }

    public String[] getModelRendererNames()
    {
        String[] astring = super.getModelRendererNames();
        return (String[])ArrayUtils.addObjectsToArray(astring, new String[] {"chest_base", "chest_lid", "chest_knob"});
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        return this.makeEntityRender(modelBase, shadowSize, true);
    }
}
