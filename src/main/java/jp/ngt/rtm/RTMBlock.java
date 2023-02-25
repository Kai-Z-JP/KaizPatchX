package jp.ngt.rtm;

import cpw.mods.fml.common.registry.GameRegistry;
import jp.ngt.ngtlib.block.BlockDummy;
import jp.ngt.ngtlib.item.ItemBlockCustom;
import jp.ngt.ngtlib.item.ItemBlockCustomColor;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.block.*;
import jp.ngt.rtm.block.BlockMirror.MirrorType;
import jp.ngt.rtm.block.tileentity.*;
import jp.ngt.rtm.electric.*;
import jp.ngt.rtm.electric.TileEntitySignalConverter.*;
import jp.ngt.rtm.rail.BlockMarker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCompressed;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

import java.util.Random;

public final class RTMBlock {
    public static BlockMarker marker;
    public static BlockMarker markerSwitch;
    public static BlockMarker markerStraight;

    public static Block fluorescent;
    public static Block ironPillar;
    public static Block insulator;
    public static Block signal;
    public static Block crossingGate;
    @Deprecated
    public static Block variableBlock;
    public static Block linePole;
    public static Block connector;
    @Deprecated
    public static Block powerUnit;
    public static Block signalConverter;
    public static Block solidifiedPigIron;
    public static Block railroadSign;
    public static BlockTurnstile turnstile;
    public static Block point;
    @Deprecated
    public static Block rsWire;
    public static Block signboard;
    public static Block trainWorkBench;
    public static Block ticketVendor;
    public static Block mirror;
    public static Block mirrorCube;
    public static Block stationCore;
    public static Block movingMachine;
    public static Block light;
    public static Block speaker;

    public static Block fireBrick;
    public static Block hotStoveBrick;
    public static Block pipe;
    public static Block slot;
    public static Block furnaceFire;
    public static Block exhaustGas;
    public static Block liquefiedPigIron;
    public static Block liquefiedSteel;
    public static Block slag;
    public static Block converterCore;
    public static Block converterBase;
    public static Block steelSlab;
    public static Block brickSlab;
    public static Block brickDoubleSlab;
    public static Block steelMaterial;
    public static Block scaffold;
    public static Block scaffoldStairs;
    public static Block framework;
    public static Block paint;
    public static Block flag;
    public static Block effect;
    public static Block plant_ornament;

    public static short renderIdVariableBlock;
    public static short renderIdSignalBase;
    public static short renderIdLiquid;
    public static short renderIdScaffold;
    public static short renderIdScaffoldStairs;
    public static short renderIdBlockRail;

    public static final Block.SoundType soundTypeMetal2 = new Block.SoundType("metal", 1.0F, 1.0F) {
        private final Random random = new Random();
        private final float[] pitches = {0.875F, 0.9375F, 1.0F, 1.0625F, 1.125F};

        public float getPitch() {
            return this.pitches[this.random.nextInt(this.pitches.length)];
        }

        @Override
        public String getBreakSound() {
            return "rtm:block.metal";
        }

        @Override
        public String getStepResourcePath() {
            return "rtm:block.metal";
        }
    };

