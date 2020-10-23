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
			RecipeManager.addRecipe(new ItemStack(RTMBlock.framework, 6, color), "III", "DI ", "III",
					'I', ingotSteel,
					'D', dye);
			//足場
			RecipeManager.addRecipe(new ItemStack(RTMBlock.scaffold, 6, color), "I I", "IDI", "SSS",
					'I', shaft,
					'S', sheetSteel,
					'D', dye);
			//階段
			RecipeManager.addRecipe(new ItemStack(RTMBlock.scaffoldStairs, 6, color), "S  ", "SSD", "SSS",
					'S', sheetSteel,
					'D', dye);
		}

		//鉄柱
		RecipeManager.addRecipe(new ItemStack(RTMBlock.ironPillar, 25, 0), "IIIII", "II II", "I I I", "II II", "IIIII",
				'I', ingotSteel);

		//マーカー赤
		RecipeManager.addRecipe(new ItemStack(RTMBlock.marker, 16, 0), "GGGGG", "G D G", "G D G", "G D G", "GGGGG",
				'D', dyeRed,
				'G', Blocks.glass);
		//斜め
		RecipeManager.addRecipe(new ItemStack(RTMBlock.marker, 16, 4), "GGGGG", "G  DG", "G D G", "GD  G", "GGGGG",
				'D', dyeRed,
				'G', Blocks.glass);
		//マーカー青
		RecipeManager.addRecipe(new ItemStack(RTMBlock.markerSwitch, 16, 0), "GGGGG", "G D G", "G D G", "G D G", "GGGGG",
				'D', dyeBlue,
				'G', Blocks.glass);
		//斜め
		RecipeManager.addRecipe(new ItemStack(RTMBlock.markerSwitch, 16, 4), "GGGGG", "G  DG", "G D G", "GD  G", "GGGGG",
				'D', dyeBlue,
				'G', Blocks.glass);
		//マーカー黄
