package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ComponentArgument implements ArgumentType<Component>
{
    private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "\"\"", "\"{\"text\":\"hello world\"}", "[\"\"]");
    public static final DynamicCommandExceptionType ERROR_INVALID_JSON = new DynamicCommandExceptionType((p_87121_) ->
    {
        return Component.a("argument.component.invalid", p_87121_);
    });

    private ComponentArgument()
    {
    }

    public static Component getComponent(CommandContext<CommandSourceStack> pContext, String pName)
    {
        return pContext.getArgument(pName, Component.class);
    }

    public static ComponentArgument textComponent()
    {
        return new ComponentArgument();
    }

    public Component parse(StringReader pReader) throws CommandSyntaxException
    {
        try
        {
            Component component = Component.Serializer.fromJson(pReader);

            if (component == null)
            {
                throw ERROR_INVALID_JSON.createWithContext(pReader, "empty");
            }
            else
            {
                return component;
            }
        }
        catch (Exception exception)
        {
            String s = exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage();
            throw ERROR_INVALID_JSON.createWithContext(pReader, s);
        }
    }

    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }
}
