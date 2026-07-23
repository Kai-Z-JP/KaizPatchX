package jp.ngt.rtm.gui.dataform

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import jp.ngt.rtm.modelpack.cfg.DataFormConfig
import jp.ngt.rtm.modelpack.cfg.DataFormField
import jp.ngt.rtm.modelpack.cfg.ResourceConfig
import jp.ngt.rtm.modelpack.state.DataEntry
import jp.ngt.rtm.modelpack.state.DataEntryList
import jp.ngt.rtm.modelpack.state.DataType
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import kotlin.math.max

@SideOnly(Side.CLIENT)
internal class ListFormComponent(
    override val field: DataFormField,
    private val definition: ResourceConfig.DMInitValue,
    currentEntry: DataEntry<*>?
) : DataFormValueComponent<List<String>> {
    private val elementType = DataEntryList.supportedElementType(definition.elementType)
    private val elements = initialValues(currentEntry)
    private val visibleFields = LinkedHashMap<Int, List<GuiTextField>>()
    private val buttonActions = HashMap<Int, (GuiButton) -> DataFormActionResult>()
    private val mutableLabels = ArrayList<DataFormLabel>()
    private var buttonFormatter: ((String, Int) -> String)? = null

    override val rowHeight: Int
        get() = max(
            DataFormMetrics.ROW_HEIGHT,
            DataFormMetrics.LABEL_HEIGHT +
                    max(1, elements.size) * DataFormMetrics.LIST_ITEM_HEIGHT +
                    DataFormMetrics.CONTROL_MARGIN
        )
    override val labels: List<DataFormLabel>
        get() = mutableLabels
    override val textFields: List<GuiTextField>
        get() = visibleFields.values.flatten()

    override fun build(context: DataFormBuildContext) {
        visibleFields.clear()
        buttonActions.clear()
        mutableLabels.clear()
        buttonFormatter = context::formatButtonValue

        val axes = if (elementType == DataType.VEC) " (X / Y / Z)" else ""
        context.addMainLabel("${field.resolvedLabel()}$axes (${elements.size})", mutableLabels)
        val resolvedElementType = elementType ?: return
        val minItems = DataFormConfig.getMinItems(definition)
        val maxItems = DataFormConfig.getMaxItems(definition)

        elements.forEachIndexed { index, elementValue ->
            buildElementRow(
                context,
                resolvedElementType,
                index,
                elementValue,
                elements.size > minItems,
                elements.size < maxItems
            )
        }
        if (elements.isEmpty()) {
            buildEmptyAddButton(context, resolvedElementType, maxItems > 0)
        }
    }

    private fun buildElementRow(
        context: DataFormBuildContext,
        resolvedElementType: DataType,
        index: Int,
        elementValue: String,
        removeEnabled: Boolean,
        addEnabled: Boolean
    ) {
        val controlY = context.localY + DataFormMetrics.LABEL_HEIGHT +
                index * DataFormMetrics.LIST_ITEM_HEIGHT
        if (!context.isControlVisible(controlY, DataFormMetrics.BUTTON_HEIGHT)) {
            return
        }

        mutableLabels += DataFormLabel(
            "$index.",
            context.localX + DataFormMetrics.CONTROL_MARGIN,
            controlY + DataFormMetrics.LIST_INDEX_TEXT_OFFSET,
            DataFormMetrics.LIST_INDEX_WIDTH,
            DataFormMetrics.MUTED_TEXT_COLOR
        )
        val controlX = context.guiLeft + context.localX + DataFormMetrics.CONTROL_MARGIN +
                DataFormMetrics.LIST_INDEX_WIDTH
        val absoluteY = context.guiTop + controlY
        val elementWidth = max(
            1,
            context.controlWidth - DataFormMetrics.LIST_INDEX_WIDTH -
                    DataFormMetrics.LIST_ACTION_BUTTON_WIDTH * 2 -
                    DataFormMetrics.LIST_BUTTON_GAP * 2
        )

        when {
            resolvedElementType == DataType.VEC -> {
                val components = parseVector(elementValue) ?: listOf("0", "0", "0")
                visibleFields[index] = context.createVectorFields(controlX, absoluteY, elementWidth, components)
            }

            resolvedElementType == DataType.BOOLEAN -> {
                actionButton(
                    context,
                    controlX,
                    absoluteY,
                    context.formatButtonValue(elementValue, elementWidth),
                    width = elementWidth
                ) { button -> toggleBooleanElement(index, button) }
            }

            !definition.suggestions.isNullOrEmpty() -> {
                suggestionButton(
                    context,
                    controlX,
                    absoluteY,
                    index,
                    elementWidth
                )
            }

            else -> {
                visibleFields[index] = listOf(
                    context.createTextField(controlX, absoluteY, elementWidth, elementValue)
                )
            }
        }

        val minusX = controlX + elementWidth + DataFormMetrics.LIST_BUTTON_GAP
        actionButton(
            context,
            minusX,
            absoluteY,
            "-",
            removeEnabled
        ) { removeElement(index) }
        actionButton(
            context,
            minusX + DataFormMetrics.LIST_ACTION_BUTTON_WIDTH + DataFormMetrics.LIST_BUTTON_GAP,
            absoluteY,
            "+",
            addEnabled
        ) { addElement(index, resolvedElementType) }
    }

    private fun buildEmptyAddButton(context: DataFormBuildContext, type: DataType, enabled: Boolean) {
        val controlY = context.localY + DataFormMetrics.LABEL_HEIGHT
        if (!context.isControlVisible(controlY, DataFormMetrics.BUTTON_HEIGHT)) {
            return
        }
        actionButton(
            context,
            context.guiLeft + context.localX + DataFormMetrics.CONTROL_MARGIN +
                    context.controlWidth - DataFormMetrics.LIST_ACTION_BUTTON_WIDTH,
            context.guiTop + controlY,
            "+",
            enabled
        ) { addElement(-1, type) }
    }

    private fun actionButton(
        context: DataFormBuildContext,
        x: Int,
        y: Int,
        text: String,
        enabled: Boolean = true,
        width: Int = DataFormMetrics.LIST_ACTION_BUTTON_WIDTH,
        action: (GuiButton) -> DataFormActionResult
    ) {
        context.createButton(
            context.allocateControlId(),
            x,
            y,
            width,
            DataFormMetrics.BUTTON_HEIGHT,
            text
        ).also { button ->
            button.enabled = enabled
            context.addButton(button)
            buttonActions[button.id] = action
        }
    }

    private fun suggestionButton(
        context: DataFormBuildContext,
        x: Int,
        y: Int,
        elementIndex: Int,
        width: Int
    ) {
        val suggestions = definition.suggestions?.asList().orEmpty()
        val button = context.createSuggestionButton(
            context.allocateControlId(),
            x,
            y,
            width,
            DataFormMetrics.BUTTON_HEIGHT,
            { suggestions.indexOf(elements.getOrNull(elementIndex)) },
            suggestions
        ) selection@{
            val selectedValue = suggestions.getOrNull(this) ?: return@selection
            if (elementIndex in elements.indices) {
                elements[elementIndex] = selectedValue
            }
        }
        context.addButton(button)
        buttonActions[button.id] = { DataFormActionResult() }
    }

    override fun syncValue() {
        visibleFields.forEach { (index, fields) ->
            if (index in elements.indices) {
                elements[index] = fields.joinToString(" ") { it.text.trim() }
            }
        }
    }

    override val fieldValue: List<String>
        get() = elements

    override fun createEntry(value: List<String>): DataEntry<*> =
        DataEntryList.fromValues(elementType ?: DataType.STRING, value, 0)

    override fun handleButton(button: GuiButton): DataFormActionResult? =
        buttonActions[button.id]?.invoke(button)

    private fun addElement(index: Int, resolvedElementType: DataType): DataFormActionResult {
        if (elements.size >= DataFormConfig.getMaxItems(definition)) {
            return DataFormActionResult()
        }
        val value = definition.suggestions?.firstOrNull()
            ?: DataEntryList.defaultElementValue(resolvedElementType)
        elements.add((index + 1).coerceIn(0, elements.size), value)
        return DataFormActionResult(
            rebuild = true,
            scrollDelta = DataFormMetrics.LIST_ITEM_HEIGHT
        )
    }

    private fun removeElement(index: Int): DataFormActionResult {
        if (elements.size <= DataFormConfig.getMinItems(definition) || index !in elements.indices) {
            return DataFormActionResult()
        }
        elements.removeAt(index)
        return DataFormActionResult(rebuild = true)
    }

    private fun toggleBooleanElement(index: Int, button: GuiButton): DataFormActionResult {
        val current = elements.getOrNull(index) ?: return DataFormActionResult()
        val next = (!current.toBoolean()).toString()
        elements[index] = next
        button.displayString = buttonFormatter?.invoke(next, button.width) ?: next
        return DataFormActionResult()
    }

    private fun initialValues(currentEntry: DataEntry<*>?): MutableList<String> {
        val resolvedElementType = elementType ?: return mutableListOf()
        return ((currentEntry as? DataEntryList)
            ?.takeIf { it.elementType == resolvedElementType }
            ?.get()
            ?.map { DataEntryList.elementToString(it, resolvedElementType) }
            ?: definition.values?.asList().orEmpty())
            .toMutableList()
    }
}
