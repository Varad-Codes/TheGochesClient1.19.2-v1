package net.minecraft.world.level.levelgen.structure.pieces;

import java.util.Locale;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasurePieces;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidPiece;
import net.minecraft.world.level.levelgen.structure.structures.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.structures.IglooPieces;
import net.minecraft.world.level.levelgen.structure.structures.JungleTemplePiece;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;
import net.minecraft.world.level.levelgen.structure.structures.NetherFossilPieces;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.structures.OceanRuinPieces;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.structures.ShipwreckPieces;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.structures.SwampHutPiece;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public interface StructurePieceType
{
    StructurePieceType MINE_SHAFT_CORRIDOR = setPieceId(MineshaftPieces.MineShaftCorridor::new, "MSCorridor");
    StructurePieceType MINE_SHAFT_CROSSING = setPieceId(MineshaftPieces.MineShaftCrossing::new, "MSCrossing");
    StructurePieceType MINE_SHAFT_ROOM = setPieceId(MineshaftPieces.MineShaftRoom::new, "MSRoom");
    StructurePieceType MINE_SHAFT_STAIRS = setPieceId(MineshaftPieces.MineShaftStairs::new, "MSStairs");
    StructurePieceType NETHER_FORTRESS_BRIDGE_CROSSING = setPieceId(NetherFortressPieces.BridgeCrossing::new, "NeBCr");
    StructurePieceType NETHER_FORTRESS_BRIDGE_END_FILLER = setPieceId(NetherFortressPieces.BridgeEndFiller::new, "NeBEF");
    StructurePieceType NETHER_FORTRESS_BRIDGE_STRAIGHT = setPieceId(NetherFortressPieces.BridgeStraight::new, "NeBS");
    StructurePieceType NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS = setPieceId(NetherFortressPieces.CastleCorridorStairsPiece::new, "NeCCS");
    StructurePieceType NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY = setPieceId(NetherFortressPieces.CastleCorridorTBalconyPiece::new, "NeCTB");
    StructurePieceType NETHER_FORTRESS_CASTLE_ENTRANCE = setPieceId(NetherFortressPieces.CastleEntrance::new, "NeCE");
    StructurePieceType NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING = setPieceId(NetherFortressPieces.CastleSmallCorridorCrossingPiece::new, "NeSCSC");
    StructurePieceType NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN = setPieceId(NetherFortressPieces.CastleSmallCorridorLeftTurnPiece::new, "NeSCLT");
    StructurePieceType NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR = setPieceId(NetherFortressPieces.CastleSmallCorridorPiece::new, "NeSC");
    StructurePieceType NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN = setPieceId(NetherFortressPieces.CastleSmallCorridorRightTurnPiece::new, "NeSCRT");
    StructurePieceType NETHER_FORTRESS_CASTLE_STALK_ROOM = setPieceId(NetherFortressPieces.CastleStalkRoom::new, "NeCSR");
    StructurePieceType NETHER_FORTRESS_MONSTER_THRONE = setPieceId(NetherFortressPieces.MonsterThrone::new, "NeMT");
    StructurePieceType NETHER_FORTRESS_ROOM_CROSSING = setPieceId(NetherFortressPieces.RoomCrossing::new, "NeRC");
    StructurePieceType NETHER_FORTRESS_STAIRS_ROOM = setPieceId(NetherFortressPieces.StairsRoom::new, "NeSR");
    StructurePieceType NETHER_FORTRESS_START = setPieceId(NetherFortressPieces.StartPiece::new, "NeStart");
    StructurePieceType STRONGHOLD_CHEST_CORRIDOR = setPieceId(StrongholdPieces.ChestCorridor::new, "SHCC");
    StructurePieceType STRONGHOLD_FILLER_CORRIDOR = setPieceId(StrongholdPieces.FillerCorridor::new, "SHFC");
    StructurePieceType STRONGHOLD_FIVE_CROSSING = setPieceId(StrongholdPieces.FiveCrossing::new, "SH5C");
    StructurePieceType STRONGHOLD_LEFT_TURN = setPieceId(StrongholdPieces.LeftTurn::new, "SHLT");
    StructurePieceType STRONGHOLD_LIBRARY = setPieceId(StrongholdPieces.Library::new, "SHLi");
    StructurePieceType STRONGHOLD_PORTAL_ROOM = setPieceId(StrongholdPieces.PortalRoom::new, "SHPR");
    StructurePieceType STRONGHOLD_PRISON_HALL = setPieceId(StrongholdPieces.PrisonHall::new, "SHPH");
    StructurePieceType STRONGHOLD_RIGHT_TURN = setPieceId(StrongholdPieces.RightTurn::new, "SHRT");
    StructurePieceType STRONGHOLD_ROOM_CROSSING = setPieceId(StrongholdPieces.RoomCrossing::new, "SHRC");
    StructurePieceType STRONGHOLD_STAIRS_DOWN = setPieceId(StrongholdPieces.StairsDown::new, "SHSD");
    StructurePieceType STRONGHOLD_START = setPieceId(StrongholdPieces.StartPiece::new, "SHStart");
    StructurePieceType STRONGHOLD_STRAIGHT = setPieceId(StrongholdPieces.Straight::new, "SHS");
    StructurePieceType STRONGHOLD_STRAIGHT_STAIRS_DOWN = setPieceId(StrongholdPieces.StraightStairsDown::new, "SHSSD");
    StructurePieceType JUNGLE_PYRAMID_PIECE = setPieceId(JungleTemplePiece::new, "TeJP");
    StructurePieceType OCEAN_RUIN = setTemplatePieceId(OceanRuinPieces.OceanRuinPiece::new, "ORP");
    StructurePieceType IGLOO = setTemplatePieceId(IglooPieces.IglooPiece::new, "Iglu");
    StructurePieceType RUINED_PORTAL = setTemplatePieceId(RuinedPortalPiece::new, "RUPO");
    StructurePieceType SWAMPLAND_HUT = setPieceId(SwampHutPiece::new, "TeSH");
    StructurePieceType DESERT_PYRAMID_PIECE = setPieceId(DesertPyramidPiece::new, "TeDP");
    StructurePieceType OCEAN_MONUMENT_BUILDING = setPieceId(OceanMonumentPieces.MonumentBuilding::new, "OMB");
    StructurePieceType OCEAN_MONUMENT_CORE_ROOM = setPieceId(OceanMonumentPieces.OceanMonumentCoreRoom::new, "OMCR");
    StructurePieceType OCEAN_MONUMENT_DOUBLE_X_ROOM = setPieceId(OceanMonumentPieces.OceanMonumentDoubleXRoom::new, "OMDXR");
    StructurePieceType OCEAN_MONUMENT_DOUBLE_XY_ROOM = setPieceId(OceanMonumentPieces.OceanMonumentDoubleXYRoom::new, "OMDXYR");
    StructurePieceType OCEAN_MONUMENT_DOUBLE_Y_ROOM = setPieceId(OceanMonumentPieces.OceanMonumentDoubleYRoom::new, "OMDYR");
    StructurePieceType OCEAN_MONUMENT_DOUBLE_YZ_ROOM = setPieceId(OceanMonumentPieces.OceanMonumentDoubleYZRoom::new, "OMDYZR");
    StructurePieceType OCEAN_MONUMENT_DOUBLE_Z_ROOM = setPieceId(OceanMonumentPieces.OceanMonumentDoubleZRoom::new, "OMDZR");
    StructurePieceType OCEAN_MONUMENT_ENTRY_ROOM = setPieceId(OceanMonumentPieces.OceanMonumentEntryRoom::new, "OMEntry");
    StructurePieceType OCEAN_MONUMENT_PENTHOUSE = setPieceId(OceanMonumentPieces.OceanMonumentPenthouse::new, "OMPenthouse");
    StructurePieceType OCEAN_MONUMENT_SIMPLE_ROOM = setPieceId(OceanMonumentPieces.OceanMonumentSimpleRoom::new, "OMSimple");
    StructurePieceType OCEAN_MONUMENT_SIMPLE_TOP_ROOM = setPieceId(OceanMonumentPieces.OceanMonumentSimpleTopRoom::new, "OMSimpleT");
    StructurePieceType OCEAN_MONUMENT_WING_ROOM = setPieceId(OceanMonumentPieces.OceanMonumentWingRoom::new, "OMWR");
    StructurePieceType END_CITY_PIECE = setTemplatePieceId(EndCityPieces.EndCityPiece::new, "ECP");
    StructurePieceType WOODLAND_MANSION_PIECE = setTemplatePieceId(WoodlandMansionPieces.WoodlandMansionPiece::new, "WMP");
    StructurePieceType BURIED_TREASURE_PIECE = setPieceId(BuriedTreasurePieces.BuriedTreasurePiece::new, "BTP");
    StructurePieceType SHIPWRECK_PIECE = setTemplatePieceId(ShipwreckPieces.ShipwreckPiece::new, "Shipwreck");
    StructurePieceType NETHER_FOSSIL = setTemplatePieceId(NetherFossilPieces.NetherFossilPiece::new, "NeFos");
    StructurePieceType JIGSAW = setFullContextPieceId(PoolElementStructurePiece::new, "jigsaw");

    StructurePiece load(StructurePieceSerializationContext p_210161_, CompoundTag p_210162_);

    private static StructurePieceType setFullContextPieceId(StructurePieceType p_210159_, String p_210160_)
    {
        return Registry.register(Registry.STRUCTURE_PIECE, p_210160_.toLowerCase(Locale.ROOT), p_210159_);
    }

    private static StructurePieceType setPieceId(StructurePieceType.ContextlessType p_210153_, String p_210154_)
    {
        return setFullContextPieceId(p_210153_, p_210154_);
    }

    private static StructurePieceType setTemplatePieceId(StructurePieceType.StructureTemplateType p_210156_, String p_210157_)
    {
        return setFullContextPieceId(p_210156_, p_210157_);
    }

    public interface ContextlessType extends StructurePieceType
    {
        StructurePiece load(CompoundTag p_210167_);

    default StructurePiece load(StructurePieceSerializationContext p_210164_, CompoundTag p_210165_)
        {
            return this.load(p_210165_);
        }
    }

    public interface StructureTemplateType extends StructurePieceType
    {
        StructurePiece load(StructureTemplateManager p_226963_, CompoundTag p_226964_);

    default StructurePiece load(StructurePieceSerializationContext p_210169_, CompoundTag p_210170_)
        {
            return this.load(p_210169_.structureTemplateManager(), p_210170_);
        }
    }
}
