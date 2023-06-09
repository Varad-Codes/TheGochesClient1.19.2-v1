package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.util.FormattedCharSequence;

public class MutableComponent implements Component
{
    private final ComponentContents contents;
    private final List<Component> siblings;
    private Style style;
    private FormattedCharSequence visualOrderText = FormattedCharSequence.EMPTY;
    @Nullable
    private Language decomposedWith;

    MutableComponent(ComponentContents p_237200_, List<Component> p_237201_, Style p_237202_)
    {
        this.contents = p_237200_;
        this.siblings = p_237201_;
        this.style = p_237202_;
    }

    public static MutableComponent create(ComponentContents p_237205_)
    {
        return new MutableComponent(p_237205_, Lists.newArrayList(), Style.EMPTY);
    }

    public ComponentContents getContents()
    {
        return this.contents;
    }

    public List<Component> getSiblings()
    {
        return this.siblings;
    }

    public MutableComponent setStyle(Style pStyle)
    {
        this.style = pStyle;
        return this;
    }

    public Style getStyle()
    {
        return this.style;
    }

    public MutableComponent append(String pString)
    {
        return this.append(Component.literal(pString));
    }

    public MutableComponent append(Component pString)
    {
        this.siblings.add(pString);
        return this;
    }

    public MutableComponent withStyle(UnaryOperator<Style> pModifyFunc)
    {
        this.setStyle(pModifyFunc.apply(this.getStyle()));
        return this;
    }

    public MutableComponent withStyle(Style pModifyFunc)
    {
        this.setStyle(pModifyFunc.applyTo(this.getStyle()));
        return this;
    }

    public MutableComponent a(ChatFormatting... p_130945_)
    {
        this.setStyle(this.getStyle().a(p_130945_));
        return this;
    }

    public MutableComponent withStyle(ChatFormatting pModifyFunc)
    {
        this.setStyle(this.getStyle().applyFormat(pModifyFunc));
        return this;
    }

    public FormattedCharSequence getVisualOrderText()
    {
        Language language = Language.getInstance();

        if (this.decomposedWith != language)
        {
            this.visualOrderText = language.getVisualOrder(this);
            this.decomposedWith = language;
        }

        return this.visualOrderText;
    }

    public boolean equals(Object p_237209_)
    {
        if (this == p_237209_)
        {
            return true;
        }
        else if (!(p_237209_ instanceof MutableComponent))
        {
            return false;
        }
        else
        {
            MutableComponent mutablecomponent = (MutableComponent)p_237209_;
            return this.contents.equals(mutablecomponent.contents) && this.style.equals(mutablecomponent.style) && this.siblings.equals(mutablecomponent.siblings);
        }
    }

    public int hashCode()
    {
        return Objects.hash(this.contents, this.style, this.siblings);
    }

    public String toString()
    {
        StringBuilder stringbuilder = new StringBuilder(this.contents.toString());
        boolean flag = !this.style.isEmpty();
        boolean flag1 = !this.siblings.isEmpty();

        if (flag || flag1)
        {
            stringbuilder.append('[');

            if (flag)
            {
                stringbuilder.append("style=");
                stringbuilder.append((Object)this.style);
            }

            if (flag && flag1)
            {
                stringbuilder.append(", ");
            }

            if (flag1)
            {
                stringbuilder.append("siblings=");
                stringbuilder.append((Object)this.siblings);
            }

            stringbuilder.append(']');
        }

        return stringbuilder.toString();
    }
}
