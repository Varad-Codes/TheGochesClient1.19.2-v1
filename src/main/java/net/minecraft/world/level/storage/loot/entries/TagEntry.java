package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TagEntry extends LootPoolSingletonContainer
{
    final TagKey<Item> tag;
    final boolean expand;

    TagEntry(TagKey<Item> pTag, boolean pExpand, int pWeight, int pQuality, LootItemCondition[] pConditions, LootItemFunction[] pFunctions)
    {
        super(pWeight, pQuality, pConditions, pFunctions);
        this.tag = pTag;
        this.expand = pExpand;
    }

    public LootPoolEntryType getType()
    {
        return LootPoolEntries.TAG;
    }

    public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext)
    {
        Registry.ITEM.getTagOrEmpty(this.tag).forEach((p_205094_) ->
        {
            pStackConsumer.accept(new ItemStack(p_205094_));
        });
    }

    private boolean expandTag(LootContext pContext, Consumer<LootPoolEntry> pGeneratorConsumer)
    {
        if (!this.canRun(pContext))
        {
            return false;
        }
        else
        {
            for (final Holder<Item> holder : Registry.ITEM.getTagOrEmpty(this.tag))
            {
                pGeneratorConsumer.accept(new LootPoolSingletonContainer.EntryBase()
                {
                    public void createItemStack(Consumer<ItemStack> p_79869_, LootContext p_79870_)
                    {
                        p_79869_.accept(new ItemStack(holder));
                    }
                });
            }

            return true;
        }
    }

    public boolean expand(LootContext pLootContext, Consumer<LootPoolEntry> pEntryConsumer)
    {
        return this.expand ? this.expandTag(pLootContext, pEntryConsumer) : super.expand(pLootContext, pEntryConsumer);
    }

    public static LootPoolSingletonContainer.Builder<?> tagContents(TagKey<Item> pTag)
    {
        return simpleBuilder((p_205099_, p_205100_, p_205101_, p_205102_) ->
        {
            return new TagEntry(pTag, false, p_205099_, p_205100_, p_205101_, p_205102_);
        });
    }

    public static LootPoolSingletonContainer.Builder<?> expandTag(TagKey<Item> pTag)
    {
        return simpleBuilder((p_205088_, p_205089_, p_205090_, p_205091_) ->
        {
            return new TagEntry(pTag, true, p_205088_, p_205089_, p_205090_, p_205091_);
        });
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<TagEntry>
    {
        public void serializeCustom(JsonObject pObject, TagEntry pContext, JsonSerializationContext pConditions)
        {
            super.serializeCustom(pObject, pContext, pConditions);
            pObject.addProperty("name", pContext.tag.location().toString());
            pObject.addProperty("expand", pContext.expand);
        }

        protected TagEntry b(JsonObject p_79873_, JsonDeserializationContext p_79874_, int p_79875_, int p_79876_, LootItemCondition[] p_79877_, LootItemFunction[] p_79878_)
        {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_79873_, "name"));
            TagKey<Item> tagkey = TagKey.create(Registry.ITEM_REGISTRY, resourcelocation);
            boolean flag = GsonHelper.getAsBoolean(p_79873_, "expand");
            return new TagEntry(tagkey, flag, p_79875_, p_79876_, p_79877_, p_79878_);
        }
    }
}
