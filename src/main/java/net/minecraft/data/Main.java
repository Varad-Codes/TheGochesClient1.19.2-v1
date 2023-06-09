package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.info.BiomeParametersDumpReport;
import net.minecraft.data.info.BlockListReport;
import net.minecraft.data.info.CommandsReport;
import net.minecraft.data.info.RegistryDumpReport;
import net.minecraft.data.info.WorldgenRegistryDumpReport;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.data.tags.BannerPatternTagsProvider;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.CatVariantTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.FlatLevelGeneratorPresetTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.data.tags.InstrumentTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.PaintingVariantTagsProvider;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.obfuscate.DontObfuscate;

public class Main
{
    @DontObfuscate
    public static void main(String[] pArgs) throws IOException
    {
        SharedConstants.tryDetectVersion();
        OptionParser optionparser = new OptionParser();
        OptionSpec<Void> optionspec = optionparser.accepts("help", "Show the help menu").forHelp();
        OptionSpec<Void> optionspec1 = optionparser.accepts("server", "Include server generators");
        OptionSpec<Void> optionspec2 = optionparser.accepts("client", "Include client generators");
        OptionSpec<Void> optionspec3 = optionparser.accepts("dev", "Include development tools");
        OptionSpec<Void> optionspec4 = optionparser.accepts("reports", "Include data reports");
        OptionSpec<Void> optionspec5 = optionparser.accepts("validate", "Validate inputs");
        OptionSpec<Void> optionspec6 = optionparser.accepts("all", "Include all generators");
        OptionSpec<String> optionspec7 = optionparser.accepts("output", "Output folder").withRequiredArg().defaultsTo("generated");
        OptionSpec<String> optionspec8 = optionparser.accepts("input", "Input folder").withRequiredArg();
        OptionSet optionset = optionparser.parse(pArgs);

        if (!optionset.has(optionspec) && optionset.hasOptions())
        {
            Path path = Paths.get(optionspec7.value(optionset));
            boolean flag = optionset.has(optionspec6);
            boolean flag1 = flag || optionset.has(optionspec2);
            boolean flag2 = flag || optionset.has(optionspec1);
            boolean flag3 = flag || optionset.has(optionspec3);
            boolean flag4 = flag || optionset.has(optionspec4);
            boolean flag5 = flag || optionset.has(optionspec5);
            DataGenerator datagenerator = createStandardGenerator(path, optionset.valuesOf(optionspec8).stream().map((p_129659_) ->
            {
                return Paths.get(p_129659_);
            }).collect(Collectors.toList()), flag1, flag2, flag3, flag4, flag5, SharedConstants.getCurrentVersion(), true);
            datagenerator.run();
        }
        else
        {
            optionparser.printHelpOn(System.out);
        }
    }

    public static DataGenerator createStandardGenerator(Path p_236680_, Collection<Path> p_236681_, boolean p_236682_, boolean p_236683_, boolean p_236684_, boolean p_236685_, boolean p_236686_, WorldVersion p_236687_, boolean p_236688_)
    {
        DataGenerator datagenerator = new DataGenerator(p_236680_, p_236681_, p_236687_, p_236688_);
        datagenerator.addProvider(p_236682_ || p_236683_, (new SnbtToNbt(datagenerator)).addFilter(new StructureUpdater()));
        datagenerator.addProvider(p_236682_, new ModelProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new AdvancementProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new LootTableProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new RecipeProvider(datagenerator));
        BlockTagsProvider blocktagsprovider = new BlockTagsProvider(datagenerator);
        datagenerator.addProvider(p_236683_, blocktagsprovider);
        datagenerator.addProvider(p_236683_, new ItemTagsProvider(datagenerator, blocktagsprovider));
        datagenerator.addProvider(p_236683_, new BannerPatternTagsProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new BiomeTagsProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new CatVariantTagsProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new EntityTypeTagsProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new FlatLevelGeneratorPresetTagsProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new FluidTagsProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new GameEventTagsProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new InstrumentTagsProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new PaintingVariantTagsProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new PoiTypeTagsProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new StructureTagsProvider(datagenerator));
        datagenerator.addProvider(p_236683_, new WorldPresetTagsProvider(datagenerator));
        datagenerator.addProvider(p_236684_, new NbtToSnbt(datagenerator));
        datagenerator.addProvider(p_236685_, new BiomeParametersDumpReport(datagenerator));
        datagenerator.addProvider(p_236685_, new BlockListReport(datagenerator));
        datagenerator.addProvider(p_236685_, new CommandsReport(datagenerator));
        datagenerator.addProvider(p_236685_, new RegistryDumpReport(datagenerator));
        datagenerator.addProvider(p_236685_, new WorldgenRegistryDumpReport(datagenerator));
        return datagenerator;
    }
}
