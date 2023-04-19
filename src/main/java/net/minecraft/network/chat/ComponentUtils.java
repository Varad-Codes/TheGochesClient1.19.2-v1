package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;

public class ComponentUtils
{
    public static final String DEFAULT_SEPARATOR_TEXT = ", ";
    public static final Component DEFAULT_SEPARATOR = Component.literal(", ").withStyle(ChatFormatting.GRAY);
    public static final Component DEFAULT_NO_STYLE_SEPARATOR = Component.literal(", ");

    public static MutableComponent mergeStyles(MutableComponent pComponent, Style pStyle)
    {
        if (pStyle.isEmpty())
        {
            return pComponent;
        }
        else
        {
            Style style = pComponent.getStyle();

            if (style.isEmpty())
            {
                return pComponent.setStyle(pStyle);
            }
            else
            {
                return style.equals(pStyle) ? pComponent : pComponent.setStyle(style.applyTo(pStyle));
            }
        }
    }

    public static Optional<MutableComponent> updateForEntity(@Nullable CommandSourceStack pCommandSourceStack, Optional<Component> pOptionalComponent, @Nullable Entity pEntity, int pRecursionDepth) throws CommandSyntaxException
    {
        return pOptionalComponent.isPresent() ? Optional.of(updateForEntity(pCommandSourceStack, pOptionalComponent.get(), pEntity, pRecursionDepth)) : Optional.empty();
    }

    public static MutableComponent updateForEntity(@Nullable CommandSourceStack pCommandSourceStack, Component pOptionalComponent, @Nullable Entity pEntity, int pRecursionDepth) throws CommandSyntaxException
    {
        if (pRecursionDepth > 100)
        {
            return pOptionalComponent.copy();
        }
        else
        {
            MutableComponent mutablecomponent = pOptionalComponent.getContents().resolve(pCommandSourceStack, pEntity, pRecursionDepth + 1);

            for (Component component : pOptionalComponent.getSiblings())
            {
                mutablecomponent.append(updateForEntity(pCommandSourceStack, component, pEntity, pRecursionDepth + 1));
            }

            return mutablecomponent.withStyle(resolveStyle(pCommandSourceStack, pOptionalComponent.getStyle(), pEntity, pRecursionDepth));
        }
    }

    private static Style resolveStyle(@Nullable CommandSourceStack pCommandSourceStack, Style pStyle, @Nullable Entity pEntity, int pRecursionDepth) throws CommandSyntaxException
    {
        HoverEvent hoverevent = pStyle.getHoverEvent();

        if (hoverevent != null)
        {
            Component component = hoverevent.getValue(HoverEvent.Action.SHOW_TEXT);

            if (component != null)
            {
                HoverEvent hoverevent1 = new HoverEvent(HoverEvent.Action.SHOW_TEXT, updateForEntity(pCommandSourceStack, component, pEntity, pRecursionDepth + 1));
                return pStyle.withHoverEvent(hoverevent1);
            }
        }

        return pStyle;
    }

    public static Component getDisplayName(GameProfile pProfile)
    {
        if (pProfile.getName() != null)
        {
            return Component.literal(pProfile.getName());
        }
        else
        {
            return pProfile.getId() != null ? Component.literal(pProfile.getId().toString()) : Component.literal("(unknown)");
        }
    }

    public static Component formatList(Collection<String> pElements)
    {
        return formatAndSortList(pElements, (p_130742_) ->
        {
            return Component.literal(p_130742_).withStyle(ChatFormatting.GREEN);
        });
    }

    public static <T extends Comparable<T>> Component formatAndSortList(Collection<T> pElements, Function<T, Component> pComponentExtractor)
    {
        if (pElements.isEmpty())
        {
            return CommonComponents.EMPTY;
        }
        else if (pElements.size() == 1)
        {
            return pComponentExtractor.apply(pElements.iterator().next());
        }
        else
        {
            List<T> list = Lists.newArrayList(pElements);
            list.sort(Comparable::compareTo);
            return formatList(list, pComponentExtractor);
        }
    }

    public static <T> Component formatList(Collection <? extends T > pElements, Function<T, Component> pComponentExtractor)
    {
        return formatList(pElements, DEFAULT_SEPARATOR, pComponentExtractor);
    }

    public static <T> MutableComponent formatList(Collection <? extends T > pElements, Optional <? extends Component > pOptionalSeparator, Function<T, Component> pComponentExtractor)
    {
        return formatList(pElements, DataFixUtils.orElse(pOptionalSeparator, DEFAULT_SEPARATOR), pComponentExtractor);
    }

    public static Component formatList(Collection <? extends Component > pElements, Component pComponentExtractor)
    {
        return formatList(pElements, pComponentExtractor, Function.identity());
    }

    public static <T> MutableComponent formatList(Collection <? extends T > pElements, Component pOptionalSeparator, Function<T, Component> pComponentExtractor)
    {
        if (pElements.isEmpty())
        {
            return Component.empty();
        }
        else if (pElements.size() == 1)
        {
            return pComponentExtractor.apply(pElements.iterator().next()).copy();
        }
        else
        {
            MutableComponent mutablecomponent = Component.empty();
            boolean flag = true;

            for (T t : pElements)
            {
                if (!flag)
                {
                    mutablecomponent.append(pOptionalSeparator);
                }

                mutablecomponent.append(pComponentExtractor.apply(t));
                flag = false;
            }

            return mutablecomponent;
        }
    }

    public static MutableComponent wrapInSquareBrackets(Component pToWrap)
    {
        return Component.a("chat.square_brackets", pToWrap);
    }

    public static Component fromMessage(Message pMessage)
    {
        return (Component)(pMessage instanceof Component ? (Component)pMessage : Component.literal(pMessage.getString()));
    }

    public static boolean isTranslationResolvable(@Nullable Component p_237135_)
    {
        if (p_237135_ != null)
        {
            ComponentContents $$2 = p_237135_.getContents();

            if ($$2 instanceof TranslatableContents)
            {
                TranslatableContents translatablecontents = (TranslatableContents)$$2;
                String s = translatablecontents.getKey();
                return Language.getInstance().has(s);
            }
        }

        return true;
    }
}
