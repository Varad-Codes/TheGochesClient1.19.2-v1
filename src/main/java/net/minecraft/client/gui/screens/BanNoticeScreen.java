package net.minecraft.client.gui.screens;

import com.mojang.authlib.minecraft.BanDetails;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.time.Duration;
import java.time.Instant;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;

public class BanNoticeScreen
{
    public static final String URL_MODERATION = "https://aka.ms/mcjavamoderation";
    private static final Component TEMPORARY_BAN_TITLE = Component.translatable("gui.banned.title.temporary").withStyle(ChatFormatting.BOLD);
    private static final Component PERMANENT_BAN_TITLE = Component.translatable("gui.banned.title.permanent").withStyle(ChatFormatting.BOLD);

    public static ConfirmLinkScreen create(BooleanConsumer p_239968_, BanDetails p_239969_)
    {
        return new ConfirmLinkScreen(p_239968_, getBannedTitle(p_239969_), getBannedScreenText(p_239969_), "https://aka.ms/mcjavamoderation", CommonComponents.GUI_ACKNOWLEDGE, true);
    }

    private static Component getBannedTitle(BanDetails p_239953_)
    {
        return isTemporaryBan(p_239953_) ? TEMPORARY_BAN_TITLE : PERMANENT_BAN_TITLE;
    }

    private static Component getBannedScreenText(BanDetails p_239138_)
    {
        return Component.a("gui.banned.description", getBanReasonText(p_239138_), getBanStatusText(p_239138_), Component.literal("https://aka.ms/mcjavamoderation"));
    }

    private static Component getBanReasonText(BanDetails p_239534_)
    {
        String s = p_239534_.reason();
        String s1 = p_239534_.reasonMessage();

        if (StringUtils.isNumeric(s))
        {
            int i = Integer.parseInt(s);
            Component component = ReportReason.getTranslationById(i);
            MutableComponent mutablecomponent;

            if (component != null)
            {
                mutablecomponent = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withBold(true));
            }
            else if (s1 != null)
            {
                mutablecomponent = Component.a("gui.banned.description.reason_id_message", i, s1).withStyle(ChatFormatting.BOLD);
            }
            else
            {
                mutablecomponent = Component.a("gui.banned.description.reason_id", i).withStyle(ChatFormatting.BOLD);
            }

            return Component.a("gui.banned.description.reason", mutablecomponent);
        }
        else
        {
            return Component.translatable("gui.banned.description.unknownreason");
        }
    }

    private static Component getBanStatusText(BanDetails p_239319_)
    {
        if (isTemporaryBan(p_239319_))
        {
            Component component = getBanDurationText(p_239319_);
            return Component.a("gui.banned.description.temporary", Component.a("gui.banned.description.temporary.duration", component).withStyle(ChatFormatting.BOLD));
        }
        else
        {
            return Component.translatable("gui.banned.description.permanent").withStyle(ChatFormatting.BOLD);
        }
    }

    private static Component getBanDurationText(BanDetails p_239880_)
    {
        Duration duration = Duration.between(Instant.now(), p_239880_.expires());
        long i = duration.toHours();

        if (i > 72L)
        {
            return CommonComponents.days(duration.toDays());
        }
        else
        {
            return i < 1L ? CommonComponents.minutes(duration.toMinutes()) : CommonComponents.hours(duration.toHours());
        }
    }

    private static boolean isTemporaryBan(BanDetails p_239501_)
    {
        return p_239501_.expires() != null;
    }
}
