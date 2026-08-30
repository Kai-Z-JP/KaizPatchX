package jp.ngt.rtm.gui

import cpw.mods.fml.client.config.GuiCheckBox
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import jp.ngt.rtm.item.ItemRail
import jp.ngt.rtm.modelpack.state.ResourceState
import net.minecraft.client.gui.GuiButton
import net.minecraft.world.World

@SideOnly(Side.CLIENT)
class GuiSelectRailModel(world: World, private val rail: ItemRail) : GuiSelectModel(world, rail) {
    private lateinit var disableAutoSplitButton: GuiCheckBox

    override fun initGui() {
        super.initGui()
        disableAutoSplitButton = GuiCheckBox(
            AUTO_SPLIT_BUTTON_ID,
            width - 205,
            55,
            "Disable Auto Split",
            !rail.isSelectedItemAutoSplitEnabled,
        )
        buttonList.add(disableAutoSplitButton)
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == AUTO_SPLIT_BUTTON_ID) return
        if (button is GuiButtonSelectModel) applyRailOptions()
        super.actionPerformed(button)
    }

    override fun saveData(state: ResourceState): Boolean {
        applyRailOptions()
        return super.saveData(state)
    }

    private fun applyRailOptions() {
        rail.setSelectedItemAutoSplitEnabled(!disableAutoSplitButton.isChecked)
    }

    private companion object {
        const val AUTO_SPLIT_BUTTON_ID = 10002
    }
}
