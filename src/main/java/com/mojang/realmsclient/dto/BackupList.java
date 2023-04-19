package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;

public class BackupList extends ValueObject
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public List<Backup> backups;

    public static BackupList parse(String pJson)
    {
        JsonParser jsonparser = new JsonParser();
        BackupList backuplist = new BackupList();
        backuplist.backups = Lists.newArrayList();

        try
        {
            JsonElement jsonelement = jsonparser.parse(pJson).getAsJsonObject().get("backups");

            if (jsonelement.isJsonArray())
            {
                Iterator<JsonElement> iterator = jsonelement.getAsJsonArray().iterator();

                while (iterator.hasNext())
                {
                    backuplist.backups.add(Backup.parse(iterator.next()));
                }
            }
        }
        catch (Exception exception)
        {
            LOGGER.error("Could not parse BackupList: {}", (Object)exception.getMessage());
        }

        return backuplist;
    }
}
