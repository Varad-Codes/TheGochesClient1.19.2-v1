package net.minecraft.client.gui.narration;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;

public class NarrationThunk<T>
{
    private final T contents;
    private final BiConsumer<Consumer<String>, T> converter;
    public static final NarrationThunk<?> EMPTY = new NarrationThunk<>(Unit.INSTANCE, (p_169171_, p_169172_) ->
    {
    });

    private NarrationThunk(T pContents, BiConsumer<Consumer<String>, T> pConverter)
    {
        this.contents = pContents;
        this.converter = pConverter;
    }

    public static NarrationThunk<?> from(String p_169161_)
    {
        return new NarrationThunk<>(p_169161_, Consumer::accept);
    }

    public static NarrationThunk<?> from(Component p_169177_)
    {
        return new NarrationThunk<>(p_169177_, (p_169174_, p_169175_) ->
        {
            p_169174_.accept(p_169175_.getString());
        });
    }

    public static NarrationThunk<?> from(List<Component> p_169163_)
    {
        return new NarrationThunk<>(p_169163_, (p_169166_, p_169167_) ->
        {
            p_169163_.stream().map(Component::getString).forEach(p_169166_);
        });
    }

    public void getText(Consumer<String> p_169169_)
    {
        this.converter.accept(p_169169_, this.contents);
    }

    public boolean equals(Object p_169179_)
    {
        if (this == p_169179_)
        {
            return true;
        }
        else if (!(p_169179_ instanceof NarrationThunk))
        {
            return false;
        }
        else
        {
            NarrationThunk<?> narrationthunk = (NarrationThunk)p_169179_;
            return narrationthunk.converter == this.converter && narrationthunk.contents.equals(this.contents);
        }
    }

    public int hashCode()
    {
        int i = this.contents.hashCode();
        return 31 * i + this.converter.hashCode();
    }
}
