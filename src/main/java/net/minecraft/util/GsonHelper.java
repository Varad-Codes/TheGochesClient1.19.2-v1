package net.minecraft.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;

public class GsonHelper
{
    private static final Gson GSON = (new GsonBuilder()).create();

    public static boolean isStringValue(JsonObject pJson, String pMemberName)
    {
        return !isValidPrimitive(pJson, pMemberName) ? false : pJson.getAsJsonPrimitive(pMemberName).isString();
    }

    public static boolean isStringValue(JsonElement pJson)
    {
        return !pJson.isJsonPrimitive() ? false : pJson.getAsJsonPrimitive().isString();
    }

    public static boolean isNumberValue(JsonObject p_144763_, String p_144764_)
    {
        return !isValidPrimitive(p_144763_, p_144764_) ? false : p_144763_.getAsJsonPrimitive(p_144764_).isNumber();
    }

    public static boolean isNumberValue(JsonElement pJson)
    {
        return !pJson.isJsonPrimitive() ? false : pJson.getAsJsonPrimitive().isNumber();
    }

    public static boolean isBooleanValue(JsonObject pJson, String pMemberName)
    {
        return !isValidPrimitive(pJson, pMemberName) ? false : pJson.getAsJsonPrimitive(pMemberName).isBoolean();
    }

    public static boolean isBooleanValue(JsonElement p_144768_)
    {
        return !p_144768_.isJsonPrimitive() ? false : p_144768_.getAsJsonPrimitive().isBoolean();
    }

    public static boolean isArrayNode(JsonObject pJson, String pMemberName)
    {
        return !isValidNode(pJson, pMemberName) ? false : pJson.get(pMemberName).isJsonArray();
    }

    public static boolean isObjectNode(JsonObject p_144773_, String p_144774_)
    {
        return !isValidNode(p_144773_, p_144774_) ? false : p_144773_.get(p_144774_).isJsonObject();
    }

    public static boolean isValidPrimitive(JsonObject pJson, String pMemberName)
    {
        return !isValidNode(pJson, pMemberName) ? false : pJson.get(pMemberName).isJsonPrimitive();
    }

    public static boolean isValidNode(JsonObject pJson, String pMemberName)
    {
        if (pJson == null)
        {
            return false;
        }
        else
        {
            return pJson.get(pMemberName) != null;
        }
    }

    public static String convertToString(JsonElement pJson, String pMemberName)
    {
        if (pJson.isJsonPrimitive())
        {
            return pJson.getAsString();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a string, was " + getType(pJson));
        }
    }

    public static String getAsString(JsonObject pJson, String pMemberName)
    {
        if (pJson.has(pMemberName))
        {
            return convertToString(pJson.get(pMemberName), pMemberName);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a string");
        }
    }

