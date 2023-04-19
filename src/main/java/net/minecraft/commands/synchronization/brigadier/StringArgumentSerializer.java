package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class StringArgumentSerializer implements ArgumentTypeInfo<StringArgumentType, StringArgumentSerializer.Template>
{
    public void serializeToNetwork(StringArgumentSerializer.Template pArgument, FriendlyByteBuf pBuffer)
    {
        pBuffer.writeEnum(pArgument.type);
    }

    public StringArgumentSerializer.Template deserializeFromNetwork(FriendlyByteBuf pBuffer)
    {
        StringArgumentType.StringType stringtype = pBuffer.readEnum(StringArgumentType.StringType.class);
        return new StringArgumentSerializer.Template(stringtype);
    }

    public void serializeToJson(StringArgumentSerializer.Template pArgument, JsonObject pJson)
    {
        String s;

        switch (pArgument.type)
        {
            case SINGLE_WORD:
                s = "word";
                break;

            case QUOTABLE_PHRASE:
                s = "phrase";
                break;

            case GREEDY_PHRASE:
                s = "greedy";
                break;

            default:
                throw new IncompatibleClassChangeError();
        }

        pJson.addProperty("type", s);
    }

    public StringArgumentSerializer.Template unpack(StringArgumentType p_235605_)
    {
        return new StringArgumentSerializer.Template(p_235605_.getType());
    }

    public final class Template implements ArgumentTypeInfo.Template<StringArgumentType>
    {
        final StringArgumentType.StringType type;

        public Template(StringArgumentType.StringType p_235626_)
        {
            this.type = p_235626_;
        }

        public StringArgumentType instantiate(CommandBuildContext p_235629_)
        {
            StringArgumentType stringargumenttype;

            switch (this.type)
            {
                case SINGLE_WORD:
                    stringargumenttype = StringArgumentType.word();
                    break;

                case QUOTABLE_PHRASE:
                    stringargumenttype = StringArgumentType.string();
                    break;

                case GREEDY_PHRASE:
                    stringargumenttype = StringArgumentType.greedyString();
                    break;

                default:
                    throw new IncompatibleClassChangeError();
            }

            return stringargumenttype;
        }

        public ArgumentTypeInfo < StringArgumentType, ? > type()
        {
            return StringArgumentSerializer.this;
        }
    }
}
