package jp.ngt.rtm.item

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import jp.ngt.ngtlib.item.ItemBlockCustomColor
import jp.ngt.rtm.RTMCore
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.StatCollector
import net.minecraft.world.World

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class ItemBlockMarker(block: Block) : ItemBlockCustomColor(block) {
    override fun onItemRightClick(itemStack: ItemStack, world: World, player: EntityPlayer): ItemStack {
        if (world.isRemote) {
            player.openGui(RTMCore.instance, RTMCore.guiIdMarkerItemSettings.toInt(), world, 0, 0, 0)
        }
        return itemStack
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(
        itemStack: ItemStack,
        player: EntityPlayer,
        list: MutableList<Any?>,
        advanced: Boolean,
    ) {
        super.addInformation(itemStack, player, list, advanced)
        val label = StatCollector.translateToLocal("item.marker.height")
        list.add(EnumChatFormatting.GRAY.toString() + label + ": " + getMarkerHeight(itemStack))
    }

    @SideOnly(Side.CLIENT)
    override fun hasEffect(itemStack: ItemStack): Boolean = getMarkerHeight(itemStack) > MIN_HEIGHT

    @SideOnly(Side.CLIENT)
    override fun hasEffect(itemStack: ItemStack, pass: Int): Boolean = hasEffect(itemStack)

    companion object {
        private const val HEIGHT_NBT_KEY = "MarkerHeight"
        const val MIN_HEIGHT = 0
        const val MAX_HEIGHT = 15

        @JvmStatic
        fun getMarkerHeight(itemStack: ItemStack?): Int {
            if (itemStack == null || !itemStack.hasTagCompound()) {
                return MIN_HEIGHT
            }
            return clampMarkerHeight(itemStack.tagCompound.getByte(HEIGHT_NBT_KEY).toInt())
        }

        @JvmStatic
        fun setMarkerHeight(itemStack: ItemStack?, height: Int) {
            if (itemStack == null) {
                return
            }
            if (!itemStack.hasTagCompound()) {
                itemStack.tagCompound = NBTTagCompound()
            }
            itemStack.tagCompound.setByte(HEIGHT_NBT_KEY, clampMarkerHeight(height).toByte())
        }

        @JvmStatic
        fun clampMarkerHeight(height: Int): Int = height.coerceIn(MIN_HEIGHT, MAX_HEIGHT)
    }
}
