package net.minecraft.world.item.alchemy;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.optifine.Config;
import net.optifine.CustomColors;

public class PotionUtils
{
    public static final String TAG_CUSTOM_POTION_EFFECTS = "CustomPotionEffects";
    public static final String TAG_CUSTOM_POTION_COLOR = "CustomPotionColor";
    public static final String TAG_POTION = "Potion";
    private static final int EMPTY_COLOR = 16253176;
    private static final Component NO_EFFECT = Component.translatable("effect.none").withStyle(ChatFormatting.GRAY);

    public static List<MobEffectInstance> getMobEffects(ItemStack pStack)
    {
        return getAllEffects(pStack.getTag());
    }

    public static List<MobEffectInstance> getAllEffects(Potion pPotion, Collection<MobEffectInstance> pEffects)
    {
        List<MobEffectInstance> list = Lists.newArrayList();
        list.addAll(pPotion.getEffects());
        list.addAll(pEffects);
        return list;
    }

    public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag pCompoundTag)
    {
        List<MobEffectInstance> list = Lists.newArrayList();
        list.addAll(getPotion(pCompoundTag).getEffects());
        getCustomEffects(pCompoundTag, list);
        return list;
    }

    public static List<MobEffectInstance> getCustomEffects(ItemStack pCompoundTag)
    {
        return getCustomEffects(pCompoundTag.getTag());
    }

    public static List<MobEffectInstance> getCustomEffects(@Nullable CompoundTag pCompoundTag)
    {
        List<MobEffectInstance> list = Lists.newArrayList();
        getCustomEffects(pCompoundTag, list);
        return list;
    }

    public static void getCustomEffects(@Nullable CompoundTag pCompoundTag, List<MobEffectInstance> pEffectList)
    {
        if (pCompoundTag != null && pCompoundTag.contains("CustomPotionEffects", 9))
        {
            ListTag listtag = pCompoundTag.getList("CustomPotionEffects", 10);

            for (int i = 0; i < listtag.size(); ++i)
            {
                CompoundTag compoundtag = listtag.getCompound(i);
                MobEffectInstance mobeffectinstance = MobEffectInstance.load(compoundtag);

                if (mobeffectinstance != null)
                {
                    pEffectList.add(mobeffectinstance);
                }
            }
        }
    }

    public static int getColor(ItemStack pEffects)
    {
        CompoundTag compoundtag = pEffects.getTag();

        if (compoundtag != null && compoundtag.contains("CustomPotionColor", 99))
        {
            return compoundtag.getInt("CustomPotionColor");
        }
        else
        {
            return getPotion(pEffects) == Potions.EMPTY ? 16253176 : getColor(getMobEffects(pEffects));
        }
    }

    public static int getColor(Potion pEffects)
    {
        return pEffects == Potions.EMPTY ? 16253176 : getColor(pEffects.getEffects());
    }

    public static int getColor(Collection<MobEffectInstance> pEffects)
    {
        int i = 3694022;

        if (pEffects.isEmpty())
        {
            return Config.isCustomColors() ? CustomColors.getPotionColor((MobEffect)null, i) : 3694022;
        }
        else
        {
            float f = 0.0F;
            float f1 = 0.0F;
            float f2 = 0.0F;
            int j = 0;

            for (MobEffectInstance mobeffectinstance : pEffects)
            {
                if (mobeffectinstance.isVisible())
                {
                    int k = mobeffectinstance.getEffect().getColor();

                    if (Config.isCustomColors())
                    {
                        k = CustomColors.getPotionColor(mobeffectinstance.getEffect(), k);
                    }

                    int l = mobeffectinstance.getAmplifier() + 1;
                    f += (float)(l * (k >> 16 & 255)) / 255.0F;
                    f1 += (float)(l * (k >> 8 & 255)) / 255.0F;
                    f2 += (float)(l * (k >> 0 & 255)) / 255.0F;
                    j += l;
                }
            }

            if (j == 0)
            {
                return 0;
            }
            else
            {
                f = f / (float)j * 255.0F;
                f1 = f1 / (float)j * 255.0F;
                f2 = f2 / (float)j * 255.0F;
                return (int)f << 16 | (int)f1 << 8 | (int)f2;
            }
        }
    }

    public static Potion getPotion(ItemStack pCompoundTag)
    {
        return getPotion(pCompoundTag.getTag());
    }

    public static Potion getPotion(@Nullable CompoundTag pCompoundTag)
    {
        return pCompoundTag == null ? Potions.EMPTY : Potion.byName(pCompoundTag.getString("Potion"));
    }

    public static ItemStack setPotion(ItemStack pStack, Potion pPotion)
    {
        ResourceLocation resourcelocation = Registry.POTION.getKey(pPotion);

        if (pPotion == Potions.EMPTY)
        {
            pStack.removeTagKey("Potion");
        }
        else
        {
            pStack.getOrCreateTag().putString("Potion", resourcelocation.toString());
        }

        return pStack;
    }

    public static ItemStack setCustomEffects(ItemStack pStack, Collection<MobEffectInstance> pEffects)
    {
        if (pEffects.isEmpty())
        {
            return pStack;
        }
        else
        {
            CompoundTag compoundtag = pStack.getOrCreateTag();
            ListTag listtag = compoundtag.getList("CustomPotionEffects", 9);

            for (MobEffectInstance mobeffectinstance : pEffects)
            {
                listtag.add(mobeffectinstance.save(new CompoundTag()));
            }

            compoundtag.put("CustomPotionEffects", listtag);
            return pStack;
        }
    }

    public static void addPotionTooltip(ItemStack pStack, List<Component> pTooltips, float pDurationFactor)
    {
        List<MobEffectInstance> list = getMobEffects(pStack);
        List<Pair<Attribute, AttributeModifier>> list1 = Lists.newArrayList();

        if (list.isEmpty())
        {
            pTooltips.add(NO_EFFECT);
        }
        else
        {
            for (MobEffectInstance mobeffectinstance : list)
            {
                MutableComponent mutablecomponent = Component.translatable(mobeffectinstance.getDescriptionId());
                MobEffect mobeffect = mobeffectinstance.getEffect();
                Map<Attribute, AttributeModifier> map = mobeffect.getAttributeModifiers();

                if (!map.isEmpty())
                {
                    for (Map.Entry<Attribute, AttributeModifier> entry : map.entrySet())
                    {
                        AttributeModifier attributemodifier = entry.getValue();
                        AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), mobeffect.getAttributeModifierValue(mobeffectinstance.getAmplifier(), attributemodifier), attributemodifier.getOperation());
                        list1.add(new Pair<>(entry.getKey(), attributemodifier1));
                    }
                }

                if (mobeffectinstance.getAmplifier() > 0)
                {
                    mutablecomponent = Component.a("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + mobeffectinstance.getAmplifier()));
                }

                if (mobeffectinstance.getDuration() > 20)
                {
                    mutablecomponent = Component.a("potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(mobeffectinstance, pDurationFactor));
                }

                pTooltips.add(mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting()));
            }
        }

        if (!list1.isEmpty())
        {
            pTooltips.add(CommonComponents.EMPTY);
            pTooltips.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for (Pair<Attribute, AttributeModifier> pair : list1)
            {
                AttributeModifier attributemodifier2 = pair.getSecond();
                double d0 = attributemodifier2.getAmount();
                double d1;

                if (attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL)
                {
                    d1 = attributemodifier2.getAmount();
                }
                else
                {
                    d1 = attributemodifier2.getAmount() * 100.0D;
                }

                if (d0 > 0.0D)
                {
                    pTooltips.add(Component.a("attribute.modifier.plus." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.BLUE));
                }
                else if (d0 < 0.0D)
                {
                    d1 *= -1.0D;
                    pTooltips.add(Component.a("attribute.modifier.take." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.RED));
                }
            }
        }
    }
}