    @Nullable
    @Contract("_,_,!null->!null;_,_,null->_")
    public static String getAsString(JsonObject pJson, String pMemberName, @Nullable String pFallback)
    {
        return pJson.has(pMemberName) ? convertToString(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static Item convertToItem(JsonElement pJson, String pMemberName)
    {
        if (pJson.isJsonPrimitive())
        {
            String s = pJson.getAsString();
            return Registry.ITEM.getOptional(new ResourceLocation(s)).orElseThrow(() ->
            {
                return new JsonSyntaxException("Expected " + pMemberName + " to be an item, was unknown string '" + s + "'");
            });
        }
        else
        {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be an item, was " + getType(pJson));
        }
    }

    public static Item getAsItem(JsonObject pJson, String pMemberName)
    {
        if (pJson.has(pMemberName))
        {
            return convertToItem(pJson.get(pMemberName), pMemberName);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find an item");
        }
    }

    @Nullable
    @Contract("_,_,!null->!null;_,_,null->_")
    public static Item getAsItem(JsonObject p_144747_, String p_144748_, @Nullable Item p_144749_)
    {
        return p_144747_.has(p_144748_) ? convertToItem(p_144747_.get(p_144748_), p_144748_) : p_144749_;
    }

    public static boolean convertToBoolean(JsonElement pJson, String pMemberName)
    {
        if (pJson.isJsonPrimitive())
        {
            return pJson.getAsBoolean();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a Boolean, was " + getType(pJson));
        }
    }

    public static boolean getAsBoolean(JsonObject pJson, String pMemberName)
    {
        if (pJson.has(pMemberName))
        {
            return convertToBoolean(pJson.get(pMemberName), pMemberName);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Boolean");
        }
    }

    public static boolean getAsBoolean(JsonObject pJson, String pMemberName, boolean pFallback)
    {
        return pJson.has(pMemberName) ? convertToBoolean(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static double convertToDouble(JsonElement p_144770_, String p_144771_)
    {
        if (p_144770_.isJsonPrimitive() && p_144770_.getAsJsonPrimitive().isNumber())
        {
            return p_144770_.getAsDouble();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + p_144771_ + " to be a Double, was " + getType(p_144770_));
        }
    }

    public static double getAsDouble(JsonObject p_144785_, String p_144786_)
    {
        if (p_144785_.has(p_144786_))
        {
            return convertToDouble(p_144785_.get(p_144786_), p_144786_);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + p_144786_ + ", expected to find a Double");
        }
    }

    public static double getAsDouble(JsonObject p_144743_, String p_144744_, double p_144745_)
    {
        return p_144743_.has(p_144744_) ? convertToDouble(p_144743_.get(p_144744_), p_144744_) : p_144745_;
    }

    public static float convertToFloat(JsonElement pJson, String pMemberName)
    {
        if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber())
        {
            return pJson.getAsFloat();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a Float, was " + getType(pJson));
        }
    }

    public static float getAsFloat(JsonObject pJson, String pMemberName)
    {
        if (pJson.has(pMemberName))
        {
            return convertToFloat(pJson.get(pMemberName), pMemberName);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Float");
        }
    }

    public static float getAsFloat(JsonObject pJson, String pMemberName, float pFallback)
    {
        return pJson.has(pMemberName) ? convertToFloat(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static long convertToLong(JsonElement pJson, String pMemberName)
    {
        if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber())
        {
            return pJson.getAsLong();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a Long, was " + getType(pJson));
        }
    }

    public static long getAsLong(JsonObject pJson, String pMemberName)
    {
        if (pJson.has(pMemberName))
        {
            return convertToLong(pJson.get(pMemberName), pMemberName);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Long");
        }
    }

    public static long getAsLong(JsonObject pJson, String pMemberName, long pFallback)
    {
        return pJson.has(pMemberName) ? convertToLong(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static int convertToInt(JsonElement pJson, String pMemberName)
    {
        if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber())
        {
            return pJson.getAsInt();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a Int, was " + getType(pJson));
        }
    }

    public static int getAsInt(JsonObject pJson, String pMemberName)
    {
        if (pJson.has(pMemberName))
        {
            return convertToInt(pJson.get(pMemberName), pMemberName);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Int");
        }
    }

    public static int getAsInt(JsonObject pJson, String pMemberName, int pFallback)
    {
        return pJson.has(pMemberName) ? convertToInt(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static byte convertToByte(JsonElement pJson, String pMemberName)
    {
        if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber())
        {
            return pJson.getAsByte();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a Byte, was " + getType(pJson));
        }
    }

    public static byte getAsByte(JsonObject p_144791_, String p_144792_)
    {
        if (p_144791_.has(p_144792_))
        {
            return convertToByte(p_144791_.get(p_144792_), p_144792_);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + p_144792_ + ", expected to find a Byte");
        }
    }

    public static byte getAsByte(JsonObject pJson, String pMemberName, byte pFallback)
    {
        return pJson.has(pMemberName) ? convertToByte(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static char convertToCharacter(JsonElement p_144776_, String p_144777_)
    {
        if (p_144776_.isJsonPrimitive() && p_144776_.getAsJsonPrimitive().isNumber())
        {
            return p_144776_.getAsCharacter();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + p_144777_ + " to be a Character, was " + getType(p_144776_));
        }
    }

    public static char getAsCharacter(JsonObject p_144794_, String p_144795_)
    {
        if (p_144794_.has(p_144795_))
        {
            return convertToCharacter(p_144794_.get(p_144795_), p_144795_);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + p_144795_ + ", expected to find a Character");
        }
    }

    public static char getAsCharacter(JsonObject p_144739_, String p_144740_, char p_144741_)
    {
        return p_144739_.has(p_144740_) ? convertToCharacter(p_144739_.get(p_144740_), p_144740_) : p_144741_;
    }

    public static BigDecimal convertToBigDecimal(JsonElement p_144779_, String p_144780_)
    {
        if (p_144779_.isJsonPrimitive() && p_144779_.getAsJsonPrimitive().isNumber())
        {
            return p_144779_.getAsBigDecimal();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + p_144780_ + " to be a BigDecimal, was " + getType(p_144779_));
        }
    }

    public static BigDecimal getAsBigDecimal(JsonObject p_144797_, String p_144798_)
    {
        if (p_144797_.has(p_144798_))
        {
            return convertToBigDecimal(p_144797_.get(p_144798_), p_144798_);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + p_144798_ + ", expected to find a BigDecimal");
        }
    }

    public static BigDecimal getAsBigDecimal(JsonObject p_144751_, String p_144752_, BigDecimal p_144753_)
    {
        return p_144751_.has(p_144752_) ? convertToBigDecimal(p_144751_.get(p_144752_), p_144752_) : p_144753_;
    }

    public static BigInteger convertToBigInteger(JsonElement p_144782_, String p_144783_)
    {
        if (p_144782_.isJsonPrimitive() && p_144782_.getAsJsonPrimitive().isNumber())
        {
            return p_144782_.getAsBigInteger();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + p_144783_ + " to be a BigInteger, was " + getType(p_144782_));
        }
    }

