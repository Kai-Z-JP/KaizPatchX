package jp.ngt.rtm.craft;

import jp.ngt.ngtlib.item.ItemUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class RecipeManager {
    public static final RecipeManager INSTANCE = new RecipeManager();

    private final List<IRecipe> recipesList = new ArrayList<>();

    private RecipeManager() {
    }

    /**
     * ShapedRecipes55として追加(ShapedOreRecipeの機能内包済み)
     */
    public static ShapedRecipes55 addRecipe(ItemStack output, Object... objs) {
        StringBuilder s = new StringBuilder();
        int i = 0;
        int j = 0;
        int k = 0;

        if (objs[i] instanceof String[]) {
            String[] astring = (String[]) objs[i++];

            for (String s1 : astring) {
                ++k;
                j = s1.length();
                s.append(s1);
            }
        } else {
            while (objs[i] instanceof String) {
                String s2 = (String) objs[i++];
                ++k;
                j = s2.length();
                s.append(s2);
            }
        }

        HashMap<Character, ItemStack> hashmap = new HashMap<>();
        for (; i < objs.length; i += 2) {
            Character character = (Character) objs[i];
            ItemStack itemstack1 = null;

            if (objs[i + 1] instanceof Item) {
                itemstack1 = new ItemStack((Item) objs[i + 1]);
            } else if (objs[i + 1] instanceof Block) {
                itemstack1 = new ItemStack((Block) objs[i + 1], 1, 32767);
            } else if (objs[i + 1] instanceof ItemStack) {
                itemstack1 = (ItemStack) objs[i + 1];
            }

            hashmap.put(character, itemstack1);
        }

        ItemStack[] aitemstack = new ItemStack[j * k];

        for (int i1 = 0; i1 < j * k; ++i1) {
            char c0 = s.charAt(i1);

            if (hashmap.containsKey(c0)) {
                aitemstack[i1] = hashmap.get(c0).copy();
            } else {
                aitemstack[i1] = null;
            }
        }

        ShapedRecipes55 recipe = new ShapedRecipes55(j, k, aitemstack, output);
        RecipeManager.INSTANCE.addRecipeToManager(recipe);
        return recipe;
    }

    /**
     * RTM専用作業台での閲覧を可能にする<br>
     * バニラのレシピも登録可
     */
    public void addRecipeToManager(IRecipe recipe) {
        CraftingManager.getInstance().getRecipeList().add(recipe);
        this.recipesList.add(recipe);
    }

    public List<IRecipe> getRecipeList() {
        return this.recipesList;
    }

    public IRecipe getRecipe(ItemStack par1) {
        List list = CraftingManager.getInstance().getRecipeList();

        for (Object o : list) {
            IRecipe recipe = (IRecipe) o;
            ItemStack output = recipe.getRecipeOutput();
            if (output != null && ItemUtil.isItemEqual(output, par1)) {
                return recipe;
            }
        }

        return null;
    }

    public ItemStack[] getRecipeItems(IRecipe par1) {
        ItemStack[] items = new ItemStack[25];

        if (par1 instanceof ShapedRecipes55) {
            ShapedRecipes55 recipe = (ShapedRecipes55) par1;
            return recipe.getRecipeItems();
        } else if (par1 instanceof RepairRecipe) {
            RepairRecipe recipe = (RepairRecipe) par1;
            return recipe.getToolAndMaterial(25);
        } else if (par1 instanceof ShapedRecipes) {
            ShapedRecipes recipe = (ShapedRecipes) par1;
            for (int i = 0; i < 5; ++i)//h
            {
                if (i < recipe.recipeHeight) {
                    for (int j = 0; j < 5; ++j)//w
                    {
                        if (j < recipe.recipeWidth) {
                            items[i * 5 + j] = recipe.recipeItems[i * recipe.recipeWidth + j];
                        }
                    }
                }
            }
            return items;
        } else if (par1 instanceof ShapelessRecipes) {
            ShapelessRecipes recipe = (ShapelessRecipes) par1;
            ArrayList arraylist = new ArrayList(recipe.recipeItems);

            for (int i = 0; i < 5; ++i) {
                for (int j = 0; j < 5; ++j) {
	            	/*if(arraylist.isEmpty())
	            	{
	            		return items;
	            	}

	            	int k = i * 5 + j;
	            	items[k] = (ItemStack)arraylist.get(k);
	            	arraylist.remove(k);*/

                    if (arraylist.isEmpty()) {
                        return items;
                    }

                    int k = i * 5 + j;
                    items[k] = (ItemStack) arraylist.get(0);
                    arraylist.remove(0);
                }
            }
        } else if (par1 instanceof ShapedOreRecipe) {
            ShapedOreRecipe recipe = (ShapedOreRecipe) par1;
            for (int i = 0; i < 5; ++i)//h
            {
                if (i < 3) {
                    for (int j = 0; j < 5; ++j)//w
                    {
                        if (j < 3) {
                            int k = i * 3 + j;
                            if (k < recipe.getInput().length) {
                                Object obj = recipe.getInput()[k];
                                if (obj instanceof ItemStack) {
                                    items[i * 5 + j] = (ItemStack) obj;
                                } else if (obj instanceof ArrayList) {
                                    items[i * 5 + j] = (ItemStack) ((ArrayList) obj).get(0);
                                }
                            }
                        }
                    }
                }
            }
        } else if (par1 instanceof ShapelessOreRecipe) {
            ShapelessOreRecipe recipe = (ShapelessOreRecipe) par1;
            ArrayList arraylist = new ArrayList(recipe.getInput());

            for (int i = 0; i < 5; ++i) {
                for (int j = 0; j < 5; ++j) {
                    int k = i * 5 + j;
                    if (arraylist.isEmpty() || k >= arraylist.size()) {
                        return items;
                    }

                    Object obj = arraylist.get(k);
                    if (obj instanceof ItemStack) {
                        items[k] = (ItemStack) obj;
                    } else if (obj instanceof ArrayList) {
                        items[k] = (ItemStack) ((ArrayList) obj).get(0);
                    }
                    arraylist.remove(k);
                }
            }
        }

        return items;
    }
}