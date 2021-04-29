package jp.ngt.rtm.craft;

import jp.ngt.ngtlib.item.ItemUtil;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;

/**
 * 鉱石辞書対応
 */
public class ShapedRecipes55 implements IRecipe {
    public final int recipeWidth;
    public final int recipeHeight;
    public final ItemStack[] recipeItems;
    private final ItemStack recipeOutput;
    private boolean hasNBT;

    public ShapedRecipes55(int w, int h, ItemStack[] stacks, ItemStack output) {
        this.recipeWidth = w;
        this.recipeHeight = h;
        this.recipeItems = stacks;
        this.recipeOutput = output;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return this.recipeOutput;
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(InventoryCrafting inventory, World world) {
        for (int i = 0; i <= 5 - this.recipeWidth; ++i) {
            for (int j = 0; j <= 5 - this.recipeHeight; ++j) {
                if (this.checkMatch(inventory, i, j, true)) {
                    return true;
                }

                if (this.checkMatch(inventory, i, j, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     */
    private boolean checkMatch(InventoryCrafting inventory, int width, int height, boolean par4) {
        for (int k = 0; k < 5; ++k) {
            for (int l = 0; l < 5; ++l) {
                int i1 = k - width;
                int j1 = l - height;
                ItemStack itemstack = null;

                if (i1 >= 0 && j1 >= 0 && i1 < this.recipeWidth && j1 < this.recipeHeight) {
                    if (par4) {
                        itemstack = this.recipeItems[this.recipeWidth - i1 - 1 + j1 * this.recipeWidth];
                    } else {
                        itemstack = this.recipeItems[i1 + j1 * this.recipeWidth];
                    }
                }

                ItemStack itemstack1 = inventory.getStackInRowAndColumn(k, l);

                if (itemstack1 != null || itemstack != null) {
                    if (!this.itemMatches(itemstack, itemstack1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    public ItemStack getCraftingResult(InventoryCrafting par1) {
        ItemStack itemstack = this.getRecipeOutput().copy();

        if (this.hasNBT) {
            for (int i = 0; i < par1.getSizeInventory(); ++i) {
                ItemStack itemstack1 = par1.getStackInSlot(i);

                if (itemstack1 != null && itemstack1.hasTagCompound()) {
                    itemstack.setTagCompound((NBTTagCompound) itemstack1.stackTagCompound.copy());
                }
            }
        }

        return itemstack;
    }

    /**
     * Returns the size of the recipe area
     */
    @Override
    public int getRecipeSize() {
        return this.recipeWidth * this.recipeHeight;
    }

    /**
     * 専用作業台のレシピ取得用
     */
    public ItemStack[] getRecipeItems() {
        ItemStack[] items = new ItemStack[25];

        for (int i = 0; i < 5; ++i)//h
        {
            if (i >= this.recipeHeight) {
                break;
            }

            for (int j = 0; j < 5; ++j)//w
            {
                if (j >= this.recipeWidth) {
                    break;
                }

                items[i * 5 + j] = this.recipeItems[i * this.recipeWidth + j];
            }
        }
        return items;
    }

    private boolean itemMatches(ItemStack target, ItemStack inInventory) {
        if (inInventory == null && target != null || inInventory != null && target == null) {
            return false;
        }

        if (target.getItem() == Items.dye)//鉱石辞書で染料がダメ値抜きで登録されてる
        {
            return ItemUtil.isItemEqual(target, inInventory);
        }

        int[] ids0 = OreDictionary.getOreIDs(target);
        int[] ids1 = OreDictionary.getOreIDs(inInventory);
        if (ids0.length > 0 && ids1.length > 0) {
            return Arrays.stream(ids0).anyMatch(j -> Arrays.stream(ids1).anyMatch(k -> j == k));
        } else {
            return ItemUtil.isItemEqual(target, inInventory);
        }
    }
}