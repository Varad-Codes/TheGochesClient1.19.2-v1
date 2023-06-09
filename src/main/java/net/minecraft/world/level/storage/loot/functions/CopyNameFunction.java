package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyNameFunction extends LootItemConditionalFunction
{
    final CopyNameFunction.NameSource source;

    CopyNameFunction(LootItemCondition[] pConditions, CopyNameFunction.NameSource pNameSource)
    {
        super(pConditions);
        this.source = pNameSource;
    }

    public LootItemFunctionType getType()
    {
        return LootItemFunctions.COPY_NAME;
    }

    public Set < LootContextParam<? >> getReferencedContextParams()
    {
        return ImmutableSet.of(this.source.param);
    }

    public ItemStack run(ItemStack pStack, LootContext pContext)
    {
        Object object = pContext.getParamOrNull(this.source.param);

        if (object instanceof Nameable nameable)
        {
            if (nameable.hasCustomName())
            {
                pStack.setHoverName(nameable.getDisplayName());
            }
        }

        return pStack;
    }

    public static LootItemConditionalFunction.Builder<?> copyName(CopyNameFunction.NameSource pSource)
    {
        return simpleBuilder((p_80191_) ->
        {
            return new CopyNameFunction(p_80191_, pSource);
        });
    }

    public static enum NameSource
    {
        THIS("this", LootContextParams.THIS_ENTITY),
        KILLER("killer", LootContextParams.KILLER_ENTITY),
        KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER),
        BLOCK_ENTITY("block_entity", LootContextParams.BLOCK_ENTITY);

        public final String name;
        public final LootContextParam<?> param;

        private NameSource(String p_80206_, LootContextParam<?> p_80207_)
        {
            this.name = p_80206_;
            this.param = p_80207_;
        }

        public static CopyNameFunction.NameSource getByName(String pName)
        {
            for (CopyNameFunction.NameSource copynamefunction$namesource : values())
            {
                if (copynamefunction$namesource.name.equals(pName))
                {
                    return copynamefunction$namesource;
                }
            }

            throw new IllegalArgumentException("Invalid name source " + pName);
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyNameFunction>
    {
        public void serialize(JsonObject pJson, CopyNameFunction pValue, JsonSerializationContext pSerializationContext)
        {
            super.serialize(pJson, pValue, pSerializationContext);
            pJson.addProperty("source", pValue.source.name);
        }

        public CopyNameFunction b(JsonObject p_80215_, JsonDeserializationContext p_80216_, LootItemCondition[] p_80217_)
        {
            CopyNameFunction.NameSource copynamefunction$namesource = CopyNameFunction.NameSource.getByName(GsonHelper.getAsString(p_80215_, "source"));
            return new CopyNameFunction(p_80217_, copynamefunction$namesource);
        }
    }
}
