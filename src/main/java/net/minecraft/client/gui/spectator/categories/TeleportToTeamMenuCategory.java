package net.minecraft.client.gui.spectator.categories;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.scores.PlayerTeam;

public class TeleportToTeamMenuCategory implements SpectatorMenuCategory, SpectatorMenuItem
{
    private static final Component TELEPORT_TEXT = Component.translatable("spectatorMenu.team_teleport");
    private static final Component TELEPORT_PROMPT = Component.translatable("spectatorMenu.team_teleport.prompt");
    private final List<SpectatorMenuItem> items = Lists.newArrayList();

    public TeleportToTeamMenuCategory()
    {
        Minecraft minecraft = Minecraft.getInstance();

        for (PlayerTeam playerteam : minecraft.level.getScoreboard().getPlayerTeams())
        {
            this.items.add(new TeleportToTeamMenuCategory.TeamSelectionItem(playerteam));
        }
    }

    public List<SpectatorMenuItem> getItems()
    {
        return this.items;
    }

    public Component getPrompt()
    {
        return TELEPORT_PROMPT;
    }

    public void selectItem(SpectatorMenu pMenu)
    {
        pMenu.selectCategory(this);
    }

    public Component getName()
    {
        return TELEPORT_TEXT;
    }

    public void renderIcon(PoseStack p_101882_, float p_101883_, int p_101884_)
    {
        RenderSystem.setShaderTexture(0, SpectatorGui.SPECTATOR_LOCATION);
        GuiComponent.blit(p_101882_, 0, 0, 16.0F, 0.0F, 16, 16, 256, 256);
    }

    public boolean isEnabled()
    {
        for (SpectatorMenuItem spectatormenuitem : this.items)
        {
            if (spectatormenuitem.isEnabled())
            {
                return true;
            }
        }

        return false;
    }

    static class TeamSelectionItem implements SpectatorMenuItem
    {
        private final PlayerTeam team;
        private final ResourceLocation location;
        private final List<PlayerInfo> players;

        public TeamSelectionItem(PlayerTeam p_194115_)
        {
            this.team = p_194115_;
            this.players = Lists.newArrayList();

            for (String s : p_194115_.getPlayers())
            {
                PlayerInfo playerinfo = Minecraft.getInstance().getConnection().getPlayerInfo(s);

                if (playerinfo != null)
                {
                    this.players.add(playerinfo);
                }
            }

            if (this.players.isEmpty())
            {
                this.location = DefaultPlayerSkin.getDefaultSkin();
            }
            else
            {
                String s1 = this.players.get(RandomSource.create().nextInt(this.players.size())).getProfile().getName();
                this.location = AbstractClientPlayer.getSkinLocation(s1);
                AbstractClientPlayer.registerSkinTexture(this.location, s1);
            }
        }

        public void selectItem(SpectatorMenu pMenu)
        {
            pMenu.selectCategory(new TeleportToPlayerMenuCategory(this.players));
        }

        public Component getName()
        {
            return this.team.getDisplayName();
        }

        public void renderIcon(PoseStack p_101898_, float p_101899_, int p_101900_)
        {
            Integer integer = this.team.getColor().getColor();

            if (integer != null)
            {
                float f = (float)(integer >> 16 & 255) / 255.0F;
                float f1 = (float)(integer >> 8 & 255) / 255.0F;
                float f2 = (float)(integer & 255) / 255.0F;
                GuiComponent.fill(p_101898_, 1, 1, 15, 15, Mth.color(f * p_101899_, f1 * p_101899_, f2 * p_101899_) | p_101900_ << 24);
            }

            RenderSystem.setShaderTexture(0, this.location);
            RenderSystem.setShaderColor(p_101899_, p_101899_, p_101899_, (float)p_101900_ / 255.0F);
            PlayerFaceRenderer.draw(p_101898_, 2, 2, 12);
        }

        public boolean isEnabled()
        {
            return !this.players.isEmpty();
        }
    }
}
