package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import org.slf4j.Logger;

public class RealmsServerAddress extends ValueObject
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public String address;
    public String resourcePackUrl;
    public String resourcePackHash;

    public static RealmsServerAddress parse(String pJson)
    {
        JsonParser jsonparser = new JsonParser();
        RealmsServerAddress realmsserveraddress = new RealmsServerAddress();

        try
        {
            JsonObject jsonobject = jsonparser.parse(pJson).getAsJsonObject();
            realmsserveraddress.address = JsonUtils.getStringOr("address", jsonobject, (String)null);
            realmsserveraddress.resourcePackUrl = JsonUtils.getStringOr("resourcePackUrl", jsonobject, (String)null);
            realmsserveraddress.resourcePackHash = JsonUtils.getStringOr("resourcePackHash", jsonobject, (String)null);
        }
        catch (Exception exception)
        {
            LOGGER.error("Could not parse RealmsServerAddress: {}", (Object)exception.getMessage());
        }

        return realmsserveraddress;
    }
}