    public static void init() {
        renderIdVariableBlock = (short) NGTUtil.getNewRenderType();
        renderIdSignalBase = (short) NGTUtil.getNewRenderType();
        renderIdLiquid = (short) NGTUtil.getNewRenderType();
        renderIdScaffold = (short) NGTUtil.getNewRenderType();
        renderIdScaffoldStairs = (short) NGTUtil.getNewRenderType();
        renderIdBlockRail = (short) NGTUtil.getNewRenderType();

        marker = (BlockMarker) (new BlockMarker(0)).setBlockName("rtm:marker").setBlockTextureName("rtm:marker_0").setHardness(1.0F).setResistance(5.0F).setCreativeTab(CreativeTabRTM.tabRailway);
        markerSwitch = (BlockMarker) (new BlockMarker(1)).setBlockName("rtm:markerSwitch").setBlockTextureName("rtm:marker_0").setHardness(1.0F).setResistance(5.0F).setCreativeTab(CreativeTabRTM.tabRailway);
        markerStraight = (BlockMarker) (new BlockMarker(10)).setBlockName("rtm:markerStraight").setBlockTextureName("rtm:marker_0").setHardness(1.0F).setResistance(5.0F).setCreativeTab(CreativeTabRTM.tabRailway);

        fluorescent = (new BlockFluorescent()).setBlockName("rtm:fluorescent");
        ironPillar = (new BlockIronPillar()).setBlockName("rtm:ironPillar").setBlockTextureName("rtm:ironPillar").setCreativeTab(CreativeTabRTM.tabRailway);
        insulator = (new BlockInsulator()).setBlockName("rtm:insulator").setBlockTextureName("rtm:insulator").setHardness(1.0F).setResistance(5.0F);
        signal = (new BlockSignal()).setBlockName("rtm:signal").setBlockTextureName("rtm:tp").setHardness(1.0F).setResistance(5.0F);
        crossingGate = (new BlockCrossingGate()).setBlockName("rtm:crossing").setHardness(1.0F).setResistance(5.0F);
        variableBlock = (new BlockVariable()).setBlockName("rtm:Variable").setBlockTextureName("rtm:variable");//.setCreativeTab(RTMCore.tabRailway);
        linePole = (new BlockLinePole()).setBlockName("rtm:linePole").setBlockTextureName("rtm:linePoleBase_0");
        connector = (new BlockConnector()).setBlockName("rtm:connector").setBlockTextureName("rtm:connector");
        powerUnit = (new BlockDummy(Material.rock)).setBlockName("rtm:powerUnit").setBlockTextureName("rtm:null");
        signalConverter = (new BlockSignalConverter()).setBlockName("rtm:signalConverter").setBlockTextureName("rtm:signalConverter").setCreativeTab(CreativeTabRTM.tabRailway);

        railroadSign = (new BlockRailroadSign()).setBlockName("rtm:railroadSign").setBlockTextureName("rtm:railroadSign").setHardness(1.0F).setResistance(5.0F);
        turnstile = (BlockTurnstile) (new BlockTurnstile()).setBlockName("rtm:turnstile").setHardness(3.0F).setResistance(20.0F);
        point = (new BlockPoint()).setBlockName("rtm:point").setHardness(3.0F).setResistance(20.0F);
        rsWire = (new BlockRSWire()).setBlockName("rtm:rsWire").setBlockTextureName("rtm:rsWire").setHardness(3.0F).setResistance(20.0F);//.setCreativeTab(RTMCore.tabRailway);
        signboard = (new BlockSignBoard()).setBlockName("rtm:signboard");
        trainWorkBench = (new BlockTrainWorkBench()).setBlockName("rtm:trainWorkBench").setBlockTextureName("rtm:workBench").setCreativeTab(CreativeTabRTM.tabRailway);
        ticketVendor = (new BlockTicketVendor()).setBlockName("rtm:ticketVendor");
        mirror = (new BlockMirror(MirrorType.Mono_Panel)).setBlockName("rtm:mirror").setBlockTextureName("rtm:mirror");
        mirrorCube = (new BlockMirror(MirrorType.Hexa_Cube)).setBlockName("rtm:mirror").setBlockTextureName("rtm:mirror");
        stationCore = (new BlockStation()).setBlockName("rtm:stationCore").setBlockTextureName("rtm:stationCore");//.setCreativeTab(RTMCore.tabRailway);
        movingMachine = (new BlockMovingMachine()).setBlockName("rtm:movingMachine").setBlockTextureName("rtm:movingMachine").setCreativeTab(CreativeTabRTM.tabRailway);
        light = (new BlockLight()).setBlockName("rtm:right").setBlockTextureName("rtm:null");
        speaker = (new BlockSpeaker()).setBlockName("rtm:speaker");

        fireBrick = (new BlockFireBrick(false)).setBlockName("rtm:fireBrick").setBlockTextureName("rtm:fireBrick").setHardness(2.0F).setResistance(10.0F).setCreativeTab(CreativeTabRTM.tabIndustry);
        hotStoveBrick = (new BlockFireBrick(true)).setBlockName("rtm:hotStoveBrick").setBlockTextureName("rtm:hotStoveBrick").setHardness(2.0F).setResistance(10.0F).setCreativeTab(CreativeTabRTM.tabIndustry);
        pipe = (new BlockPipe()).setBlockName("rtm:pipe");
        slot = (new BlockSlot()).setBlockName("rtm:slot").setBlockTextureName("rtm:slot").setHardness(2.0F).setResistance(10.0F).setCreativeTab(CreativeTabRTM.tabIndustry);
        furnaceFire = (new BlockFurnaceFire(true)).setBlockName("rtm:furnaceFire").setBlockTextureName("rtm:furnaceFire");
        exhaustGas = (new BlockFurnaceFire(false)).setBlockName("rtm:exhaustGas").setBlockTextureName("rtm:exhaustGas");
        liquefiedPigIron = (new BlockMeltedMetal()).setBlockName("rtm:pigIron_L").setBlockTextureName("rtm:pigIron_L");
        liquefiedSteel = (new BlockMeltedMetal()).setBlockName("rtm:steel_L").setBlockTextureName("rtm:steel_L");
        slag = (new BlockMeltedMetal()).setBlockName("rtm:slag").setBlockTextureName("rtm:slag");
        converterCore = (new BlockConverter(true)).setBlockName("rtm:converterCore").setHardness(2.0F).setResistance(10.0F);
        converterBase = (new BlockConverter(false)).setBlockName("rtm:converterBase").setHardness(2.0F).setResistance(10.0F);
        steelSlab = (new BlockMetalSlab()).setBlockName("rtm:steelSlab").setBlockTextureName("rtm:steelSlab").setHardness(2.0F).setResistance(10.0F);
        brickSlab = (new BlockBrickSlab(false)).setBlockName("rtm:brickSlab").setBlockTextureName("rtm:fireBrick").setHardness(2.0F).setResistance(10.0F).setCreativeTab(CreativeTabRTM.tabIndustry);
        brickDoubleSlab = (new BlockBrickSlab(true)).setBlockName("rtm:brickSlab").setBlockTextureName("rtm:fireBrick").setHardness(2.0F).setResistance(10.0F).setCreativeTab(CreativeTabRTM.tabIndustry);
        steelMaterial = (new BlockCompressed(MapColor.ironColor)).setBlockName("rtm:steelMaterial").setBlockTextureName("rtm:steelMaterial").setHardness(2.0F).setResistance(10.0F).setStepSound(RTMBlock.soundTypeMetal2).setCreativeTab(CreativeTabRTM.tabIndustry);
        scaffold = (new BlockScaffold()).setBlockName("rtm:scaffold").setBlockTextureName("rtm:framework").setCreativeTab(CreativeTabRTM.tabIndustry);
        scaffoldStairs = (new BlockScaffoldStairs(scaffold)).setBlockName("rtm:scaffoldStairs").setBlockTextureName("rtm:framework").setCreativeTab(CreativeTabRTM.tabIndustry);
        framework = (new BlockLinePole()).setBlockName("rtm:framework").setBlockTextureName("rtm:framework").setStepSound(RTMBlock.soundTypeMetal2).setCreativeTab(CreativeTabRTM.tabIndustry);
        paint = (new BlockPaint()).setBlockName("rtm:paint");
        flag = (new BlockFlag()).setBlockName("rtm:flag").setBlockTextureName("rtm:linePoleBase_3");

        effect = (new BlockEffect()).setBlockName("rtm:effect").setBlockTextureName("rtm:effect").setCreativeTab(CreativeTabRTM.tabRTMTools);

        plant_ornament = (new BlockPlant()).setBlockName("plant_ornament");

        RTMRail.init();

        RTMCore.registerRtmPrefixed(marker, ItemBlockCustomColor.class, "rtm:marker");
        RTMCore.registerRtmPrefixed(markerSwitch, ItemBlockCustomColor.class, "rtm:markerSwitch");
        RTMCore.registerRtmPrefixed(markerStraight, ItemBlockCustomColor.class, "rtm:markerStraight");

        RTMCore.registerRtmPrefixed(fluorescent, "rtm:fluorescent");
        RTMCore.registerRtmPrefixed(ironPillar, "rtm:ironPillar");
        RTMCore.registerRtmPrefixed(insulator, "rtm:insulator");
        RTMCore.registerRtmPrefixed(signal, "rtm:signal");
        RTMCore.registerRtmPrefixed(crossingGate, "rtm:crossing");
        RTMCore.registerRtmPrefixed(variableBlock, "rtm:Variable");
        RTMCore.registerRtmPrefixed(linePole, "rtm:linePole");
        RTMCore.registerRtmPrefixed(connector, "rtm:connector");
        RTMCore.registerRtmPrefixed(powerUnit, "rtm:powerUnit");
        GameRegistry.registerBlock(signalConverter, ItemBlockCustom.class, "signal_converter");
        RTMCore.registerRtmPrefixed(railroadSign, "rtm:railroadSign");
        RTMCore.registerRtmPrefixed(turnstile, "rtm:turnstile");
        RTMCore.registerRtmPrefixed(point, "rtm:point");
        RTMCore.registerRtmPrefixed(rsWire, "rtm:rsWire");
        RTMCore.registerRtmPrefixed(signboard, "rtm:signboard");
        RTMCore.registerRtmPrefixed(trainWorkBench, ItemBlockCustom.class, "rtm:trainWorkBench");
        GameRegistry.registerBlock(ticketVendor, "ticketVendor");
        GameRegistry.registerBlock(mirror, "mirror");
        GameRegistry.registerBlock(mirrorCube, "mirror_cube");
        GameRegistry.registerBlock(stationCore, "station_core");
        GameRegistry.registerBlock(movingMachine, "moving_machine");
        GameRegistry.registerBlock(light, "light_block");
        GameRegistry.registerBlock(speaker, "speaker");

        RTMCore.registerRtmPrefixed(fireBrick, "rtm:fireBrick");
        RTMCore.registerRtmPrefixed(hotStoveBrick, "rtm:hotStoveBrick");
        RTMCore.registerRtmPrefixed(pipe, "rtm:pipe");
        RTMCore.registerRtmPrefixed(slot, "rtm:slot");
        RTMCore.registerRtmPrefixed(furnaceFire, "rtm:furnaceFire");
        RTMCore.registerRtmPrefixed(exhaustGas, "rtm:exhaustGas");
        RTMCore.registerRtmPrefixed(liquefiedPigIron, "rtm:pigIron_L");
        RTMCore.registerRtmPrefixed(liquefiedSteel, "rtm:steel_L");
        RTMCore.registerRtmPrefixed(slag, "rtm:slag");
        RTMCore.registerRtmPrefixed(converterCore, "rtm:converterCore");
        RTMCore.registerRtmPrefixed(converterBase, "rtm:converterBase");
        RTMCore.registerRtmPrefixed(steelSlab, "rtm:steelSlab");
        RTMCore.registerRtmPrefixed(brickSlab, "rtm:brickSlab");
        RTMCore.registerRtmPrefixed(brickDoubleSlab, "rtm:brickDoubleSlab");
        RTMCore.registerRtmPrefixed(steelMaterial, "rtm:steelMaterial");
        GameRegistry.registerBlock(scaffold, "scaffold");
        GameRegistry.registerBlock(scaffoldStairs, "scaffold_stairs");
        GameRegistry.registerBlock(framework, "framework");
        GameRegistry.registerBlock(paint, "paint");
        GameRegistry.registerBlock(flag, "flag");

        GameRegistry.registerBlock(effect, "effect");

        GameRegistry.registerBlock(plant_ornament, "plant_ornament");

        GameRegistry.registerTileEntity(TileEntityFluorescent.class, "fluorescent");
        GameRegistry.registerTileEntity(TileEntityInsulator.class, "TEInsulator");
        GameRegistry.registerTileEntity(TileEntitySignal.class, "TESignal");
        GameRegistry.registerTileEntity(TileEntityCrossingGate.class, "TECrossingGate");
        GameRegistry.registerTileEntity(TileEntityConnector.class, "TEConnector");

        GameRegistry.registerTileEntity(TileEntitySC_RSIn.class, "TESC_RSIn");
        GameRegistry.registerTileEntity(TileEntitySC_RSOut.class, "TESC_RSOut");
        GameRegistry.registerTileEntity(TileEntitySC_Increment.class, "TESC_Inc");
        GameRegistry.registerTileEntity(TileEntitySC_Decrement.class, "TESC_Dec");
        GameRegistry.registerTileEntity(TileEntitySC_Wireless.class, "TESC_Wireless");

        GameRegistry.registerTileEntity(TileEntityRailroadSign.class, "TERailroadSign");
        GameRegistry.registerTileEntity(TileEntityTurnstile.class, "TETurnstile");
        GameRegistry.registerTileEntity(TileEntityPoint.class, "TEPoint");
        GameRegistry.registerTileEntity(TileEntitySignBoard.class, "TESignBoard");
        GameRegistry.registerTileEntity(TileEntityTrainWorkBench.class, "TETrainWorkBench");
        GameRegistry.registerTileEntity(TileEntityTicketVendor.class, "TETicketVendor");
        GameRegistry.registerTileEntity(TileEntityMirror.class, "TEMirror");
        GameRegistry.registerTileEntity(TileEntityStation.class, "TEStation");
        GameRegistry.registerTileEntity(TileEntityMovingMachine.class, "TEMovingMachine");
        GameRegistry.registerTileEntity(TileEntityLight.class, "TELight");
        GameRegistry.registerTileEntity(TileEntitySpeaker.class, "TESpeaker");

        GameRegistry.registerTileEntity(TileEntityPipe.class, "TEPipe");
        GameRegistry.registerTileEntity(TileEntityConverter.class, "TEConverter");
        GameRegistry.registerTileEntity(TileEntityConverterCore.class, "TEConverterCore");
        GameRegistry.registerTileEntity(TileEntitySlot.class, "TESlot");
        GameRegistry.registerTileEntity(TileEntityScaffold.class, "TEScaffold");
        GameRegistry.registerTileEntity(TileEntityScaffoldStairs.class, "TEScaffoldStairs");
        GameRegistry.registerTileEntity(TileEntityPaint.class, "TEPaint");
        GameRegistry.registerTileEntity(TileEntityFlag.class, "TEFlag");

        GameRegistry.registerTileEntity(TileEntityEffect.class, "TEEffect");

        GameRegistry.registerTileEntity(TileEntityPlantOrnament.class, "TEPlantOrnament");
        GameRegistry.registerTileEntity(TileEntityPole.class, "TEPole");
    }
}
