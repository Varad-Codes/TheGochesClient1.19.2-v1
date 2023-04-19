package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FormattedCharSequence;
import net.optifine.Lang;
import org.slf4j.Logger;

public class ClientLanguage extends Language
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, String> storage;
    private final boolean defaultRightToLeft;

    private ClientLanguage(Map<String, String> pStorage, boolean pDefaultRightToLeft)
    {
        this.storage = pStorage;
        this.defaultRightToLeft = pDefaultRightToLeft;
    }

    public static ClientLanguage loadFrom(ResourceManager pResourceManager, List<LanguageInfo> pLanguageInfo)
    {
        Map<String, String> map = Maps.newHashMap();
        boolean flag = false;

        for (LanguageInfo languageinfo : pLanguageInfo)
        {
            flag |= languageinfo.isBidirectional();
            String s = languageinfo.getCode();
            String s1 = String.format(Locale.ROOT, "lang/%s.json", s);

            for (String s2 : pResourceManager.getNamespaces())
            {
                try
                {
                    ResourceLocation resourcelocation = new ResourceLocation(s2, s1);
                    appendFrom(s, pResourceManager.getResourceStack(resourcelocation), map);
                    Lang.loadResources(pResourceManager, languageinfo.getCode(), map);
                }
                catch (Exception exception1)
                {
                    LOGGER.warn("Skipped language file: {}:{} ({})", s2, s1, exception1.toString());
                }
            }
        }

        return new ClientLanguage(ImmutableMap.copyOf(map), flag);
    }

    private static void appendFrom(String p_235036_, List<Resource> p_235037_, Map<String, String> p_235038_)
    {
        for (Resource resource : p_235037_)
        {
            try
            {
                InputStream inputstream = resource.open();

                try
                {
                    Language.loadFromJson(inputstream, p_235038_::put);
                }
                catch (Throwable throwable1)
                {
                    if (inputstream != null)
                    {
                        try
                        {
                            inputstream.close();
                        }
                        catch (Throwable throwable)
                        {
                            throwable1.addSuppressed(throwable);
                        }
                    }

                    throw throwable1;
                }

                if (inputstream != null)
                {
                    inputstream.close();
                }
            }
            catch (IOException ioexception1)
            {
                LOGGER.warn("Failed to load translations for {} from pack {}", p_235036_, resource.sourcePackId(), ioexception1);
            }
        }
    }

    public String getOrDefault(String p_118920_)
    {
        return this.storage.getOrDefault(p_118920_, p_118920_);
    }

    public boolean has(String p_118928_)
    {
        return this.storage.containsKey(p_118928_);
    }

    public boolean isDefaultRightToLeft()
    {
        return this.defaultRightToLeft;
    }

    public FormattedCharSequence getVisualOrder(FormattedText p_118925_)
    {
        return FormattedBidiReorder.reorder(p_118925_, this.defaultRightToLeft);
    }

    public Map<String, String> getLanguageData()
    {
        return this.storage;
    }
}
