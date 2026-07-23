package jp.ngt.rtm.gui.dataform

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import kotlin.math.max
import kotlin.math.min

@SideOnly(Side.CLIENT)
internal class VectorFormInput(
    val fields: List<GuiTextField>,
    val pasteButtonId: Int
) {
    fun pasteClipboard(): Boolean {
        val components = parseClipboardVector(GuiScreen.getClipboardString()) ?: return false
        fields.forEachIndexed { index, field ->
            field.text = components[index]
        }
        return true
    }
}

@SideOnly(Side.CLIENT)
internal fun DataFormBuildContext.createVectorInput(
    x: Int,
    y: Int,
    width: Int,
    components: List<String>
): VectorFormInput {
    val minimumFieldsWidth = DataFormMetrics.VECTOR_COMPONENT_COUNT +
            DataFormMetrics.VECTOR_FIELD_GAP * (DataFormMetrics.VECTOR_COMPONENT_COUNT - 1)
    val pasteGap = min(
        DataFormMetrics.VECTOR_FIELD_GAP,
        max(0, width - minimumFieldsWidth - 1)
    )
    val buttonWidth = min(
        DataFormMetrics.VECTOR_PASTE_BUTTON_WIDTH,
        max(1, width - minimumFieldsWidth - pasteGap)
    )
    val fieldsWidth = max(1, width - buttonWidth - pasteGap)
    val fields = createVectorFields(x, y, fieldsWidth, components)
    val buttonId = allocateControlId()
    addButton(
        createButton(
            buttonId,
            x + width - buttonWidth,
            y,
            buttonWidth,
            DataFormMetrics.CONTROL_HEIGHT,
            DataFormMetrics.VECTOR_PASTE_BUTTON_TEXT
        )
    )
    return VectorFormInput(fields, buttonId)
}

internal fun parseClipboardVector(value: String?): List<String>? {
    val groups = value
        ?.let(CLIPBOARD_VECTOR_REGEX::matchEntire)
        ?.groupValues
        ?: return null
    val components = if (groups[2].isNotEmpty()) {
        listOf(groups[1], groups[2], groups[3])
    } else {
        listOf(groups[1], groups[4], groups[5])
    }
    return components.takeIf { values ->
        values.all { it.toDoubleOrNull()?.isFinite() == true }
    }
}

private const val VECTOR_NUMBER_PATTERN =
    """[+-]?(?:\d+(?:\.\d*)?|\.\d+)"""

private val CLIPBOARD_VECTOR_REGEX = Regex(
    """^\s*($VECTOR_NUMBER_PATTERN)(?:\s*,\s*($VECTOR_NUMBER_PATTERN)\s*,\s*""" +
            """($VECTOR_NUMBER_PATTERN)|\s+($VECTOR_NUMBER_PATTERN)\s+""" +
            """($VECTOR_NUMBER_PATTERN))\s*$"""
)
