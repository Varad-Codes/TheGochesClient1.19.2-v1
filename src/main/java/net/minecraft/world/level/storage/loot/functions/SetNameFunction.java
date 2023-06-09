package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class SetNameFunction extends LootItemConditionalFunction
{
    private static final Logger LOGGER = LogUtils.getLogger();
    final Component name;
    @Nullable
    final LootContext.EntityTarget resolutionContext;

    SetNameFunction(LootItemCondition[] pConditions, @Nullable Component pName, @Nullable LootContext.EntityTarget pResolutionContext)
    {
        super(pConditions);
        this.name = pName;
        this.resolutionContext = pResolutionContext;
    }

    public LootItemFunctionType getType()
    {
        return LootItemFunctions.SET_NAME;
    }

    public Set < LootContextParam<? >> getReferencedContextParams()
    {
        return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
    }

    public static UnaryOperator<Component> createResolver(LootContext pLootContext, @Nullable LootContext.EntityTarget pResolutionContext)
    {
        if (pResolutionContext != null)
        {
            Entity entity = pLootContext.getParamOrNull(pResolutionContext.getParam());

            if (entity != null)
            {
                CommandSourceStack commandsourcestack = entity.createCommandSourceStack().withPermission(2);
                return (p_81147_) ->
                {
                    try {
                        return ComponentUtils.updateForEntity(commandsourcestack, p_81147_, entity, 0);
                    }
                    catch (CommandSyntaxException commandsyntaxexception)
                    {
                        LOGGER.warn("Failed to resolve text component", (Throwable)commandsyntaxexception);
                        return p_81147_;
                    }
                };
            }
        }

        return (p_81152_) ->
        {
            return p_81152_;
        };
    }

    public ItemStack run(ItemStack pStack, LootContext pContext)
    {
        if (this.name != null)
        {
            pStack.setHoverName(createResolver(pContext, this.resolutionContext).apply(this.name));
        }

        return pStack;
    }

    public static LootItemConditionalFunction.Builder<?> setName(Component pName)
    {
        return simpleBuilder((p_165468_) ->
        {
            return new SetNameFunction(p_165468_, pName, (LootContext.EntityTarget)null);
        });
    }

    public static LootItemConditionalFunction.Builder<?> setName(Component pName, LootContext.EntityTarget pResolutionContext)
    {
        return simpleBuilder((p_165465_) ->
        {
            return new SetNameFunction(p_165465_, pName, pResolutionContext);
        });
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetNameFunction>
    {
        public void serialize(JsonObject pJson, SetNameFunction pValue, JsonSerializationContext pSerializationContext)
        {
            super.serialize(pJson, pValue, pSerializationContext);

            if (pValue.name != null)
            {
                pJson.add("name", Component.Serializer.toJsonTree(pValue.name));
            }

            if (pValue.resolutionContext != null)
            {
                pJson.add("entity", pSerializationContext.serialize(pValue.resolutionContext));
            }
        }

        public SetNameFunction b(JsonObject p_81155_, JsonDeserializationContext p_81156_, LootItemCondition[] p_81157_)
        {
            Component component = Component.Serializer.fromJson(p_81155_.get("name"));
            LootContext.EntityTarget lootcontext$entitytarget = GsonHelper.getAsObject(p_81155_, "entity", (LootContext.EntityTarget)null, p_81156_, LootContext.EntityTarget.class);
            return new SetNameFunction(p_81157_, component, lootcontext$entitytarget);
        }
    }
}
