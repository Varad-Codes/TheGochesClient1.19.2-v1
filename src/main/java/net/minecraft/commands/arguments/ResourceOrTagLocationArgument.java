package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class ResourceOrTagLocationArgument<T> implements ArgumentType<ResourceOrTagLocationArgument.Result<T>>
{
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
    final ResourceKey <? extends Registry<T >> registryKey;

    public ResourceOrTagLocationArgument(ResourceKey <? extends Registry<T >> p_210949_)
    {
        this.registryKey = p_210949_;
    }

    public static <T> ResourceOrTagLocationArgument<T> resourceOrTag(ResourceKey <? extends Registry<T >> p_210969_)
    {
        return new ResourceOrTagLocationArgument<>(p_210969_);
    }

    public static <T> ResourceOrTagLocationArgument.Result<T> getRegistryType(CommandContext<CommandSourceStack> p_210956_, String p_210957_, ResourceKey<Registry<T>> p_210958_, DynamicCommandExceptionType p_210959_) throws CommandSyntaxException
    {
        ResourceOrTagLocationArgument.Result<?> result = p_210956_.getArgument(p_210957_, ResourceOrTagLocationArgument.Result.class);
        Optional<ResourceOrTagLocationArgument.Result<T>> optional = result.cast(p_210958_);
        return optional.orElseThrow(() ->
        {
            return p_210959_.create(result);
        });
    }

    public ResourceOrTagLocationArgument.Result<T> parse(StringReader p_210951_) throws CommandSyntaxException
    {
        if (p_210951_.canRead() && p_210951_.peek() == '#')
        {
            int i = p_210951_.getCursor();

            try
            {
                p_210951_.skip();
                ResourceLocation resourcelocation1 = ResourceLocation.read(p_210951_);
                return new ResourceOrTagLocationArgument.TagResult<>(TagKey.create(this.registryKey, resourcelocation1));
            }
            catch (CommandSyntaxException commandsyntaxexception)
            {
                p_210951_.setCursor(i);
                throw commandsyntaxexception;
            }
        }
        else
        {
            ResourceLocation resourcelocation = ResourceLocation.read(p_210951_);
            return new ResourceOrTagLocationArgument.ResourceResult<>(ResourceKey.create(this.registryKey, resourcelocation));
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> p_210977_, SuggestionsBuilder p_210978_)
    {
        Object object = p_210977_.getSource();

        if (object instanceof SharedSuggestionProvider sharedsuggestionprovider)
        {
            return sharedsuggestionprovider.suggestRegistryElements(this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ALL, p_210978_, p_210977_);
        }
        else
        {
            return p_210978_.buildFuture();
        }
    }

    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }

    public static class Info<T> implements ArgumentTypeInfo<ResourceOrTagLocationArgument<T>, ResourceOrTagLocationArgument.Info<T>.Template>
    {
        public void serializeToNetwork(ResourceOrTagLocationArgument.Info<T>.Template p_233414_, FriendlyByteBuf p_233415_)
        {
            p_233415_.writeResourceLocation(p_233414_.registryKey.location());
        }

        public ResourceOrTagLocationArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf p_233425_)
        {
            ResourceLocation resourcelocation = p_233425_.readResourceLocation();
            return new ResourceOrTagLocationArgument.Info.Template(ResourceKey.createRegistryKey(resourcelocation));
        }

        public void serializeToJson(ResourceOrTagLocationArgument.Info<T>.Template p_233411_, JsonObject p_233412_)
        {
            p_233412_.addProperty("registry", p_233411_.registryKey.location().toString());
        }

        public ResourceOrTagLocationArgument.Info<T>.Template unpack(ResourceOrTagLocationArgument<T> p_233417_)
        {
            return new ResourceOrTagLocationArgument.Info.Template(p_233417_.registryKey);
        }

        public final class Template implements ArgumentTypeInfo.Template<ResourceOrTagLocationArgument<T>>
        {
            final ResourceKey <? extends Registry<T >> registryKey;

            Template(ResourceKey <? extends Registry<T >> p_233432_)
            {
                this.registryKey = p_233432_;
            }

            public ResourceOrTagLocationArgument<T> instantiate(CommandBuildContext p_233435_)
            {
                return new ResourceOrTagLocationArgument<>(this.registryKey);
            }

            public ArgumentTypeInfo < ResourceOrTagLocationArgument<T>, ? > type()
            {
                return Info.this;
            }
        }
    }

    static record ResourceResult<T>(ResourceKey<T> key) implements ResourceOrTagLocationArgument.Result<T>
    {
        public Either<ResourceKey<T>, TagKey<T>> unwrap()
        {
            return Either.left(this.key);
        }

        public <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey <? extends Registry<E >> p_210988_)
        {
            return this.key.cast(p_210988_).map(ResourceOrTagLocationArgument.ResourceResult::new);
        }

        public boolean test(Holder<T> p_210986_)
        {
            return p_210986_.is(this.key);
        }

        public String asPrintable()
        {
            return this.key.location().toString();
        }
    }

    public interface Result<T> extends Predicate<Holder<T>>
    {
        Either<ResourceKey<T>, TagKey<T>> unwrap();

        <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey <? extends Registry<E >> p_210997_);

        String asPrintable();
    }

    static record TagResult<T>(TagKey<T> key) implements ResourceOrTagLocationArgument.Result<T>
    {
        public Either<ResourceKey<T>, TagKey<T>> unwrap()
        {
            return Either.right(this.key);
        }

        public <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey <? extends Registry<E >> p_211022_)
        {
            return this.key.cast(p_211022_).map(ResourceOrTagLocationArgument.TagResult::new);
        }

        public boolean test(Holder<T> p_211020_)
        {
            return p_211020_.is(this.key);
        }

        public String asPrintable()
        {
            return "#" + this.key.location();
        }
    }
}
