package jp.ngt.rtm.craft;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RepairRecipe implements IRecipe {
    private final Item toolItem;
    private final ItemStack materialItem;

    public RepairRecipe(Item par1, ItemStack par2) {
        this.toolItem = par1;
        this.materialItem = par2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(this.toolItem, 1, 1);
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(InventoryCrafting inventory, World world) {
        ItemStack[] stacks = this.getToolAndMaterial(inventory);
        return stacks[0] != null && stacks[1] != null;
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    public ItemStack getCraftingResult(InventoryCrafting par1) {
        ItemStack[] stacks = this.getToolAndMaterial(par1);
        if (stacks[0] != null && stacks[1] != null) {
            ItemStack tool = stacks[0].copy();
            tool.setItemDamage(tool.getItemDamage() - 1);
            return tool;
        }
        return null;
    }

    /**
     * {tool, material}
     */
    private ItemStack[] getToolAndMaterial(InventoryCrafting par1) {
        ItemStack[] stacks = new ItemStack[2];
        for (int i = 0; i < par1.getSizeInventory(); ++i) {
            ItemStack stack = par1.getStackInSlot(i);
            if (stack != null) {
                if (stack.getItem() == this.toolItem && stack.getItemDamage() > 0) {
                    stacks[0] = stack;
                    break;
                }
            }
        }

        for (int i = 0; i < par1.getSizeInventory(); ++i) {
            ItemStack stack = par1.getStackInSlot(i);
            if (stack != null) {
                if (stack.getItem() == this.materialItem.getItem() && stack.getItemDamage() == this.materialItem.getItemDamage()) {
                    stacks[1] = stack;
                    break;
                }
            }
        }

        return stacks;
    }

    /**
     * Returns the size of the recipe area
     */
    @Override
    public int getRecipeSize() {
        return 2;
    }

    public ItemStack[] getToolAndMaterial(int par1) {
        ItemStack[] stacks = new ItemStack[par1];
        stacks[0] = new ItemStack(this.toolItem, 1, 2);
        stacks[1] = this.materialItem.copy();
        return stacks;
    }
}