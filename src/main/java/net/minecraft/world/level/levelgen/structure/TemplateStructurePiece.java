package net.minecraft.world.level.levelgen.structure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.function.Function;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public abstract class TemplateStructurePiece extends StructurePiece
{
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final String templateName;
    protected StructureTemplate template;
    protected StructurePlaceSettings placeSettings;
    protected BlockPos templatePosition;

    public TemplateStructurePiece(StructurePieceType pType, int pGenDepth, StructureTemplateManager pStructureManager, ResourceLocation pLocation, String pTemplateName, StructurePlaceSettings pPlaceSettings, BlockPos pTemplatePosition)
    {
        super(pType, pGenDepth, pStructureManager.getOrCreate(pLocation).getBoundingBox(pPlaceSettings, pTemplatePosition));
        this.setOrientation(Direction.NORTH);
        this.templateName = pTemplateName;
        this.templatePosition = pTemplatePosition;
        this.template = pStructureManager.getOrCreate(pLocation);
        this.placeSettings = pPlaceSettings;
    }

    public TemplateStructurePiece(StructurePieceType p_226894_, CompoundTag p_226895_, StructureTemplateManager p_226896_, Function<ResourceLocation, StructurePlaceSettings> p_226897_)
    {
        super(p_226894_, p_226895_);
        this.setOrientation(Direction.NORTH);
        this.templateName = p_226895_.getString("Template");
        this.templatePosition = new BlockPos(p_226895_.getInt("TPX"), p_226895_.getInt("TPY"), p_226895_.getInt("TPZ"));
        ResourceLocation resourcelocation = this.makeTemplateLocation();
        this.template = p_226896_.getOrCreate(resourcelocation);
        this.placeSettings = p_226897_.apply(resourcelocation);
        this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
    }

    protected ResourceLocation makeTemplateLocation()
    {
        return new ResourceLocation(this.templateName);
    }

    protected void addAdditionalSaveData(StructurePieceSerializationContext p_192690_, CompoundTag p_192691_)
    {
        p_192691_.putInt("TPX", this.templatePosition.getX());
        p_192691_.putInt("TPY", this.templatePosition.getY());
        p_192691_.putInt("TPZ", this.templatePosition.getZ());
        p_192691_.putString("Template", this.templateName);
    }

    public void postProcess(WorldGenLevel p_226899_, StructureManager p_226900_, ChunkGenerator p_226901_, RandomSource p_226902_, BoundingBox p_226903_, ChunkPos p_226904_, BlockPos p_226905_)
    {
        this.placeSettings.setBoundingBox(p_226903_);
        this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);

        if (this.template.placeInWorld(p_226899_, this.templatePosition, p_226905_, this.placeSettings, p_226902_, 2))
        {
            for (StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.STRUCTURE_BLOCK))
            {
                if (structuretemplate$structureblockinfo.nbt != null)
                {
                    StructureMode structuremode = StructureMode.valueOf(structuretemplate$structureblockinfo.nbt.getString("mode"));

                    if (structuremode == StructureMode.DATA)
                    {
                        this.handleDataMarker(structuretemplate$structureblockinfo.nbt.getString("metadata"), structuretemplate$structureblockinfo.pos, p_226899_, p_226902_, p_226903_);
                    }
                }
            }

            for (StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo1 : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.JIGSAW))
            {
                if (structuretemplate$structureblockinfo1.nbt != null)
                {
                    String s = structuretemplate$structureblockinfo1.nbt.getString("final_state");
                    BlockState blockstate = Blocks.AIR.defaultBlockState();

                    try
                    {
                        blockstate = BlockStateParser.parseForBlock(Registry.BLOCK, s, true).blockState();
                    }
                    catch (CommandSyntaxException commandsyntaxexception)
                    {
                        LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", s, structuretemplate$structureblockinfo1.pos);
                    }

                    p_226899_.setBlock(structuretemplate$structureblockinfo1.pos, blockstate, 3);
                }
            }
        }
    }

    protected abstract void handleDataMarker(String pMarker, BlockPos pPos, ServerLevelAccessor pLevel, RandomSource pRandom, BoundingBox pBox);

    @Deprecated
    public void move(int pX, int pY, int pZ)
    {
        super.move(pX, pY, pZ);
        this.templatePosition = this.templatePosition.offset(pX, pY, pZ);
    }

    public Rotation getRotation()
    {
        return this.placeSettings.getRotation();
    }

    public StructureTemplate template()
    {
        return this.template;
    }

    public BlockPos templatePosition()
    {
        return this.templatePosition;
    }

    public StructurePlaceSettings placeSettings()
    {
        return this.placeSettings;
    }
}