    public static BigInteger getAsBigInteger(JsonObject p_144800_, String p_144801_)
    {
        if (p_144800_.has(p_144801_))
        {
            return convertToBigInteger(p_144800_.get(p_144801_), p_144801_);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + p_144801_ + ", expected to find a BigInteger");
        }
    }

    public static BigInteger getAsBigInteger(JsonObject p_144755_, String p_144756_, BigInteger p_144757_)
    {
        return p_144755_.has(p_144756_) ? convertToBigInteger(p_144755_.get(p_144756_), p_144756_) : p_144757_;
    }

    public static short convertToShort(JsonElement p_144788_, String p_144789_)
    {
        if (p_144788_.isJsonPrimitive() && p_144788_.getAsJsonPrimitive().isNumber())
        {
            return p_144788_.getAsShort();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + p_144789_ + " to be a Short, was " + getType(p_144788_));
        }
    }

    public static short getAsShort(JsonObject p_144803_, String p_144804_)
    {
        if (p_144803_.has(p_144804_))
        {
            return convertToShort(p_144803_.get(p_144804_), p_144804_);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + p_144804_ + ", expected to find a Short");
        }
    }

    public static short getAsShort(JsonObject p_144759_, String p_144760_, short p_144761_)
    {
        return p_144759_.has(p_144760_) ? convertToShort(p_144759_.get(p_144760_), p_144760_) : p_144761_;
    }

    public static JsonObject convertToJsonObject(JsonElement pJson, String pMemberName)
    {
        if (pJson.isJsonObject())
        {
            return pJson.getAsJsonObject();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a JsonObject, was " + getType(pJson));
        }
    }

    public static JsonObject getAsJsonObject(JsonObject pJson, String pMemberName)
    {
        if (pJson.has(pMemberName))
        {
            return convertToJsonObject(pJson.get(pMemberName), pMemberName);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a JsonObject");
        }
    }

    @Nullable
    @Contract("_,_,!null->!null;_,_,null->_")
    public static JsonObject getAsJsonObject(JsonObject pJson, String pMemberName, @Nullable JsonObject pFallback)
    {
        return pJson.has(pMemberName) ? convertToJsonObject(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static JsonArray convertToJsonArray(JsonElement pJson, String pMemberName)
    {
        if (pJson.isJsonArray())
        {
            return pJson.getAsJsonArray();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a JsonArray, was " + getType(pJson));
        }
    }

    public static JsonArray getAsJsonArray(JsonObject pJson, String pMemberName)
    {
        if (pJson.has(pMemberName))
        {
            return convertToJsonArray(pJson.get(pMemberName), pMemberName);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a JsonArray");
        }
    }

    @Nullable
    @Contract("_,_,!null->!null;_,_,null->_")
    public static JsonArray getAsJsonArray(JsonObject pJson, String pMemberName, @Nullable JsonArray pFallback)
    {
        return pJson.has(pMemberName) ? convertToJsonArray(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static <T> T convertToObject(@Nullable JsonElement pJson, String pMemberName, JsonDeserializationContext pContext, Class <? extends T > pAdapter)
    {
        if (pJson != null)
        {
            return pContext.deserialize(pJson, pAdapter);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + pMemberName);
        }
    }

    public static <T> T getAsObject(JsonObject pJson, String pMemberName, JsonDeserializationContext pContext, Class <? extends T > pAdapter)
    {
        if (pJson.has(pMemberName))
        {
            return convertToObject(pJson.get(pMemberName), pMemberName, pContext, pAdapter);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + pMemberName);
        }
    }

    @Nullable
    @Contract("_,_,!null,_,_->!null;_,_,null,_,_->_")
    public static <T> T getAsObject(JsonObject pJson, String pMemberName, @Nullable T pFallback, JsonDeserializationContext pContext, Class <? extends T > pAdapter)
    {
        return (T)(pJson.has(pMemberName) ? convertToObject(pJson.get(pMemberName), pMemberName, pContext, pAdapter) : pFallback);
    }

    public static String getType(@Nullable JsonElement pJson)
    {
        String s = StringUtils.abbreviateMiddle(String.valueOf((Object)pJson), "...", 10);

        if (pJson == null)
        {
            return "null (missing)";
        }
        else if (pJson.isJsonNull())
        {
            return "null (json)";
        }
        else if (pJson.isJsonArray())
        {
            return "an array (" + s + ")";
        }
        else if (pJson.isJsonObject())
        {
            return "an object (" + s + ")";
        }
        else
        {
            if (pJson.isJsonPrimitive())
            {
                JsonPrimitive jsonprimitive = pJson.getAsJsonPrimitive();

                if (jsonprimitive.isNumber())
                {
                    return "a number (" + s + ")";
                }

                if (jsonprimitive.isBoolean())
                {
                    return "a boolean (" + s + ")";
                }
            }

            return s;
        }
    }

    @Nullable
    public static <T> T fromJson(Gson pGson, Reader pReader, Class<T> pType, boolean pLenient)
    {
        try
        {
            JsonReader jsonreader = new JsonReader(pReader);
            jsonreader.setLenient(pLenient);
            return pGson.getAdapter(pType).read(jsonreader);
        }
        catch (IOException ioexception)
        {
            throw new JsonParseException(ioexception);
        }
    }

    @Nullable
    public static <T> T fromJson(Gson pGson, Reader pReader, TypeToken<T> pType, boolean pLenient)
    {
        try
        {
            JsonReader jsonreader = new JsonReader(pReader);
            jsonreader.setLenient(pLenient);
            return pGson.getAdapter(pType).read(jsonreader);
        }
        catch (IOException ioexception)
        {
            throw new JsonParseException(ioexception);
        }
    }

    @Nullable
    public static <T> T fromJson(Gson pGson, String pReader, TypeToken<T> pType, boolean pLenient)
    {
        return fromJson(pGson, new StringReader(pReader), pType, pLenient);
    }

    @Nullable
    public static <T> T fromJson(Gson pGson, String pReader, Class<T> pType, boolean pLenient)
    {
        return fromJson(pGson, new StringReader(pReader), pType, pLenient);
    }

    @Nullable
    public static <T> T fromJson(Gson pGson, Reader pReader, TypeToken<T> pType)
    {
        return fromJson(pGson, pReader, pType, false);
    }

    @Nullable
    public static <T> T fromJson(Gson pGson, String pReader, TypeToken<T> pType)
    {
        return fromJson(pGson, pReader, pType, false);
    }

    @Nullable
    public static <T> T fromJson(Gson pGson, Reader pReader, Class<T> pType)
    {
        return fromJson(pGson, pReader, pType, false);
    }

    @Nullable
    public static <T> T fromJson(Gson pGson, String pReader, Class<T> pType)
    {
        return fromJson(pGson, pReader, pType, false);
    }

    public static JsonObject parse(String pReader, boolean pLenient)
    {
        return parse(new StringReader(pReader), pLenient);
    }

    public static JsonObject parse(Reader pReader, boolean pLenient)
    {
        return fromJson(GSON, pReader, JsonObject.class, pLenient);
    }

    public static JsonObject parse(String pReader)
    {
        return parse(pReader, false);
    }

    public static JsonObject parse(Reader pReader)
    {
        return parse(pReader, false);
    }

    public static JsonArray parseArray(String p_216215_)
    {
        return parseArray(new StringReader(p_216215_));
    }

    public static JsonArray parseArray(Reader p_144766_)
    {
        return fromJson(GSON, p_144766_, JsonArray.class, false);
    }

    public static String toStableString(JsonElement p_216217_)
    {
        StringWriter stringwriter = new StringWriter();
        JsonWriter jsonwriter = new JsonWriter(stringwriter);

        try
        {
            writeValue(jsonwriter, p_216217_, Comparator.naturalOrder());
        }
        catch (IOException ioexception)
        {
            throw new AssertionError(ioexception);
        }

        return stringwriter.toString();
    }

    public static void writeValue(JsonWriter p_216208_, @Nullable JsonElement p_216209_, @Nullable Comparator<String> p_216210_) throws IOException
    {
        if (p_216209_ != null && !p_216209_.isJsonNull())
        {
            if (p_216209_.isJsonPrimitive())
            {
                JsonPrimitive jsonprimitive = p_216209_.getAsJsonPrimitive();

                if (jsonprimitive.isNumber())
                {
                    p_216208_.value(jsonprimitive.getAsNumber());
                }
                else if (jsonprimitive.isBoolean())
                {
                    p_216208_.value(jsonprimitive.getAsBoolean());
                }
                else
                {
                    p_216208_.value(jsonprimitive.getAsString());
                }
            }
            else if (p_216209_.isJsonArray())
            {
                p_216208_.beginArray();

                for (JsonElement jsonelement : p_216209_.getAsJsonArray())
                {
                    writeValue(p_216208_, jsonelement, p_216210_);
                }

                p_216208_.endArray();
            }
            else
            {
                if (!p_216209_.isJsonObject())
                {
                    throw new IllegalArgumentException("Couldn't write " + p_216209_.getClass());
                }

                p_216208_.beginObject();

                for (Map.Entry<String, JsonElement> entry : sortByKeyIfNeeded(p_216209_.getAsJsonObject().entrySet(), p_216210_))
                {
                    p_216208_.name(entry.getKey());
                    writeValue(p_216208_, entry.getValue(), p_216210_);
                }

                p_216208_.endObject();
            }
        }
        else
        {
            p_216208_.nullValue();
        }
    }

    private static Collection<Map.Entry<String, JsonElement>> sortByKeyIfNeeded(Collection<Map.Entry<String, JsonElement>> p_216212_, @Nullable Comparator<String> p_216213_)
    {
        if (p_216213_ == null)
        {
            return p_216212_;
        }
        else
        {
            List<Map.Entry<String, JsonElement>> list = new ArrayList<>(p_216212_);
            list.sort(Entry.comparingByKey(p_216213_));
            return list;
        }
    }
}