//        RecipeManager.addRecipe(new ItemStack(RTMBlock.markerSlope, 16, 0), new Object[]{
//                "GGGGG", "G D G", "G D G", "G D G", "GGGGG",
//                'D', dyeYellow,
//                'G', Blocks.glass});
//        RecipeManager.addRecipe(new ItemStack(RTMBlock.markerSlope, 16, 4), new Object[]{
//                "GGGGG", "G D G", "G D G", "G   G", "GGGGG",
//                'D', dyeYellow,
//                'G', Blocks.glass});
//        RecipeManager.addRecipe(new ItemStack(RTMBlock.markerSlope, 16, 8), new Object[]{
//                "GGGGG", "G D G", "G   G", "G   G", "GGGGG",
//                'D', dyeYellow,
//                'G', Blocks.glass});
//        RecipeManager.addRecipe(new ItemStack(RTMBlock.markerSlope, 16, 12), new Object[]{
//                "GGGGG", "G   G", "G D G", "G   G", "GGGGG",
//                'D', dyeYellow,
//                'G', Blocks.glass});

		//信号変換器(RS->S)
		RecipeManager.addRecipe(new ItemStack(RTMBlock.signalConverter, 1, 0), "SSSSS", "SWWWS", "SRIRS", "SWWWS", "SSSSS",
				'R', Items.redstone,
				'S', sheetSteel,
				'W', wire,
				'I', shaft);
		//(S->RS)
		RecipeManager.addRecipe(new ItemStack(RTMBlock.signalConverter, 1, 1), "SSSSS", "SWWWS", "SRIRS", "SWWWS", "SSSSS",
				'R', Blocks.redstone_torch,
				'S', sheetSteel,
				'W', wire,
				'I', shaft);
		//++
		RecipeManager.addRecipe(new ItemStack(RTMBlock.signalConverter, 1, 2), "SSSSS", "SWIWS", "SWIWS", "SWIWS", "SSSSS",
				'S', sheetSteel,
				'W', wire,
				'I', shaft);
		//--
		RecipeManager.addRecipe(new ItemStack(RTMBlock.signalConverter, 1, 3), "SSSSS", "SWWWS", "SIIIS", "SWWWS", "SSSSS",
				'S', sheetSteel,
				'W', wire,
				'I', shaft);

		//WorkBench
		RecipeManager.addRecipe(new ItemStack(RTMBlock.trainWorkBench, 1, 0), "III", "ICI", "III",
				'S', ingotSteel,
				'C', Blocks.crafting_table);
		RecipeManager.addRecipe(new ItemStack(RTMBlock.trainWorkBench, 1, 1), "ISI", "SCS", "ISI",
				'I', ingotSteel,
				'S', sheetSteel,
				'C', Blocks.crafting_table);
		//耐火レンガ
		RecipeManager.addRecipe(new ItemStack(RTMBlock.fireBrick, 6, 0), "NDN", "DCD", "NDN",
				'N', Items.netherbrick,
				'D', Blocks.dirt,
				'C', Items.clay_ball);
		//熱風炉用レンガ
		RecipeManager.addRecipe(new ItemStack(RTMBlock.hotStoveBrick, 6, 0), "NDN", "DCD", "NDN",
				'N', Items.netherbrick,
				'D', Blocks.soul_sand,
				'C', Items.clay_ball);
		//Slot
		RecipeManager.addRecipe(new ItemStack(RTMBlock.slot, 2, 0), "SS SS", "SI IS", "SM MS", "SI IS", "SS SS",
				'S', sheetSteel,
				'I', ingotSteel,
				'M', motor);
		//ハーフ耐火レンガ
		RecipeManager.addRecipe(new ItemStack(RTMBlock.brickSlab, 6, 0), "   ", "   ", "BBB",
				'B', RTMBlock.fireBrick);
		//鋼材
		RecipeManager.addRecipe(new ItemStack(RTMBlock.steelMaterial, 2, 0), "II", "II",
				'I', sheetSteel);

		//台車
		RecipeManager.addRecipe(new ItemStack(RTMItem.bogie, 1, 0), " W W ", "PPPPP", " S S ", "PPPPP", " W W ",
				'W', new ItemStack(RTMItem.material, 1, 1),
				'P', sheetSteel,
				'S', shaft);
		//蛍光灯(ガラス)
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 6, 0), "GGG", "ILI", "GGG",
				'I', Items.iron_ingot,
				'L', Items.glowstone_dust,
				'G', Blocks.glass_pane);
		//蛍光灯(ダイヤ)
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 6, 1), "GGG", "ILI", "GGG",
				'I', Items.iron_ingot,
				'L', Items.glowstone_dust,
				'G', Items.diamond);
		//蛍光灯(カバー)
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 6, 4), "GGG", "ILI", "GGG",
				'I', Items.iron_ingot,
				'L', Items.glowstone_dust,
				'G', Blocks.glass);
		//碍子
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 2, IstlObjType.INSULATOR.id), "   ", " R ", " I ",
				'R', Blocks.sandstone,
				'I', ingotSteel);
		//入力コネクタ
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 2, IstlObjType.CONNECTOR_IN.id), "   ", " RD", " I ",
				'R', Blocks.sandstone,
				'D', new ItemStack(Items.dye, 1, 4),
				'I', ingotSteel);
		//出力コネクタ
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 2, IstlObjType.CONNECTOR_OUT.id), "   ", " RD", " I ",
				'R', Blocks.sandstone,
				'D', dyeRed,
				'I', ingotSteel);

		//架線柱（石）
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemLinePole, 6, 0), "ISI", "ISI", " S ",
				'S', Blocks.stone,
				'I', ingotSteel);
		//架線柱（鉄）
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemLinePole, 6, 1), "ISI", "ISI", " S ",
				'S', RTMBlock.ironPillar,
				'I', ingotSteel);
		//架線柱（石2）
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemLinePole, 6, 2), "ISI", " S ", " S ",
				'S', Blocks.stone,
				'I', ingotSteel);
		//架線柱（鉄）
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemLinePole, 6, 3), "III", " I ", " I ",
				'I', ingotSteel);

		//踏切
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 1, 5), " YIY ", " RIR ", " BIB ", "  I  ", "  I  ",
				'R', Items.redstone,
				'Y', dyeYellow,
				'B', new ItemStack(Items.dye, 1, 0),
				'I', ingotSteel);
		//ATC
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 3, IstlObjType.ATC.id), "   ", "PPP", "IRI",
				'R', Blocks.redstone_torch,
				'P', Blocks.stone_pressure_plate,
				'I', ingotSteel);
		//検知器
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 3, IstlObjType.TRAIN_DETECTOR.id), "   ", "PPP", "IRI",
				'R', Items.redstone,
				'P', Blocks.stone_pressure_plate,
				'I', ingotSteel);
		//Turnstile
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 2, 12), "SFS", "SRS", "SRS",
				'S', sheetSteel,
				'F', ingotSteel,
				'R', Items.redstone);
		//BumpingPost
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 1, 13), "ISI", "I I", "I I",
				'S', sheetSteel,
				'I', shaft);
		//Point
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 1, IstlObjType.POINT.id), "     ", "  M  ", "IISI ", "  S  ", "IISI ",
				'S', sheetSteel,
				'I', shaft,
				'M', Blocks.lever);

		//信号
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemSignal, 2, 0), "IGI", "IYI", "IRI", "IYI", "IGI",
				'R', dyeRed,
				'Y', dyeYellow,
				'G', new ItemStack(Items.dye, 1, 2),
				'I', sheetSteel);
		//シャフト
		RecipeManager.addRecipe(new ItemStack(RTMItem.material, 3, 0), "I  ", "I  ", "I  ",
				'I', ingotSteel);
		//ホイール
		RecipeManager.addRecipe(new ItemStack(RTMItem.material, 4, 1), " III ", "IIIII", "II II", "IIIII", " III ",
				'I', ingotSteel);
		//ディーゼルエンジン
		RecipeManager.addRecipe(new ItemStack(RTMItem.material, 2, 2), "SSSSS", "SPPRS", "SFFAA", "SPPRS", "SSSSS",
				'R', Items.redstone,
				'P', Blocks.piston,
				'A', shaft,
				'S', sheetSteel,
				'F', Blocks.furnace);
		//モーター
		RecipeManager.addRecipe(new ItemStack(RTMItem.material, 2, 3), "SSSSS", "SWWRS", "SIIAA", "SWWRS", "SSSSS",
				'I', ingotSteel,
				'W', wire,
				'R', Items.redstone,
				'A', shaft,
				'S', sheetSteel);
		//電線
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemWire, 5, 0), "     ", "RRRRR", "IIIII", "RRRRR", "     ",
				'R', Items.redstone,
				'I', ingotSteel);
		//レール
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemLargeRail, 1, 5), "WIWIW", " I I ", "WIWIW", " I I ", "WIWIW",
				'I', ingotSteel,
				'W', Blocks.planks);
		//鋼板
		RecipeManager.addRecipe(new ItemStack(RTMItem.material, 5, 8), "     ", "     ", "     ", "     ", "IIIII",
				'I', ingotSteel);
		//バール
		RecipeManager.addRecipe(new ItemStack(RTMItem.crowbar, 1), " II  ", "  I  ", "  I  ", "  I  ", "  I  ",
				'I', ingotSteel);
		//レンチ
		RecipeManager.addRecipe(new ItemStack(RTMItem.wrench, 1), " I I ", " III ", "  I  ", "  I  ", "  I  ",
				'I', ingotSteel);
		//気動車
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemtrain, 1, 0), "FGFGF", "BE EB", "FGFGF",
				'F', Blocks.iron_block,
				'G', Blocks.glass,
				'B', new ItemStack(RTMItem.bogie, 1, 0),
				'E', new ItemStack(RTMItem.material, 1, 2));
		//電車
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemtrain, 1, 1), "FGFGF", "BE EB", "FGFGF",
				'F', Blocks.iron_block,
				'G', Blocks.glass,
				'B', new ItemStack(RTMItem.bogie, 1, 0),
				'E', motor);
		//貨車
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemtrain, 1, 2), "FGFGF", "BE EB", "FGFGF",
				'F', Blocks.iron_block,
				'G', Blocks.glass,
				'B', new ItemStack(RTMItem.bogie, 1, 0),
				'E', shaft);
		//Motorman
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemMotorman, 1, 0), "  W  ", "IIIII", "  I  ", " I I ", " I I ",
				'W', new ItemStack(Items.skull, 1, 1),
				'I', ingotSteel);
		//コンテナ
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemCargo, 1, 0), "SSSSS", "S   S", "S   S", "S   S", "IIIII",
				'S', sheetSteel,
				'I', ingotSteel);
		//砲
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemCargo, 1, 1), "SSSSS", "    S", "SSSSS", " III ", " III ",
				'S', sheetSteel,
				'I', ingotSteel);
		//鉄道標識
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemRailroadSign, 2, 0), "   ", " S ", " I ",
				'S', sheetSteel,
				'I', ingotSteel);
		//Ticket
		RecipeManager.addRecipe(new ItemStack(RTMItem.ticket, 9, 1), "   ", "OBO", "PPP",
				'O', new ItemStack(Items.dye, 1, 14),
				'B', new ItemStack(Items.dye, 1, 0),
				'P', Items.paper);
		//金鋸
		RecipeManager.addRecipe(new ItemStack(RTMItem.iron_hacksaw), "   ", "IIS", "II ",
				'S', Items.stick,
				'I', Items.iron_ingot);
		//パドル
		RecipeManager.addRecipe(new ItemStack(RTMItem.paddle), " II", " II", "I  ",
				'I', Items.iron_ingot);
		//ふいご
		RecipeManager.addRecipe(new ItemStack(RTMItem.bellows), "SL ", " LG", "SL ",
				'S', Items.stick,
				'L', Items.leather,
				'G', Items.gold_ingot);
		//パイプ
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemPipe, 4, 0), "SSSSS", "S   S", "S   S", "S   S", "SSSSS",
				'S', sheetSteel);
		RecipeManager.addRecipe(new ItemStack(RTMItem.itemPipe, 4, 1), "SSSSS", "S   S", "S S S", "S   S", "SSSSS",
				'S', sheetSteel);
		/*RTMRecipe.addRecipe(new ItemStack(RTMItem.itemPipe, 4, 1),new Object[]{
			"SSSS ", "S  S ", "S  S ", "SSSS ", "     ",
			'S', sheetSteel});
		RTMRecipe.addRecipe(new ItemStack(RTMItem.itemPipe, 4, 2),new Object[]{
			"SSS  ", "S S  ", "SSS  ", "     ", "     ",
			'S', sheetSteel});
		RTMRecipe.addRecipe(new ItemStack(RTMItem.itemPipe, 4, 3),new Object[]{
			" S   ", "S S  ", " S   ", "     ", "     ",
			'S', sheetSteel});*/
		//看板
		RecipeManager.addRecipe(new ItemStack(RTMItem.installedObject, 2, 17), "     ", "SSSSS", "SFFFS", "SFFFS", "SSSSS",
				'F', new ItemStack(RTMItem.installedObject, 1, 0),
				'S', sheetSteel);
		//鏡
		RecipeManager.addRecipe(new ItemStack(RTMItem.mirror, 9, 0), "GGGGG", "GSSSG", "GSSSG", "GSSSG", "GGGGG",
				'G', new ItemStack(Blocks.glass_pane, 1, 0),
				'S', sheetSteel);
		for (int i = 1; i < 16; i += 2) {
			int meta = 20 + i;
			String s0 = i == 1 ? " M   " : (i == 3 ? " MM  " : " MMM ");
			String s1 = i <= 5 ? "  G  " : (i <= 13 ? "  GM " : " MGM ");
			String s2 = i <= 7 ? "     " : (i == 9 ? "   M " : (i == 11 ? "  MM " : " MMM "));
			RecipeManager.addRecipe(new ItemStack(RTMItem.mirror, 1, meta), "     ", s0, s1, s2, "     ",
					'M', new ItemStack(RTMItem.mirror, 1, 0),
					'G', new ItemStack(Blocks.glass, 1, 0));
		}
		//移動装置
		RecipeManager.addRecipe(new ItemStack(RTMBlock.movingMachine, 1, 0), "SSS", "SMS", "SSS",
				'S', RTMBlock.steelMaterial,
				'M', motor);

		//弾
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 3, 1), "  S  ", " STS ", "SSTSS", "SSTSS", "SSSSS",
				'S', sheetSteel,
				'T', Blocks.tnt);
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 64, 5), " S   ", "SSS  ", "SSS  ", "     ", "     ",
				'S', sheetSteel);
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 64, 13), " S   ", "SSS  ", "SSS  ", "SSS  ", "     ",
				'S', sheetSteel);
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 64, 17), " S   ", "SSS  ", "SSS  ", "SSS  ", "SSS  ",
				'S', sheetSteel);

		//薬莢
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 5, 2), "S   S", "S   S", "S   S", "S   S", "SSISS",
				'S', sheetSteel,
				'I', ingotSteel);
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 64, 6), "S S  ", "S S  ", "SSS  ", "     ", "     ",
				'S', sheetSteel);
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 64, 14), "S S  ", "S S  ", "S S  ", "SSS  ", "     ",
				'S', sheetSteel);
		RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 64, 18), "S S  ", "S S  ", "S S  ", "S S  ", "SSS  ",
				'S', sheetSteel);

		//弾薬
		for (int i = 0; i < ItemAmmunition.num; ++i) {
			if (i == 2) {
				continue;
			}
			int i0 = i * 4;
			if (i == 0) {
				RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 1, i0), " B   ", " T   ", " C   ", "     ", "     ",
						'B', new ItemStack(RTMItem.bullet, 1, i0 + 1),
						'C', new ItemStack(RTMItem.bullet, 1, i0 + 2),
						'T', Blocks.tnt);
			} else if (i == 3) {
				RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 1, i0), " B   ", " PP  ", " C   ", "     ", "     ",
						'B', new ItemStack(RTMItem.bullet, 1, i0 + 1),
						'C', new ItemStack(RTMItem.bullet, 1, i0 + 2),
						'P', gunpowder);
			} else if (i == 4) {
				RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 1, i0), " B   ", "PPP  ", " C   ", "     ", "     ",
						'B', new ItemStack(RTMItem.bullet, 1, i0 + 1),
						'C', new ItemStack(RTMItem.bullet, 1, i0 + 2),
						'P', gunpowder);
			} else if (i == 5) {
			} else {
				RecipeManager.addRecipe(new ItemStack(RTMItem.bullet, 1, i0), " B   ", " P   ", " C   ", "     ", "     ",
						'B', new ItemStack(RTMItem.bullet, 1, i0 + 1),
						'C', new ItemStack(RTMItem.bullet, 1, i0 + 2),
						'P', gunpowder);
			}
		}

		//弾倉
		RecipeManager.addRecipe(new ItemStack(RTMItem.magazine_handgun, 3, GunType.handgun.maxSize), "S S  ", "S S  ", "SIS  ", "     ", "     ",
				'S', sheetSteel,
				'I', ingotSteel);
		RecipeManager.addRecipe(new ItemStack(RTMItem.magazine_rifle, 3, GunType.rifle.maxSize), "  S  ", "  S  ", "  S  ", "     ", "     ",
				'S', sheetSteel,
				'I', ingotSteel);
		RecipeManager.addRecipe(new ItemStack(RTMItem.magazine_alr, 3, GunType.autoloading_rifle.maxSize), " S  S", " S  S", " S  S", " SIIS", "     ",
				'S', sheetSteel,
				'I', ingotSteel);
		RecipeManager.addRecipe(new ItemStack(RTMItem.magazine_sr, 3, GunType.sniper_rifle.maxSize), " S  S", " S  S", " SIIS", "     ", "     ",
				'S', sheetSteel,
				'I', ingotSteel);
		RecipeManager.addRecipe(new ItemStack(RTMItem.magazine_smg, 3, GunType.smg.maxSize), " S S ", " S S ", " S S ", " SIS ", "     ",
				'S', sheetSteel,
				'I', ingotSteel);
		RecipeManager.addRecipe(new ItemStack(RTMItem.magazine_amr, 3, GunType.amr.maxSize), "S   S", "S   S", "SIIIS", "     ", "     ",
				'S', sheetSteel,
				'I', ingotSteel);

		//銃
		RecipeManager.addRecipe(new ItemStack(RTMItem.handgun, 1, 0), "SSS  ", "  M  ", "     ", "     ", "     ",
				'S', sheetSteel,
				'M', new ItemStack(RTMItem.magazine_handgun, 1, 0));
		RecipeManager.addRecipe(new ItemStack(RTMItem.rifle, 1, 0), "SSSM ", "   WW", "     ", "     ", "     ",
				'S', sheetSteel,
				'W', Blocks.planks,
				'M', new ItemStack(RTMItem.magazine_rifle, 1, 0));
		RecipeManager.addRecipe(new ItemStack(RTMItem.autoloading_rifle, 1, 0), "SSSS ", " M II", "     ", "     ", "     ",
				'S', sheetSteel,
				'I', ingotSteel,
				'M', new ItemStack(RTMItem.magazine_alr, 1, 0));
		RecipeManager.addRecipe(new ItemStack(RTMItem.sniper_rifle, 1, 0), " SS  ", "SSSS ", " M II", "     ", "     ",
				'S', sheetSteel,
				'I', ingotSteel,
				'M', new ItemStack(RTMItem.magazine_sr, 1, 0));
		RecipeManager.addRecipe(new ItemStack(RTMItem.smg, 1, 0), "SSSS ", " M II", "     ", "     ", "     ",
				'S', sheetSteel,
				'I', ingotSteel,
				'M', new ItemStack(RTMItem.magazine_smg, 1, 0));
		RecipeManager.addRecipe(new ItemStack(RTMItem.amr, 1, 0), " SS  ", "SSSS ", " M II", "     ", "     ",
				'S', sheetSteel,
				'I', ingotSteel,
				'M', new ItemStack(RTMItem.magazine_amr, 1, 0));

		RecipeManager.INSTANCE.addRecipeToManager(new RepairRecipe(RTMItem.magazine_handgun, new ItemStack(RTMItem.bullet, 1, 4)));
		RecipeManager.INSTANCE.addRecipeToManager(new RepairRecipe(RTMItem.magazine_rifle, new ItemStack(RTMItem.bullet, 1, 12)));
		RecipeManager.INSTANCE.addRecipeToManager(new RepairRecipe(RTMItem.magazine_alr, new ItemStack(RTMItem.bullet, 1, 12)));
		RecipeManager.INSTANCE.addRecipeToManager(new RepairRecipe(RTMItem.magazine_sr, new ItemStack(RTMItem.bullet, 1, 12)));
		RecipeManager.INSTANCE.addRecipeToManager(new RepairRecipe(RTMItem.magazine_smg, new ItemStack(RTMItem.bullet, 1, 4)));
		RecipeManager.INSTANCE.addRecipeToManager(new RepairRecipe(RTMItem.magazine_amr, new ItemStack(RTMItem.bullet, 1, 16)));

		//火薬
		GameRegistry.addShapelessRecipe(new ItemStack(RTMItem.material, 64, 4), Items.redstone, Items.gunpowder, Items.gunpowder, Items.gunpowder);

		GameRegistry.addSmelting(new ItemStack(Items.coal, 1, 0), new ItemStack(RTMItem.coke), 0.25F);
	}
}