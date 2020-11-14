package jp.ngt.rtm;

import cpw.mods.fml.common.registry.GameRegistry;
import jp.ngt.ngtlib.item.ItemMultiIcon;
import jp.ngt.ngtlib.item.SerializableItemType;
import jp.ngt.rtm.item.*;
import jp.ngt.rtm.item.ItemGun.GunType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class RTMItem {
	public static Item bogie;
	public static Item installedObject;
	public static Item material;
	public static Item crowbar;
	public static Item itemtrain;
	public static Item itemMotorman;
	public static Item itemCargo;
	public static Item bucketLiquid;
	public static Item itemRailroadSign;
	public static Item ticket;
	public static Item ticketBook;
	public static Item icCard;
	public static Item itemLargeRail;
	public static Item itemSignal;
	public static Item itemLinePole;
	public static Item money;
	public static Item wrench;
	public static Item mirror;
	public static Item itemVehicle;
	public static Item itemWire;

	public static Item handgun;
	public static Item rifle;
	public static Item autoloading_rifle;//自動小銃
	public static Item sniper_rifle;
	public static Item smg;//短機関銃
	public static Item amr;

	public static Item magazine_handgun;
	public static Item magazine_rifle;
	public static Item magazine_alr;//自動小銃
	public static Item magazine_sr;
	public static Item magazine_smg;//短機関銃
	public static Item magazine_amr;
	public static Item bullet;
	public static Item nvd;//Night vision device

	public static Item iron_hacksaw;//金鋸
	public static Item steel_ingot;
	public static Item paddle;
	public static Item coke;//コークス
	public static Item bellows;
	public static Item itemPipe;
	public static Item paintTool;

	public static Item camera;

	public static final byte RAIL_ICON = 6;
	public static final byte RAIL_SHAPE = 8;

	public static void init() {
		bogie = (new ItemBogie()).setUnlocalizedName("rtm:bogie").setTextureName("rtm:bogieD").setCreativeTab(CreativeTabRTM.tabRailway);
		installedObject = (new ItemInstalledObject()).setUnlocalizedName("rtm:installedObject").setTextureName("rtm:installedObject").setCreativeTab(CreativeTabRTM.tabRailway);
		material = (new ItemMultiIcon(getItemMaterial())).setUnlocalizedName("rtm:material").setTextureName("rtm:material").setCreativeTab(CreativeTabRTM.tabRailway);
		crowbar = (new ItemCrowbar()).setUnlocalizedName("rtm:crowbar").setTextureName("rtm:crowbar").setCreativeTab(CreativeTabRTM.tabRTMTools);
		itemtrain = (new ItemTrain()).setUnlocalizedName("rtm:itemTrain").setCreativeTab(CreativeTabRTM.tabRailway);
		itemMotorman = (new ItemNPC()).setUnlocalizedName("rtm:itemMotorman").setTextureName("rtm:itemMotorman").setCreativeTab(CreativeTabRTM.tabRailway);
		itemCargo = (new ItemCargo()).setUnlocalizedName("rtm:itemCargo").setTextureName("rtm:itemContainer").setCreativeTab(CreativeTabRTM.tabRailway);
		itemRailroadSign = (new ItemRailroadSign()).setUnlocalizedName("rtm:itemRailroadSign").setTextureName("rtm:itemRailroadSign").setCreativeTab(CreativeTabRTM.tabRailway);
		ticket = (new ItemTicket(0)).setUnlocalizedName("rtm:ticket").setTextureName("rtm:ticket").setCreativeTab(CreativeTabRTM.tabRailway);
		ticketBook = (new ItemTicket(1)).setUnlocalizedName("rtm:ticketBook").setTextureName("rtm:ticket").setCreativeTab(CreativeTabRTM.tabRailway);
		icCard = (new ItemTicket(2)).setUnlocalizedName("rtm:icCard").setTextureName("rtm:icCard").setCreativeTab(CreativeTabRTM.tabRailway);
		itemLargeRail = (new ItemRail()).setUnlocalizedName("rtm:itemLargeRail").setTextureName("rtm:largeRail_w").setCreativeTab(CreativeTabRTM.tabRailway);
		itemSignal = (new ItemSignal()).setUnlocalizedName("rtm:itemSignal").setTextureName("rtm:signal").setCreativeTab(CreativeTabRTM.tabRailway);
		itemLinePole = (new ItemLinePole()).setUnlocalizedName("rtm:itemLinePole").setTextureName("rtm:itemLinePole").setCreativeTab(CreativeTabRTM.tabRailway);
		money = (new ItemMultiIcon(getItemMoney())).setUnlocalizedName("rtm:money").setTextureName("rtm:money_0").setCreativeTab(CreativeTabRTM.tabRTMTools);
		wrench = (new ItemWrench()).setUnlocalizedName("rtm:wrench").setTextureName("rtm:wrench").setCreativeTab(CreativeTabRTM.tabRTMTools);
		mirror = (new ItemMirror(getItemMirror())).setUnlocalizedName("rtm:mirror").setTextureName("rtm:mirror").setCreativeTab(CreativeTabRTM.tabIndustry);
		itemVehicle = (new ItemVehicle()).setUnlocalizedName("rtm:vehicle").setTextureName("rtm:vehicle").setCreativeTab(CreativeTabRTM.tabRailway);
		itemWire = (new ItemWire()).setUnlocalizedName("rtm:wire").setTextureName("rtm:wire").setCreativeTab(CreativeTabRTM.tabRailway);

		handgun = (new ItemGun(GunType.handgun)).setUnlocalizedName("rtm:handgun").setTextureName("rtm:gun").setCreativeTab(CreativeTabRTM.tabRTMTools);
		rifle = (new ItemGun(GunType.rifle)).setUnlocalizedName("rtm:rifle").setTextureName("rtm:rifle").setCreativeTab(CreativeTabRTM.tabRTMTools);
		autoloading_rifle = (new ItemGun(GunType.autoloading_rifle)).setUnlocalizedName("rtm:autoloading_rifle").setTextureName("rtm:autoloading_rifle").setCreativeTab(CreativeTabRTM.tabRTMTools);
		sniper_rifle = (new ItemGun(GunType.sniper_rifle)).setUnlocalizedName("rtm:sniper_rifle").setTextureName("rtm:sniper_rifle").setCreativeTab(CreativeTabRTM.tabRTMTools);
		smg = (new ItemGun(GunType.smg)).setUnlocalizedName("rtm:smg").setTextureName("rtm:smg").setCreativeTab(CreativeTabRTM.tabRTMTools);
		amr = (new ItemGun(GunType.amr)).setUnlocalizedName("rtm:amr").setTextureName("rtm:sniper_rifle").setCreativeTab(CreativeTabRTM.tabRTMTools);

		magazine_handgun = (new ItemMagazine(GunType.handgun)).setUnlocalizedName("rtm:magazine_handgun").setTextureName("rtm:magazine_handgun").setCreativeTab(CreativeTabRTM.tabRTMTools);
		magazine_rifle = (new ItemMagazine(GunType.rifle)).setUnlocalizedName("rtm:magazine_rifle").setTextureName("rtm:magazine_rifle").setCreativeTab(CreativeTabRTM.tabRTMTools);
		magazine_alr = (new ItemMagazine(GunType.autoloading_rifle)).setUnlocalizedName("rtm:magazine_alr").setTextureName("rtm:magazine_alr").setCreativeTab(CreativeTabRTM.tabRTMTools);
		magazine_sr = (new ItemMagazine(GunType.sniper_rifle)).setUnlocalizedName("rtm:magazine_sr").setTextureName("rtm:magazine_amr").setCreativeTab(CreativeTabRTM.tabRTMTools);
		magazine_smg = (new ItemMagazine(GunType.smg)).setUnlocalizedName("rtm:magazine_smg").setTextureName("rtm:magazine_alr").setCreativeTab(CreativeTabRTM.tabRTMTools);
		magazine_amr = (new ItemMagazine(GunType.amr)).setUnlocalizedName("rtm:magazine_amr").setTextureName("rtm:magazine_amr").setCreativeTab(CreativeTabRTM.tabRTMTools);
		bullet = (new ItemAmmunition()).setUnlocalizedName("rtm:bullet").setTextureName("rtm:bullet_handgun").setCreativeTab(CreativeTabRTM.tabRTMTools);
		nvd = (new ItemNVD()).setUnlocalizedName("rtm:nvd").setTextureName("rtm:nvd").setCreativeTab(CreativeTabRTM.tabRTMTools);

		bucketLiquid = (new ItemBucketLiquid()).setUnlocalizedName("rtm:bucketLiquid").setTextureName("bucket_lava").setCreativeTab(CreativeTabRTM.tabIndustry);
		iron_hacksaw = (new ItemHacksaw()).setUnlocalizedName("rtm:ironHacksaw").setTextureName("rtm:ironHacksaw").setCreativeTab(CreativeTabRTM.tabRTMTools);
		steel_ingot = (new Item()).setUnlocalizedName("rtm:ingotSteel").setTextureName("rtm:ingotSteel").setCreativeTab(CreativeTabRTM.tabIndustry);
		paddle = (new ItemPaddle()).setUnlocalizedName("rtm:paddle").setTextureName("rtm:paddle").setCreativeTab(CreativeTabRTM.tabRTMTools);
		coke = (new Item()).setUnlocalizedName("rtm:coke").setTextureName("rtm:coke").setCreativeTab(CreativeTabRTM.tabIndustry);
		bellows = (new ItemBellows()).setUnlocalizedName("rtm:bellows").setTextureName("rtm:bellows").setCreativeTab(CreativeTabRTM.tabRTMTools);
		itemPipe = (new ItemPipe()).setUnlocalizedName("rtm:itemPipe").setTextureName("rtm:itemPipe").setCreativeTab(CreativeTabRTM.tabIndustry);
		paintTool = (new ItemPaintTool()).setUnlocalizedName("rtm:paintTool").setTextureName("rtm:paintTool").setCreativeTab(CreativeTabRTM.tabRTMTools);

		camera = (new ItemCamera()).setUnlocalizedName("rtm:camera").setTextureName("rtm:camera").setCreativeTab(CreativeTabRTM.tabRTMTools);

		GameRegistry.registerItem(bogie, "rtm:bogie");
		GameRegistry.registerItem(installedObject, "rtm:installedObject");
		GameRegistry.registerItem(material, "rtm:material");
		GameRegistry.registerItem(crowbar, "rtm:crowbar");
		GameRegistry.registerItem(itemtrain, "rtm:itemTrain");
		GameRegistry.registerItem(itemMotorman, "rtm:itemMotorman");
		GameRegistry.registerItem(itemCargo, "rtm:itemCargo");
		GameRegistry.registerItem(itemRailroadSign, "rtm:itemRailroadSign");
		GameRegistry.registerItem(ticket, "rtm:ticket");
		GameRegistry.registerItem(ticketBook, "rtm:ticketBook");
		GameRegistry.registerItem(icCard, "rtm:icCard");
		GameRegistry.registerItem(itemLargeRail, "itemLargeRail");
		GameRegistry.registerItem(itemSignal, "itemSignal");
		GameRegistry.registerItem(itemLinePole, "itemLinePole");
		GameRegistry.registerItem(money, "money");
		GameRegistry.registerItem(wrench, "wrench");
		GameRegistry.registerItem(mirror, "itemMirror");
		GameRegistry.registerItem(itemVehicle, "itemVehicle");
		GameRegistry.registerItem(itemWire, "itemWire");

		GameRegistry.registerItem(handgun, "handgun");
		GameRegistry.registerItem(rifle, "rifle");
		GameRegistry.registerItem(autoloading_rifle, "autoloading_rifle");
		GameRegistry.registerItem(sniper_rifle, "sniper_rifle");
		GameRegistry.registerItem(smg, "smg");
		GameRegistry.registerItem(amr, "amr");

		GameRegistry.registerItem(magazine_handgun, "magazine_handgun");
		GameRegistry.registerItem(magazine_rifle, "magazine_rifle");
		GameRegistry.registerItem(magazine_alr, "magazine_alr");
		GameRegistry.registerItem(magazine_sr, "magazine_sr");
		GameRegistry.registerItem(magazine_smg, "magazine_smg");
		GameRegistry.registerItem(magazine_amr, "magazine_amr");
		GameRegistry.registerItem(bullet, "bullet");
		GameRegistry.registerItem(nvd, "nvd");

		GameRegistry.registerItem(bucketLiquid, "rtm:bucketLiquid");
		GameRegistry.registerItem(iron_hacksaw, "rtm:ironHacksaw");
		GameRegistry.registerItem(steel_ingot, "rtm:ingotSteel");
		GameRegistry.registerItem(paddle, "rtm:paddle");
		GameRegistry.registerItem(coke, "rtm:coke");
		GameRegistry.registerItem(bellows, "rtm:bellows");
		GameRegistry.registerItem(itemPipe, "rtm:itemPipe");
		GameRegistry.registerItem(paintTool, "paint_tool");

		GameRegistry.registerItem(camera, "rtm:camera");

		OreDictionary.registerOre("ingotSteel", new ItemStack(steel_ingot, 1, 0));
		OreDictionary.registerOre("fuelCoke", new ItemStack(coke, 1, 0));
	}

	private static Map<Integer, String> getItemMaterial() {
		Map<Integer, String> map = new HashMap<>();
		map.put(0, "rtm:shaft");
		map.put(1, "rtm:wheel");
		map.put(2, "rtm:DE");
		map.put(3, "rtm:motor");
		map.put(4, "rtm:powder");
		//map.put(5, "rtm:itemWire_0");//普通の電線
		//map.put(6, "rtm:itemWire_1");//架線
		//7
		map.put(8, "rtm:sheetSteel");
		//9
		return map;
	}

	private static Map<Integer, String> getItemMoney() {
        return Arrays.stream(MoneyType.values()).collect(Collectors.toMap(MoneyType::getId, type -> "rtm:money_" + type.getId(), (a, b) -> b));
    }

	private static Map<Integer, String> getItemMirror() {
		Map<Integer, String> map = new HashMap<>();
		map.put(0, "rtm:itemMirror");
		for (int i = 1; i < 16; i += 2) {
			map.put(20 + i, "rtm:itemMirrorBlock");
		}
		return map;
	}

	public enum MoneyType implements SerializableItemType {
		Y1(0, 1),
		Y5(1, 5),
		Y10(2, 10),
		Y50(3, 50),
		Y100(4, 100),
		Y500(5, 500),
		Y1000(6, 1000),
		Y5000(7, 5000),
		Y10000(8, 10000);

		private final byte id;
		public final short price;

		MoneyType(int par1, int par2) {
			this.id = (byte) par1;
			this.price = (short) par2;
		}

		@Override
		public int getId() {
			return this.id;
		}

		public static int getPrice(int id) {
            return Arrays.stream(MoneyType.values()).filter(type -> type.id == id).findFirst().map(type -> type.price).orElse((short) 0);
        }
	}
}