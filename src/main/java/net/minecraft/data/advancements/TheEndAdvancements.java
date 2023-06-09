package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.ChangeDimensionTrigger;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LevitationTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.SummonedEntityTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

public class TheEndAdvancements implements Consumer<Consumer<Advancement>>
{
    public void accept(Consumer<Advancement> p_124030_)
    {
        Advancement advancement = Advancement.Builder.advancement().display(Blocks.END_STONE, Component.translatable("advancements.end.root.title"), Component.translatable("advancements.end.root.description"), new ResourceLocation("textures/gui/advancements/backgrounds/end.png"), FrameType.TASK, false, false, false).addCriterion("entered_end", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(Level.END)).save(p_124030_, "end/root");
        Advancement advancement1 = Advancement.Builder.advancement().parent(advancement).display(Blocks.DRAGON_HEAD, Component.translatable("advancements.end.kill_dragon.title"), Component.translatable("advancements.end.kill_dragon.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("killed_dragon", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(EntityType.ENDER_DRAGON))).save(p_124030_, "end/kill_dragon");
        Advancement advancement2 = Advancement.Builder.advancement().parent(advancement1).display(Items.ENDER_PEARL, Component.translatable("advancements.end.enter_end_gateway.title"), Component.translatable("advancements.end.enter_end_gateway.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("entered_end_gateway", EnterBlockTrigger.TriggerInstance.entersBlock(Blocks.END_GATEWAY)).save(p_124030_, "end/enter_end_gateway");
        Advancement.Builder.advancement().parent(advancement1).display(Items.END_CRYSTAL, Component.translatable("advancements.end.respawn_dragon.title"), Component.translatable("advancements.end.respawn_dragon.description"), (ResourceLocation)null, FrameType.GOAL, true, true, false).addCriterion("summoned_dragon", SummonedEntityTrigger.TriggerInstance.summonedEntity(EntityPredicate.Builder.entity().of(EntityType.ENDER_DRAGON))).save(p_124030_, "end/respawn_dragon");
        Advancement advancement3 = Advancement.Builder.advancement().parent(advancement2).display(Blocks.PURPUR_BLOCK, Component.translatable("advancements.end.find_end_city.title"), Component.translatable("advancements.end.find_end_city.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).addCriterion("in_city", PlayerTrigger.TriggerInstance.located(LocationPredicate.inStructure(BuiltinStructures.END_CITY))).save(p_124030_, "end/find_end_city");
        Advancement.Builder.advancement().parent(advancement1).display(Items.DRAGON_BREATH, Component.translatable("advancements.end.dragon_breath.title"), Component.translatable("advancements.end.dragon_breath.description"), (ResourceLocation)null, FrameType.GOAL, true, true, false).addCriterion("dragon_breath", InventoryChangeTrigger.TriggerInstance.a(Items.DRAGON_BREATH)).save(p_124030_, "end/dragon_breath");
        Advancement.Builder.advancement().parent(advancement3).display(Items.SHULKER_SHELL, Component.translatable("advancements.end.levitate.title"), Component.translatable("advancements.end.levitate.description"), (ResourceLocation)null, FrameType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).addCriterion("levitated", LevitationTrigger.TriggerInstance.levitated(DistancePredicate.vertical(MinMaxBounds.Doubles.atLeast(50.0D)))).save(p_124030_, "end/levitate");
        Advancement.Builder.advancement().parent(advancement3).display(Items.ELYTRA, Component.translatable("advancements.end.elytra.title"), Component.translatable("advancements.end.elytra.description"), (ResourceLocation)null, FrameType.GOAL, true, true, false).addCriterion("elytra", InventoryChangeTrigger.TriggerInstance.a(Items.ELYTRA)).save(p_124030_, "end/elytra");
        Advancement.Builder.advancement().parent(advancement1).display(Blocks.DRAGON_EGG, Component.translatable("advancements.end.dragon_egg.title"), Component.translatable("advancements.end.dragon_egg.description"), (ResourceLocation)null, FrameType.GOAL, true, true, false).addCriterion("dragon_egg", InventoryChangeTrigger.TriggerInstance.a(Blocks.DRAGON_EGG)).save(p_124030_, "end/dragon_egg");
    }
}
