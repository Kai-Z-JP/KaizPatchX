package jp.ngt.rtm.gui.dataform

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import jp.ngt.rtm.modelpack.cfg.DataFormField
import jp.ngt.rtm.modelpack.cfg.ResourceConfig
import jp.ngt.rtm.modelpack.state.DataEntry
import jp.ngt.rtm.modelpack.state.DataEntryList
import jp.ngt.rtm.modelpack.state.DataType
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import kotlin.math.max

@SideOnly(Side.CLIENT)
internal interface DataFormComponent {
    val field: DataFormField
    val rowHeight: Int
    val labels: List<DataFormLabel> get() = emptyList()
    val textFields: List<GuiTextField> get() = emptyList()

    val key: String
        get() = this.field.resolvedKey()

    fun measuredRowHeight(fontRenderer: FontRenderer, controlWidth: Int): Int = rowHeight

    fun build(context: DataFormBuildContext)

    fun syncValue() = Unit

    fun handleButton(button: GuiButton): DataFormActionResult? = null
}

internal interface DataFormValueComponent<T : Any> : DataFormComponent {
    val fieldValue: T
    val entry: DataEntry<*> get() = createEntry(fieldValue)

    fun createEntry(value: T): DataEntry<*>
}

internal data class DataFormLabel(
    val text: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val color: Int = DataFormMetrics.TEXT_COLOR
)

internal data class DataFormActionResult(
    val rebuild: Boolean = false,
    val scrollDelta: Int = 0
)

@SideOnly(Side.CLIENT)
internal class DataFormBuildContext(
    val fontRenderer: FontRenderer,
    val guiLeft: Int,
    val guiTop: Int,
    val localX: Int,
    val localY: Int,
    val controlWidth: Int,
    private val clip: DataFormClip,
    private val nextControlId: () -> Int,
    private val isVisible: (localY: Int, height: Int) -> Boolean,
    private val addButton: (GuiButton) -> Unit
) {
    fun allocateControlId(): Int = nextControlId()

    fun isLabelVisible(): Boolean = isVisible(localY, DataFormMetrics.LABEL_HEIGHT)

    fun isControlVisible(controlY: Int, height: Int): Boolean = isVisible(controlY, height)

    fun addButton(button: GuiButton) = addButton.invoke(button)

    fun createButton(id: Int, x: Int, y: Int, width: Int, height: Int, text: String): GuiButton =
        ClippedButton(id, x, y, width, height, text, clip)

    fun createSuggestionButton(
        id: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        index: () -> Int,
        suggestions: List<String>,
        onSelect: Int.() -> Unit
    ): GuiButton = ClippedScrollingListButton(
        id,
        x,
        y,
        width,
        height,
        index,
        suggestions,
        onSelect,
        clip
    )

    fun addMainLabel(text: String, target: MutableList<DataFormLabel>) {
        if (isLabelVisible()) {
            target += DataFormLabel(
                text,
                localX + DataFormMetrics.CONTROL_MARGIN,
                localY,
                controlWidth
            )
        }
    }

    fun createTextField(x: Int, y: Int, width: Int, value: String): GuiTextField {
        val field = GuiTextField(
            fontRenderer,
            x,
            y,
            max(1, width),
            DataFormMetrics.CONTROL_HEIGHT
        )
        field.maxStringLength = DataFormMetrics.MAX_TEXT_FIELD_LENGTH
        field.text = value
        return field
    }

    fun createVectorFields(x: Int, y: Int, width: Int, components: List<String>): List<GuiTextField> {
        val fields = ArrayList<GuiTextField>(DataFormMetrics.VECTOR_COMPONENT_COUNT)
        val usableWidth = max(
            DataFormMetrics.VECTOR_COMPONENT_COUNT,
            width - DataFormMetrics.VECTOR_FIELD_GAP * (DataFormMetrics.VECTOR_COMPONENT_COUNT - 1)
        )
        val componentWidth = usableWidth / DataFormMetrics.VECTOR_COMPONENT_COUNT
        var fieldX = x
        repeat(DataFormMetrics.VECTOR_COMPONENT_COUNT) { component ->
            val fieldWidth = if (component == DataFormMetrics.VECTOR_COMPONENT_COUNT - 1) {
                x + width - fieldX
            } else {
                componentWidth
            }
            fields += createTextField(fieldX, y, fieldWidth, components[component])
            fieldX += fieldWidth + DataFormMetrics.VECTOR_FIELD_GAP
        }
        return fields
    }

    fun formatButtonValue(value: String, width: Int): String =
        fontRenderer.trimStringToWidth(value, max(1, width - 6))
}

internal object DataFormComponentFactory {
    fun create(
        field: DataFormField,
        definition: ResourceConfig.DMInitValue?,
        currentEntry: DataEntry<*>?
    ): DataFormComponent? {
        if (field.isTextElement()) {
            return TextFormComponent(field)
        }
        val resolvedDefinition = definition ?: return null
        return when (DataType.getType(resolvedDefinition.type)) {
            DataType.LIST -> if (DataEntryList.supportedElementType(resolvedDefinition.elementType) == null) {
                null
            } else {
                ListFormComponent(field, resolvedDefinition, currentEntry)
            }

            null -> null
            else -> ScalarFormComponent(field, resolvedDefinition, currentEntry)
        }
    }
}

internal fun parseVector(value: String?): List<String>? {
    val trimmed = value?.trim().orEmpty()
    if (trimmed.isEmpty()) {
        return null
    }
    return trimmed.split(DataFormMetrics.WHITESPACE_REGEX)
        .takeIf { it.size == DataFormMetrics.VECTOR_COMPONENT_COUNT }
}

internal object DataFormMetrics {
    const val APPLY_BUTTON_ID = 0
    const val CANCEL_BUTTON_ID = 1
    const val FORM_CONTROL_ID = 100
    const val GRID_PADDING = 8
    const val PREFERRED_CELL_WIDTH = 130
    const val PREFERRED_HEIGHT = 240
    const val TITLE_TOP = 8
    const val LIST_TOP = 24
    const val ROW_HEIGHT = 40
    const val TEXT_ROW_HEIGHT = 20
    const val TEXT_LINE_HEIGHT = 10
    const val LABEL_HEIGHT = 11
    const val CONTROL_MARGIN = 4
    const val CONTROL_HEIGHT = 18
    const val FOOTER_HEIGHT = 34
    const val FOOTER_BUTTON_OFFSET = 25
    const val BUTTON_HEIGHT = 20
    const val MAX_TEXT_FIELD_LENGTH = 1024
    const val VECTOR_COMPONENT_COUNT = 3
    const val VECTOR_FIELD_GAP = 3
    const val LIST_ITEM_HEIGHT = 22
    const val LIST_INDEX_WIDTH = 18
    const val LIST_INDEX_TEXT_OFFSET = 5
    const val LIST_BUTTON_GAP = 3
    const val LIST_ACTION_BUTTON_WIDTH = 20
    const val SCROLL_STEP = 20
    const val MIN_SCROLL_BAR_HEIGHT = 12
    val WHITESPACE_REGEX = Regex("\\s+")
    const val BACKGROUND_COLOR = 0xFFD0D0D0.toInt()
    const val BORDER_COLOR = 0xFF555555.toInt()
    const val LIST_COLOR = 0xFFF2F2F2.toInt()
    const val SCROLL_TRACK_COLOR = 0xFFB8B8B8.toInt()
    const val SCROLL_BAR_COLOR = 0xFF666666.toInt()
    const val TEXT_COLOR = 0x303030
    const val MUTED_TEXT_COLOR = 0x666666
    const val ERROR_COLOR = 0xAA2020
}
