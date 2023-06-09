package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SubtitleOverlay extends GuiComponent implements SoundEventListener
{
    private static final long DISPLAY_TIME = 3000L;
    private final Minecraft minecraft;
    private final List<SubtitleOverlay.Subtitle> subtitles = Lists.newArrayList();
    private boolean isListening;

    public SubtitleOverlay(Minecraft pMinecraft)
    {
        this.minecraft = pMinecraft;
    }

    public void render(PoseStack pPoseStack)
    {
        if (!this.isListening && this.minecraft.options.showSubtitles().get())
        {
            this.minecraft.getSoundManager().addListener(this);
            this.isListening = true;
        }
        else if (this.isListening && !this.minecraft.options.showSubtitles().get())
        {
            this.minecraft.getSoundManager().removeListener(this);
            this.isListening = false;
        }

        if (this.isListening && !this.subtitles.isEmpty())
        {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Vec3 vec3 = new Vec3(this.minecraft.player.getX(), this.minecraft.player.getEyeY(), this.minecraft.player.getZ());
            Vec3 vec31 = (new Vec3(0.0D, 0.0D, -1.0D)).xRot(-this.minecraft.player.getXRot() * ((float)Math.PI / 180F)).yRot(-this.minecraft.player.getYRot() * ((float)Math.PI / 180F));
            Vec3 vec32 = (new Vec3(0.0D, 1.0D, 0.0D)).xRot(-this.minecraft.player.getXRot() * ((float)Math.PI / 180F)).yRot(-this.minecraft.player.getYRot() * ((float)Math.PI / 180F));
            Vec3 vec33 = vec31.cross(vec32);
            int i = 0;
            int j = 0;
            Iterator<SubtitleOverlay.Subtitle> iterator = this.subtitles.iterator();

            while (iterator.hasNext())
            {
                SubtitleOverlay.Subtitle subtitleoverlay$subtitle = iterator.next();

                if (subtitleoverlay$subtitle.getTime() + 3000L <= Util.getMillis())
                {
                    iterator.remove();
                }
                else
                {
                    j = Math.max(j, this.minecraft.font.width(subtitleoverlay$subtitle.getText()));
                }
            }

            j += this.minecraft.font.width("<") + this.minecraft.font.width(" ") + this.minecraft.font.width(">") + this.minecraft.font.width(" ");

            for (SubtitleOverlay.Subtitle subtitleoverlay$subtitle1 : this.subtitles)
            {
                int k = 255;
                Component component = subtitleoverlay$subtitle1.getText();
                Vec3 vec34 = subtitleoverlay$subtitle1.getLocation().subtract(vec3).normalize();
                double d0 = -vec33.dot(vec34);
                double d1 = -vec31.dot(vec34);
                boolean flag = d1 > 0.5D;
                int l = j / 2;
                int i1 = 9;
                int j1 = i1 / 2;
                float f = 1.0F;
                int k1 = this.minecraft.font.width(component);
                int l1 = Mth.floor(Mth.clampedLerp(255.0F, 75.0F, (float)(Util.getMillis() - subtitleoverlay$subtitle1.getTime()) / 3000.0F));
                int i2 = l1 << 16 | l1 << 8 | l1;
                pPoseStack.pushPose();
                pPoseStack.translate((double)((float)this.minecraft.getWindow().getGuiScaledWidth() - (float)l * 1.0F - 2.0F), (double)((float)(this.minecraft.getWindow().getGuiScaledHeight() - 35) - (float)(i * (i1 + 1)) * 1.0F), 0.0D);
                pPoseStack.scale(1.0F, 1.0F, 1.0F);
                fill(pPoseStack, -l - 1, -j1 - 1, l + 1, j1 + 1, this.minecraft.options.getBackgroundColor(0.8F));
                RenderSystem.enableBlend();

                if (!flag)
                {
                    if (d0 > 0.0D)
                    {
                        this.minecraft.font.draw(pPoseStack, ">", (float)(l - this.minecraft.font.width(">")), (float)(-j1), i2 + -16777216);
                    }
                    else if (d0 < 0.0D)
                    {
                        this.minecraft.font.draw(pPoseStack, "<", (float)(-l), (float)(-j1), i2 + -16777216);
                    }
                }

                this.minecraft.font.draw(pPoseStack, component, (float)(-k1 / 2), (float)(-j1), i2 + -16777216);
                pPoseStack.popPose();
                ++i;
            }

            RenderSystem.disableBlend();
        }
    }

    public void onPlaySound(SoundInstance pSound, WeighedSoundEvents pAccessor)
    {
        if (pAccessor.getSubtitle() != null)
        {
            Component component = pAccessor.getSubtitle();

            if (!this.subtitles.isEmpty())
            {
                for (SubtitleOverlay.Subtitle subtitleoverlay$subtitle : this.subtitles)
                {
                    if (subtitleoverlay$subtitle.getText().equals(component))
                    {
                        subtitleoverlay$subtitle.refresh(new Vec3(pSound.getX(), pSound.getY(), pSound.getZ()));
                        return;
                    }
                }
            }

            this.subtitles.add(new SubtitleOverlay.Subtitle(component, new Vec3(pSound.getX(), pSound.getY(), pSound.getZ())));
        }
    }

    public static class Subtitle
    {
        private final Component text;
        private long time;
        private Vec3 location;

        public Subtitle(Component pText, Vec3 pLocation)
        {
            this.text = pText;
            this.location = pLocation;
            this.time = Util.getMillis();
        }

        public Component getText()
        {
            return this.text;
        }

        public long getTime()
        {
            return this.time;
        }

        public Vec3 getLocation()
        {
            return this.location;
        }

        public void refresh(Vec3 pLocation)
        {
            this.location = pLocation;
            this.time = Util.getMillis();
        }
    }
}
