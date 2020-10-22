package jp.ngt.rtm.entity.npc;

import jp.ngt.ngtlib.io.FileType;
import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.io.NGTText;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Menu {
	private final List<MenuEntry> menuList = new ArrayList<>();

	public Menu(String s) {
		this.init(s);
	}

	/**
	 * 重複時は上書き
	 */
	public boolean add(MenuEntry entry) {
		if (entry.item != null) {
			int index = this.menuList.indexOf(entry);
			if (index >= 0) {
				NGTLog.debug("[Menu] 重複アイテムを削除 : %s", entry.item.getDisplayName());
				this.menuList.remove(index);
			}
			return this.menuList.add(entry);
		}
		return false;
	}

	public void remove(int index) {
		this.menuList.remove(index);
	}

	public MenuEntry get(int index) {
		return this.menuList.get(index);
	}

	public List<MenuEntry> getList() {
		return this.menuList;
	}

	public boolean init(String s) {
		this.menuList.clear();

		if (s != null && !s.isEmpty()) {
			try {
				NBTTagCompound nbt0 = (NBTTagCompound) JsonToNBT.func_150315_a(s);
				NBTTagList nbttaglist = nbt0.getTagList("list", 10);
				for (int i = 0; i < nbttaglist.tagCount(); ++i) {
					NBTTagCompound nbt = nbttaglist.getCompoundTagAt(i);
					MenuEntry entry = MenuEntry.readFromNBT(nbt);
					if (entry != null) {
						this.add(entry);
					}
				}

				if (!this.menuList.isEmpty()) {
					return true;
				}
			} catch (NBTException e) {
				e.printStackTrace();
			}
		}

		this.add(new MenuEntry(new ItemStack(Items.cookie, 10), 200));
		this.add(new MenuEntry(new ItemStack(Items.cooked_fished, 5), 500));
		return false;
	}

	@Override
	public String toString() {
		NBTTagList nbttaglist = new NBTTagList();
		for (MenuEntry entry : this.menuList) {
			nbttaglist.appendTag(entry.writeToNBT());
		}
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("list", nbttaglist);
		return nbt.toString();
	}

	public boolean exportToText() {
		File file = NGTFileLoader.saveFile(FileType.JSON);
		if (file != null) {
			return NGTText.writeToText(file, this.toString());
		}
		return false;
	}

	public boolean importFromText() {
		File file = NGTFileLoader.selectFile(FileType.JSON);
		if (file != null) {
			try {
				return this.init(NGTText.readText(file, false, "UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
