package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.ArrayUtils;

public class AlternativesEntry extends CompositeEntryBase
{
    AlternativesEntry(LootPoolEntryContainer[] p_79384_, LootItemCondition[] p_79385_)
    {
        super(p_79384_, p_79385_);
    }

    public LootPoolEntryType getType()
    {
        return LootPoolEntries.ALTERNATIVES;
    }

    protected ComposableEntryContainer a(ComposableEntryContainer[] p_79390_)
    {
        switch (p_79390_.length)
        {
            case 0:
                return ALWAYS_FALSE;

            case 1:
                return p_79390_[0];

            case 2:
                return p_79390_[0].or(p_79390_[1]);

            default:
                return (p_79393_, p_79394_) ->
                {
                    for (ComposableEntryContainer composableentrycontainer : p_79390_)
                    {
                        if (composableentrycontainer.expand(p_79393_, p_79394_))
                        {
                            return true;
                        }
                    }

                    return false;
                };
        }
    }

    public void validate(ValidationContext pValidationContext)
    {
        super.validate(pValidationContext);

        for (int i = 0; i < this.children.length - 1; ++i)
        {
            if (ArrayUtils.isEmpty((Object[])this.children[i].conditions))
            {
                pValidationContext.reportProblem("Unreachable entry!");
            }
        }
    }

    public static AlternativesEntry.Builder a(LootPoolEntryContainer.Builder<?>... p_79396_)
    {
        return new AlternativesEntry.Builder(p_79396_);
    }

    public static <E> AlternativesEntry.Builder alternatives(Collection<E> p_230934_, Function < E, LootPoolEntryContainer.Builder<? >> p_230935_)
    {
        return new AlternativesEntry.Builder(p_230934_.stream().map(p_230935_::apply).toArray((p_230932_) ->
        {
            return new LootPoolEntryContainer.Builder[p_230932_];
        }));
    }

    public static class Builder extends LootPoolEntryContainer.Builder<AlternativesEntry.Builder>
    {
        private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

        public Builder(LootPoolEntryContainer.Builder<?>... pChildren)
        {
            for (LootPoolEntryContainer.Builder<?> builder : pChildren)
            {
                this.entries.add(builder.build());
            }
        }

        protected AlternativesEntry.Builder getThis()
        {
            return this;
        }

        public AlternativesEntry.Builder otherwise(LootPoolEntryContainer.Builder<?> pChildBuilder)
        {
            this.entries.add(pChildBuilder.build());
            return this;
        }

        public LootPoolEntryContainer build()
        {
            return new AlternativesEntry(this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
        }
    }
}
