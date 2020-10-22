package jp.ngt.rtm;

import cpw.mods.fml.common.registry.GameRegistry;
import jp.ngt.rtm.craft.RecipeManager;
import jp.ngt.rtm.craft.RepairRecipe;
import jp.ngt.rtm.craft.ShapedRecipes55;
import jp.ngt.rtm.item.ItemAmmunition;
import jp.ngt.rtm.item.ItemGun.GunType;
import jp.ngt.rtm.item.ItemInstalledObject.IstlObjType;
import net.minecraft.block.BlockColored;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

public final class RTMRecipe {
	public static void init() {
		RecipeSorter.register("rtm:shaped55", ShapedRecipes55.class, Category.SHAPED, "after:minecraft:shapeless");
		RecipeSorter.register("rtm:repair", RepairRecipe.class, Category.SHAPELESS, "after:minecraft:shapeless");

		ItemStack ingotSteel = new ItemStack(RTMItem.steel_ingot, 1, 0);
		/*ArrayList<ItemStack> ingotSteelList = OreDictionary.getOres("ingotSteel");
		ingotSteel = ingotSteelList.size() > 0 ? ingotSteelList.get(0) : new ItemStack(RTMItem.steel_ingot, 1, 0);*/

		ItemStack dyeRed = new ItemStack(Items.dye, 1, 1);
		ItemStack dyeBlue = new ItemStack(Items.dye, 1, 4);
		ItemStack dyeYellow = new ItemStack(Items.dye, 1, 11);
		ItemStack shaft = new ItemStack(RTMItem.material, 1, 0);
		ItemStack wire = new ItemStack(RTMItem.itemWire, 1, 0);
		ItemStack sheetSteel = new ItemStack(RTMItem.material, 1, 8);
		ItemStack motor = new ItemStack(RTMItem.material, 1, 3);
		ItemStack gunpowder = new ItemStack(RTMItem.material, 1, 4);

		for (int i = 0; i < 16; ++i) {
			ItemStack dye = new ItemStack(Items.dye, 1, i);
			int color = BlockColored.func_150031_c(i);
			//鉄骨
			RecipeManager.addRecipe(new ItemStack(RTMBlock.framework, 6, color), new Object[]{
					"III", "DI ", "III",
					Character.valueOf('I'), ingotSteel,
					Character.valueOf('D'), dye});
			//足場
			RecipeManager.addRecipe(new ItemStack(RTMBlock.scaffold, 6, color), new Object[]{
					"I I", "IDI", "SSS",
					Character.valueOf('I'), shaft,
					Character.valueOf('S'), sheetSteel,
					Character.valueOf('D'), dye});
			//階段
			RecipeManager.addRecipe(new ItemStack(RTMBlock.scaffoldStairs, 6, color), new Object[]{
					"S  ", "SSD", "SSS",
					Character.valueOf('S'), sheetSteel,
					Character.valueOf('D'), dye});
		}

		//鉄柱
		RecipeManager.addRecipe(new ItemStack(RTMBlock.ironPillar, 25, 0), new Object[]{
				"IIIII", "II II", "I I I", "II II", "IIIII",
				Character.valueOf('I'), ingotSteel});

		//マーカー赤
		RecipeManager.addRecipe(new ItemStack(RTMBlock.marker, 16, 0), new Object[]{
				"GGGGG", "G D G", "G D G", "G D G", "GGGGG",
				Character.valueOf('D'), dyeRed,
				Character.valueOf('G'), Blocks.glass});
		//斜め
		RecipeManager.addRecipe(new ItemStack(RTMBlock.marker, 16, 4), new Object[]{
				"GGGGG", "G  DG", "G D G", "GD  G", "GGGGG",
				Character.valueOf('D'), dyeRed,
				Character.valueOf('G'), Blocks.glass});
		//マーカー青
		RecipeManager.addRecipe(new ItemStack(RTMBlock.markerSwitch, 16, 0), new Object[]{
				"GGGGG", "G D G", "G D G", "G D G", "GGGGG",
				Character.valueOf('D'), dyeBlue,
				Character.valueOf('G'), Blocks.glass});
		//斜め
		RecipeManager.addRecipe(new ItemStack(RTMBlock.markerSwitch, 16, 4), new Object[]{
				"GGGGG", "G  DG", "G D G", "GD  G", "GGGGG",
				Character.valueOf('D'), dyeBlue,
				Character.valueOf('G'), Blocks.glass});
		//マーカー黄
		RecipeManager.addRecipe(new ItemStack(RTMBlock.markerSlope, 16, 0), new Object[]{
				"GGGGG", "G D G", "G D G", "G D G", "GGGGG",
				Character.valueOf('D'), dyeYellow,
				Character.valueOf('G'), Blocks.glass});
		RecipeManager.addRecipe(new ItemStack(RTMBlock.markerSlope, 16, 4), new Object[]{
				"GGGGG", "G D G", "G D G", "G   G", "GGGGG",
				Character.valueOf('D'), dyeYellow,
				Character.valueOf('G'), Blocks.glass});
		RecipeManager.addRecipe(new ItemStack(RTMBlock.markerSlope, 16, 8), new Object[]{
				"GGGGG", "G D G", "G   G", "G   G", "GGGGG",
				Character.valueOf('D'), dyeYellow,
				Character.valueOf('G'), Blocks.glass});
		RecipeManager.addRecipe(new ItemStack(RTMBlock.markerSlope, 16, 12), new Object[]{
				"GGGGG", "G   G", "G D G", "G   G", "GGGGG",
				Character.valueOf('D'), dyeYellow,
				Character.valueOf('G'), Blocks.glass});

		//信号変換器(RS->S)
		RecipeManager.addRecipe(new ItemStack(RTMBlock.signalConverter, 1, 0), new Object[]{
				"SSSSS", "SWWWS", "SRIRS", "SWWWS", "SSSSS",
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('W'), wire,
				Character.valueOf('I'), shaft});
		//(S->RS)
		RecipeManager.addRecipe(new ItemStack(RTMBlock.signalConverter, 1, 1), new Object[]{
				"SSSSS", "SWWWS", "SRIRS", "SWWWS", "SSSSS",
				Character.valueOf('R'), Blocks.redstone_torch,
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('W'), wire,
				Character.valueOf('I'), shaft});
		//++
		RecipeManager.addRecipe(new ItemStack(RTMBlock.signalConverter, 1, 2), new Object[]{
				"SSSSS", "SWIWS", "SWIWS", "SWIWS", "SSSSS",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('W'), wire,
				Character.valueOf('I'), shaft});
		//--
		RecipeManager.addRecipe(new ItemStack(RTMBlock.signalConverter, 1, 3), new Object[]{
				"SSSSS", "SWWWS", "SIIIS", "SWWWS", "SSSSS",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('W'), wire,
				Character.valueOf('I'), shaft});

		//WorkBench
		RecipeManager.addRecipe(new ItemStack(RTMBlock.trainWorkBench, 1, 0), new Object[]{
				"III", "ICI", "III",
				Character.valueOf('S'), ingotSteel,
				Character.valueOf('C'), Blocks.crafting_table});
		RecipeManager.addRecipe(new ItemStack(RTMBlock.trainWorkBench, 1, 1), new Object[]{
				"ISI", "SCS", "ISI",
				Character.valueOf('I'), ingotSteel,
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('C'), Blocks.crafting_table});
		//耐火レンガ
		RecipeManager.addRecipe(new ItemStack(RTMBlock.fireBrick, 6, 0), new Object[]{
				"NDN", "DCD", "NDN",
				Character.valueOf('N'), Items.netherbrick,
				Character.valueOf('D'), Blocks.dirt,
				Character.valueOf('C'), Items.clay_ball});
		//熱風炉用レンガ
		RecipeManager.addRecipe(new ItemStack(RTMBlock.hotStoveBrick, 6, 0), new Object[]{
				"NDN", "DCD", "NDN",
				Character.valueOf('N'), Items.netherbrick,
				Character.valueOf('D'), Blocks.soul_sand,
				Character.valueOf('C'), Items.clay_ball});
		//Slot
		RecipeManager.addRecipe(new ItemStack(RTMBlock.slot, 2, 0), new Object[]{
				"SS SS", "SI IS", "SM MS", "SI IS", "SS SS",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel,
				Character.valueOf('M'), motor});
		//ハーフ耐火レンガ
		RecipeManager.addRecipe(new ItemStack(RTMBlock.brickSlab, 6, 0), new Object[]{
				"   ", "   ", "BBB",
				Character.valueOf('B'), RTMBlock.fireBrick});
		//鋼材
		RecipeManager.addRecipe(new ItemStack(RTMBlock.steelMaterial, 2, 0), new Object[]{
				"II", "II",
				Character.valueOf('I'), sheetSteel});

		//台車
		RecipeManager.addRecipe(new ItemStack(RTMItem.bogie, 1, 0), new Object[]{
				" W W ", "PPPPP", " S S ", "PPPPP", " W W ",
				Character.valueOf('W'), new ItemStack(RTMItem.material, 1, 1),
				Character.valueOf('P'), sheetSteel,
				Character.valueOf('S'), shaft});
		//蛍光灯(ガラス)
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 6, 0), new Object[]{
				"GGG", "ILI", "GGG",
				Character.valueOf('I'), Items.iron_ingot,
				Character.valueOf('L'), Items.glowstone_dust,
				Character.valueOf('G'), Blocks.glass_pane});
		//蛍光灯(ダイヤ)
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 6, 1), new Object[]{
				"GGG", "ILI", "GGG",
				Character.valueOf('I'), Items.iron_ingot,
				Character.valueOf('L'), Items.glowstone_dust,
				Character.valueOf('G'), Items.diamond});
		//蛍光灯(カバー)
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 6, 4), new Object[]{
				"GGG", "ILI", "GGG",
				Character.valueOf('I'), Items.iron_ingot,
				Character.valueOf('L'), Items.glowstone_dust,
				Character.valueOf('G'), Blocks.glass});
		//碍子
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 2, IstlObjType.INSULATOR.id), new Object[]{
				"   ", " R ", " I ",
				Character.valueOf('R'), Blocks.sandstone,
				Character.valueOf('I'), ingotSteel});
		//入力コネクタ
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 2, IstlObjType.CONNECTOR_IN.id), new Object[]{
				"   ", " RD", " I ",
				Character.valueOf('R'), Blocks.sandstone,
				Character.valueOf('D'), new ItemStack(Items.dye, 1, 4),
				Character.valueOf('I'), ingotSteel});
		//出力コネクタ
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 2, IstlObjType.CONNECTOR_OUT.id), new Object[]{
				"   ", " RD", " I ",
				Character.valueOf('R'), Blocks.sandstone,
				Character.valueOf('D'), dyeRed,
				Character.valueOf('I'), ingotSteel});

		//架線柱（石）
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemLinePole, 6, 0), new Object[]{
				"ISI", "ISI", " S ",
				Character.valueOf('S'), Blocks.stone,
				Character.valueOf('I'), ingotSteel});
		//架線柱（鉄）
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemLinePole, 6, 1), new Object[]{
				"ISI", "ISI", " S ",
				Character.valueOf('S'), RTMBlock.ironPillar,
				Character.valueOf('I'), ingotSteel});
		//架線柱（石2）
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemLinePole, 6, 2), new Object[]{
				"ISI", " S ", " S ",
				Character.valueOf('S'), Blocks.stone,
				Character.valueOf('I'), ingotSteel});
		//架線柱（鉄）
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemLinePole, 6, 3), new Object[]{
				"III", " I ", " I ",
				Character.valueOf('I'), ingotSteel});

		//踏切
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 1, 5), new Object[]{
				" YIY ", " RIR ", " BIB ", "  I  ", "  I  ",
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('Y'), dyeYellow,
				Character.valueOf('B'), new ItemStack(Items.dye, 1, 0),
				Character.valueOf('I'), ingotSteel});
		//ATC
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 3, IstlObjType.ATC.id), new Object[]{
				"   ", "PPP", "IRI",
				Character.valueOf('R'), Blocks.redstone_torch,
				Character.valueOf('P'), Blocks.stone_pressure_plate,
				Character.valueOf('I'), ingotSteel});
		//検知器
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 3, IstlObjType.TRAIN_DETECTOR.id), new Object[]{
				"   ", "PPP", "IRI",
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('P'), Blocks.stone_pressure_plate,
				Character.valueOf('I'), ingotSteel});
		//Turnstile
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 2, 12), new Object[]{
				"SFS", "SRS", "SRS",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('F'), ingotSteel,
				Character.valueOf('R'), Items.redstone});
		//BumpingPost
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 1, 13), new Object[]{
				"ISI", "I I", "I I",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), shaft});
		//Point
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 1, IstlObjType.POINT.id), new Object[]{
				"     ", "  M  ", "IISI ", "  S  ", "IISI ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), shaft,
				Character.valueOf('M'), Blocks.lever});

		//信号
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemSignal, 2, 0), new Object[]{
				"IGI", "IYI", "IRI", "IYI", "IGI",
				Character.valueOf('R'), dyeRed,
				Character.valueOf('Y'), dyeYellow,
				Character.valueOf('G'), new ItemStack(Items.dye, 1, 2),
				Character.valueOf('I'), sheetSteel});
		//シャフト
		RecipeManager.addRecipe(new ItemStack(RTMItem.material, 3, 0), new Object[]{
				"I  ", "I  ", "I  ",
				Character.valueOf('I'), ingotSteel});
		//ホイール
		RecipeManager.addRecipe(new ItemStack(RTMItem.material, 4, 1), new Object[]{
				" III ", "IIIII", "II II", "IIIII", " III ",
				Character.valueOf('I'), ingotSteel});
		//ディーゼルエンジン
		RecipeManager.addRecipe(new ItemStack(RTMItem.material, 2, 2), new Object[]{
				"SSSSS", "SPPRS", "SFFAA", "SPPRS", "SSSSS",
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('P'), Blocks.piston,
				Character.valueOf('A'), shaft,
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('F'), Blocks.furnace});
		//モーター
		RecipeManager.addRecipe(new ItemStack(RTMItem.material, 2, 3), new Object[]{
				"SSSSS", "SWWRS", "SIIAA", "SWWRS", "SSSSS",
				Character.valueOf('I'), ingotSteel,
				Character.valueOf('W'), wire,
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('A'), shaft,
				Character.valueOf('S'), sheetSteel,});
		//電線
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemWire, 5, 0), new Object[]{
				"     ", "RRRRR", "IIIII", "RRRRR", "     ",
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('I'), ingotSteel});
		//レール
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemLargeRail, 1, 5), new Object[]{
				"WIWIW", " I I ", "WIWIW", " I I ", "WIWIW",
				Character.valueOf('I'), ingotSteel,
				Character.valueOf('W'), Blocks.planks});
		//鋼板
		RecipeManager.addRecipe(new ItemStack(RTMItem.material, 5, 8), new Object[]{
				"     ", "     ", "     ", "     ", "IIIII",
				Character.valueOf('I'), ingotSteel});
		//バール
		RecipeManager.addRecipe(new ItemStack(RTMItem.crowbar, 1), new Object[]{
				" II  ", "  I  ", "  I  ", "  I  ", "  I  ",
				Character.valueOf('I'), ingotSteel});
		//レンチ
		RecipeManager.addRecipe(new ItemStack(RTMItem.wrench, 1), new Object[]{
				" I I ", " III ", "  I  ", "  I  ", "  I  ",
				Character.valueOf('I'), ingotSteel});
		//気動車
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemtrain, 1, 0), new Object[]{
				"FGFGF", "BE EB", "FGFGF",
				Character.valueOf('F'), Blocks.iron_block,
				Character.valueOf('G'), Blocks.glass,
				Character.valueOf('B'), new ItemStack(RTMItem.bogie, 1, 0),
				Character.valueOf('E'), new ItemStack(RTMItem.material, 1, 2)});
		//電車
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemtrain, 1, 1), new Object[]{
				"FGFGF", "BE EB", "FGFGF",
				Character.valueOf('F'), Blocks.iron_block,
				Character.valueOf('G'), Blocks.glass,
				Character.valueOf('B'), new ItemStack(RTMItem.bogie, 1, 0),
				Character.valueOf('E'), motor});
		//貨車
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemtrain, 1, 2), new Object[]{
				"FGFGF", "BE EB", "FGFGF",
				Character.valueOf('F'), Blocks.iron_block,
				Character.valueOf('G'), Blocks.glass,
				Character.valueOf('B'), new ItemStack(RTMItem.bogie, 1, 0),
				Character.valueOf('E'), shaft});
		//Motorman
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemMotorman, 1, 0), new Object[]{
				"  W  ", "IIIII", "  I  ", " I I ", " I I ",
				Character.valueOf('W'), new ItemStack(Items.skull, 1, 1),
				Character.valueOf('I'), ingotSteel});
		//コンテナ
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemCargo, 1, 0), new Object[]{
				"SSSSS", "S   S", "S   S", "S   S", "IIIII",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel});
		//砲
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemCargo, 1, 1), new Object[]{
				"SSSSS", "    S", "SSSSS", " III ", " III ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel});
		//鉄道標識
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemRailroadSign, 2, 0), new Object[]{
				"   ", " S ", " I ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel});
		//Ticket
		RecipeManager.addRecipe(new ItemStack(RTMItem.ticket, 9, 1), new Object[]{
				"   ", "OBO", "PPP",
				Character.valueOf('O'), new ItemStack(Items.dye, 1, 14),
				Character.valueOf('B'), new ItemStack(Items.dye, 1, 0),
				Character.valueOf('P'), Items.paper});
		//金鋸
		RecipeManager.addRecipe(new ItemStack(RTMItem.iron_hacksaw), new Object[]{
				"   ", "IIS", "II ",
				Character.valueOf('S'), Items.stick,
				Character.valueOf('I'), Items.iron_ingot});
		//パドル
		RecipeManager.addRecipe(new ItemStack(RTMItem.paddle), new Object[]{
				" II", " II", "I  ",
				Character.valueOf('I'), Items.iron_ingot});
		//ふいご
		RecipeManager.addRecipe(new ItemStack(RTMItem.bellows), new Object[]{
				"SL ", " LG", "SL ",
				Character.valueOf('S'), Items.stick,
				Character.valueOf('L'), Items.leather,
				Character.valueOf('G'), Items.gold_ingot});
		//パイプ
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemPipe, 4, 0), new Object[]{
				"SSSSS", "S   S", "S   S", "S   S", "SSSSS",
				Character.valueOf('S'), sheetSteel});
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemPipe, 4, 1), new Object[]{
				"SSSSS", "S   S", "S S S", "S   S", "SSSSS",
				Character.valueOf('S'), sheetSteel});
		/*RTMRecipe.addRecipe(new ItemStack(RTMItem.itemPipe, 4, 1),new Object[]{
			"SSSS ", "S  S ", "S  S ", "SSSS ", "     ",
			Character.valueOf('S'), sheetSteel});
		RTMRecipe.addRecipe(new ItemStack(RTMItem.itemPipe, 4, 2),new Object[]{
			"SSS  ", "S S  ", "SSS  ", "     ", "     ",
			Character.valueOf('S'), sheetSteel});
		RTMRecipe.addRecipe(new ItemStack(RTMItem.itemPipe, 4, 3),new Object[]{
			" S   ", "S S  ", " S   ", "     ", "     ",
			Character.valueOf('S'), sheetSteel});*/
		//看板
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 2, 17), new Object[]{
				"     ", "SSSSS", "SFFFS", "SFFFS", "SSSSS",
				Character.valueOf('F'), new ItemStack(RTMItem.installedObject, 1, 0),
				Character.valueOf('S'), sheetSteel});
		//鏡
		RecipeManager.addRecipe(new ItemStack(RTMItem.mirror, 9, 0), new Object[]{
				"GGGGG", "GSSSG", "GSSSG", "GSSSG", "GGGGG",
				Character.valueOf('G'), new ItemStack(Blocks.glass_pane, 1, 0),
				Character.valueOf('S'), sheetSteel});
		for (int i = 1; i < 16; i += 2) {
			int meta = 20 + i;
			String s0 = i == 1 ? " M   " : (i == 3 ? " MM  " : " MMM ");
			String s1 = i <= 5 ? "  G  " : (i <= 13 ? "  GM " : " MGM ");
			String s2 = i <= 7 ? "     " : (i == 9 ? "   M " : (i == 11 ? "  MM " : " MMM "));
			RecipeManager.addRecipe(new ItemStack(RTMItem.mirror, 1, meta), new Object[]{
					"     ", s0, s1, s2, "     ",
					Character.valueOf('M'), new ItemStack(RTMItem.mirror, 1, 0),
					Character.valueOf('G'), new ItemStack(Blocks.glass, 1, 0)});
		}
		//移動装置
		RecipeManager.addRecipe(new ItemStack(RTMBlock.movingMachine, 1, 0), new Object[]{
				"SSS", "SMS", "SSS",
				Character.valueOf('S'), RTMBlock.steelMaterial,
				Character.valueOf('M'), motor});

		//弾
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 3, 1), new Object[]{
				"  S  ", " STS ", "SSTSS", "SSTSS", "SSSSS",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('T'), Blocks.tnt});
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 64, 5), new Object[]{
				" S   ", "SSS  ", "SSS  ", "     ", "     ",
				Character.valueOf('S'), sheetSteel});
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 64, 13), new Object[]{
				" S   ", "SSS  ", "SSS  ", "SSS  ", "     ",
				Character.valueOf('S'), sheetSteel});
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 64, 17), new Object[]{
				" S   ", "SSS  ", "SSS  ", "SSS  ", "SSS  ",
				Character.valueOf('S'), sheetSteel});

		//薬莢
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 5, 2), new Object[]{
				"S   S", "S   S", "S   S", "S   S", "SSISS",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel});
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 64, 6), new Object[]{
				"S S  ", "S S  ", "SSS  ", "     ", "     ",
				Character.valueOf('S'), sheetSteel});
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 64, 14), new Object[]{
				"S S  ", "S S  ", "S S  ", "SSS  ", "     ",
				Character.valueOf('S'), sheetSteel});
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 64, 18), new Object[]{
				"S S  ", "S S  ", "S S  ", "S S  ", "SSS  ",
				Character.valueOf('S'), sheetSteel});

		//弾薬
		for (int i = 0; i < ItemAmmunition.num; ++i) {
			if (i == 2) {
				continue;
			}
			int i0 = i * 4;
			if (i == 0) {
				RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 1, i0), new Object[]{
						" B   ", " T   ", " C   ", "     ", "     ",
						Character.valueOf('B'), new ItemStack(RTMItem.bullet, 1, i0 + 1),
						Character.valueOf('C'), new ItemStack(RTMItem.bullet, 1, i0 + 2),
						Character.valueOf('T'), Blocks.tnt});
			} else if (i == 3) {
				RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 1, i0), new Object[]{
						" B   ", " PP  ", " C   ", "     ", "     ",
						Character.valueOf('B'), new ItemStack(RTMItem.bullet, 1, i0 + 1),
						Character.valueOf('C'), new ItemStack(RTMItem.bullet, 1, i0 + 2),
						Character.valueOf('P'), gunpowder});
			} else if (i == 4) {
				RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 1, i0), new Object[]{
						" B   ", "PPP  ", " C   ", "     ", "     ",
						Character.valueOf('B'), new ItemStack(RTMItem.bullet, 1, i0 + 1),
						Character.valueOf('C'), new ItemStack(RTMItem.bullet, 1, i0 + 2),
						Character.valueOf('P'), gunpowder});
			} else if (i == 5) {
				;
			} else {
				RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 1, i0), new Object[]{
						" B   ", " P   ", " C   ", "     ", "     ",
						Character.valueOf('B'), new ItemStack(RTMItem.bullet, 1, i0 + 1),
						Character.valueOf('C'), new ItemStack(RTMItem.bullet, 1, i0 + 2),
						Character.valueOf('P'), gunpowder});
			}
		}

		//弾倉
		RecipeManager.addRecipe(new ItemStack(RTMItem.magazine_handgun, 3, GunType.handgun.maxSize), new Object[]{
				"S S  ", "S S  ", "SIS  ", "     ", "     ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel});
		RecipeManager.addRecipe(new ItemStack(RTMItem.magazine_rifle, 3, GunType.rifle.maxSize), new Object[]{
				"  S  ", "  S  ", "  S  ", "     ", "     ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel});
		RecipeManager.addRecipe(new ItemStack(RTMItem.magazine_alr, 3, GunType.autoloading_rifle.maxSize), new Object[]{
				" S  S", " S  S", " S  S", " SIIS", "     ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel});
		RecipeManager.addRecipe(new ItemStack(RTMItem.magazine_sr, 3, GunType.sniper_rifle.maxSize), new Object[]{
				" S  S", " S  S", " SIIS", "     ", "     ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel});
		RecipeManager.addRecipe(new ItemStack(RTMItem.magazine_smg, 3, GunType.smg.maxSize), new Object[]{
				" S S ", " S S ", " S S ", " SIS ", "     ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel});
		RecipeManager.addRecipe(new ItemStack(RTMItem.magazine_amr, 3, GunType.amr.maxSize), new Object[]{
				"S   S", "S   S", "SIIIS", "     ", "     ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel});

		//銃
		RecipeManager.addRecipe(new ItemStack(RTMItem.handgun, 1, 0), new Object[]{
				"SSS  ", "  M  ", "     ", "     ", "     ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('M'), new ItemStack(RTMItem.magazine_handgun, 1, 0)});
		RecipeManager.addRecipe(new ItemStack(RTMItem.rifle, 1, 0), new Object[]{
				"SSSM ", "   WW", "     ", "     ", "     ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('W'), Blocks.planks,
				Character.valueOf('M'), new ItemStack(RTMItem.magazine_rifle, 1, 0)});
		RecipeManager.addRecipe(new ItemStack(RTMItem.autoloading_rifle, 1, 0), new Object[]{
				"SSSS ", " M II", "     ", "     ", "     ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel,
				Character.valueOf('M'), new ItemStack(RTMItem.magazine_alr, 1, 0)});
		RecipeManager.addRecipe(new ItemStack(RTMItem.sniper_rifle, 1, 0), new Object[]{
				" SS  ", "SSSS ", " M II", "     ", "     ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel,
				Character.valueOf('M'), new ItemStack(RTMItem.magazine_sr, 1, 0)});
		RecipeManager.addRecipe(new ItemStack(RTMItem.smg, 1, 0), new Object[]{
				"SSSS ", " M II", "     ", "     ", "     ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel,
				Character.valueOf('M'), new ItemStack(RTMItem.magazine_smg, 1, 0)});
		RecipeManager.addRecipe(new ItemStack(RTMItem.amr, 1, 0), new Object[]{
				" SS  ", "SSSS ", " M II", "     ", "     ",
				Character.valueOf('S'), sheetSteel,
				Character.valueOf('I'), ingotSteel,
				Character.valueOf('M'), new ItemStack(RTMItem.magazine_amr, 1, 0)});

		RecipeManager.INSTANCE.addRecipeToManager(new RepairRecipe(RTMItem.magazine_handgun, new ItemStack(RTMItem.bullet, 1, 4)));
		RecipeManager.INSTANCE.addRecipeToManager(new RepairRecipe(RTMItem.magazine_rifle, new ItemStack(RTMItem.bullet, 1, 12)));
		RecipeManager.INSTANCE.addRecipeToManager(new RepairRecipe(RTMItem.magazine_alr, new ItemStack(RTMItem.bullet, 1, 12)));
		RecipeManager.INSTANCE.addRecipeToManager(new RepairRecipe(RTMItem.magazine_sr, new ItemStack(RTMItem.bullet, 1, 12)));
		RecipeManager.INSTANCE.addRecipeToManager(new RepairRecipe(RTMItem.magazine_smg, new ItemStack(RTMItem.bullet, 1, 4)));
		RecipeManager.INSTANCE.addRecipeToManager(new RepairRecipe(RTMItem.magazine_amr, new ItemStack(RTMItem.bullet, 1, 16)));

		//火薬
		GameRegistry.addShapelessRecipe(new ItemStack(RTMItem.material, 64, 4), new Object[]{
				Items.redstone, Items.gunpowder, Items.gunpowder, Items.gunpowder});

		GameRegistry.addSmelting(new ItemStack(Items.coal, 1, 0), new ItemStack(RTMItem.coke), 0.25F);
	}
}