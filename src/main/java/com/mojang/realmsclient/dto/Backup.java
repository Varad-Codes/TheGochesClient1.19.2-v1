package com.mojang.realmsclient.dto;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;

public class Backup extends ValueObject
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public String backupId;
    public Date lastModifiedDate;
    public long size;
    private boolean uploadedVersion;
    public Map<String, String> metadata = Maps.newHashMap();
    public Map<String, String> changeList = Maps.newHashMap();

    public static Backup parse(JsonElement pJson)
    {
        JsonObject jsonobject = pJson.getAsJsonObject();
        Backup backup = new Backup();

        try
        {
            backup.backupId = JsonUtils.getStringOr("backupId", jsonobject, "");
            backup.lastModifiedDate = JsonUtils.getDateOr("lastModifiedDate", jsonobject);
            backup.size = JsonUtils.getLongOr("size", jsonobject, 0L);

            if (jsonobject.has("metadata"))
            {
                JsonObject jsonobject1 = jsonobject.getAsJsonObject("metadata");

                for (Map.Entry<String, JsonElement> entry : jsonobject1.entrySet())
                {
                    if (!entry.getValue().isJsonNull())
                    {
                        backup.metadata.put(format(entry.getKey()), entry.getValue().getAsString());
                    }
                }
            }
        }
        catch (Exception exception)
        {
            LOGGER.error("Could not parse Backup: {}", (Object)exception.getMessage());
        }

        return backup;
    }

    private static String format(String p_87402_)
    {
        String[] astring = p_87402_.split("_");
        StringBuilder stringbuilder = new StringBuilder();

        for (String s : astring)
        {
            if (s != null && s.length() >= 1)
            {
                if ("of".equals(s))
                {
                    stringbuilder.append(s).append(" ");
                }
                else
                {
                    char c0 = Character.toUpperCase(s.charAt(0));
                    stringbuilder.append(c0).append(s.substring(1)).append(" ");
                }
            }
        }

        return stringbuilder.toString();
    }

    public boolean isUploadedVersion()
    {
        return this.uploadedVersion;
    }

    public void setUploadedVersion(boolean pUploadedVersion)
    {
        this.uploadedVersion = pUploadedVersion;
    }
}
