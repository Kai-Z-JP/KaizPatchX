package jp.ngt.rtm.gui.dataform

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import jp.ngt.rtm.modelpack.cfg.*
import jp.ngt.rtm.modelpack.state.*
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import kotlin.math.max

@SideOnly(Side.CLIENT)
internal class ListCompoundFormComponent(
    override val field: DataFormField,
    private val definition: ResourceConfig.DMInitValue,
    currentEntry: DataEntry<*>?
) : DataFormOperationSource {
    private val elementForm = requireNotNull(field.elementForm)
    private val elements = initialElements(currentEntry)
    private val structureEdits = ArrayList<StructureEdit>()
    private val buttonActions = HashMap<Int, () -> DataFormActionResult>()
    private val ownLabels = ArrayList<DataFormLabel>()

    override val rowHeight: Int = DataFormMetrics.ROW_HEIGHT
    override val labels: List<DataFormLabel>
        get() = ownLabels + elements.flatMap { state -> state.components.flatMap(DataFormComponent::labels) }
    override val textFields: List<GuiTextField>
        get() = elements.flatMap { state -> state.components.flatMap(DataFormComponent::textFields) }

    override fun measuredRowHeight(fontRenderer: FontRenderer, controlWidth: Int): Int =
        DataFormMetrics.LABEL_HEIGHT + if (elements.isEmpty()) {
            DataFormMetrics.BUTTON_HEIGHT + DataFormMetrics.CONTROL_MARGIN
        } else {
            elements.sumOf { elementHeight(it, fontRenderer, controlWidth) }
        }

    override fun build(context: DataFormBuildContext) {
        buttonActions.clear()
        ownLabels.clear()
        context.addMainLabel("${field.resolvedLabel()} (${elements.size})", ownLabels)
        var offsetY = DataFormMetrics.LABEL_HEIGHT
        elements.forEachIndexed { index, state ->
            buildElement(context, state, index, offsetY)
            offsetY += elementHeight(state, context.fontRenderer, context.controlWidth)
        }
        if (elements.isEmpty()) {
            addActionButton(
                context,
                context.guiLeft + context.localX + context.controlWidth - DataFormMetrics.LIST_ACTION_BUTTON_WIDTH,
                context.guiTop + context.localY + offsetY,
                "+",
                DataFormConfig.getMaxItems(definition) > 0
            ) { addElement(-1) }
        }
    }

    private fun buildElement(
        context: DataFormBuildContext,
        state: ElementState,
        index: Int,
        offsetY: Int
    ) {
        val headerY = context.localY + offsetY
        ownLabels += DataFormLabel(
            "[$index]",
            context.localX + DataFormMetrics.CONTROL_MARGIN,
            headerY + DataFormMetrics.LIST_INDEX_TEXT_OFFSET,
            DataFormMetrics.LIST_INDEX_WIDTH * 2,
            DataFormMetrics.MUTED_TEXT_COLOR
        )
        val actionsWidth = DataFormMetrics.LIST_ACTION_BUTTON_WIDTH * 2 + DataFormMetrics.LIST_BUTTON_GAP
        val actionX = context.guiLeft + context.localX + context.controlWidth - actionsWidth
        val absoluteY = context.guiTop + headerY
        addActionButton(
            context, actionX, absoluteY, "-",
            elements.size > DataFormConfig.getMinItems(definition)
        ) { removeElement(index) }
        addActionButton(
            context,
            actionX + DataFormMetrics.LIST_ACTION_BUTTON_WIDTH + DataFormMetrics.LIST_BUTTON_GAP,
            absoluteY,
            "+",
            elements.size < DataFormConfig.getMaxItems(definition)
        ) { addElement(index) }

        val columns = elementForm.columns.coerceAtLeast(1)
        val innerX = context.localX + DataFormMetrics.CONTROL_MARGIN
        val innerWidth = max(20, context.controlWidth - DataFormMetrics.CONTROL_MARGIN * 2)
        val cellWidth = innerWidth / columns
        val layout = DataFormGridLayout(elementForm, state.components, context.fontRenderer, cellWidth)
        val contentY = headerY + ELEMENT_HEADER_HEIGHT
        state.components.forEach { component ->
            val childField = component.field
            val rowTop = layout.rowOffsets.getOrNull(childField.row) ?: return@forEach
            val childX = innerX + childField.column * cellWidth
            val childWidth = max(
                20,
                cellWidth * childField.columnSpan - DataFormMetrics.CONTROL_MARGIN * 2
            )
            component.build(context.nested(childX, contentY + rowTop, childWidth))
        }
    }

    private fun addActionButton(
        context: DataFormBuildContext,
        x: Int,
        y: Int,
        text: String,
        enabled: Boolean,
        action: () -> DataFormActionResult
    ) {
        val button = context.createButton(
            context.allocateControlId(), x, y,
            DataFormMetrics.LIST_ACTION_BUTTON_WIDTH,
            DataFormMetrics.BUTTON_HEIGHT, text
        )
        button.enabled = enabled
        context.addButton(button)
        buttonActions[button.id] = action
    }

    override fun syncValue() {
        elements.forEach { state -> state.components.forEach(DataFormComponent::syncValue) }
    }

    override fun handleButton(button: GuiButton): DataFormActionResult? {
        buttonActions[button.id]?.let { return it.invoke() }
        elements.forEach { state ->
            state.components.forEach { component ->
                component.handleButton(button)?.let { return it }
            }
        }
        return null
    }

    override fun collectOperations(
        rootKey: String,
        prefix: List<DataFormPathSegment>
    ): List<DataFormOperation> {
        val operations = ArrayList<DataFormOperation>()
        structureEdits.forEach { edit ->
            operations += when (edit) {
                is StructureEdit.Insert -> DataFormOperation.insert(rootKey, prefix, edit.index)
                is StructureEdit.Remove -> DataFormOperation.remove(rootKey, prefix, edit.index)
            }
        }
        elements.forEachIndexed { index, state ->
            state.components.filterNot { it.field.isTextElement() }.forEach { component ->
                val childPrefix = prefix + DataFormPathSegment.Element(index) +
                        component.field.resolvedPath().map(DataFormPathSegment::Key)
                when (component) {
                    is DataFormOperationSource -> operations += component.collectOperations(rootKey, childPrefix)
                    is DataFormValueComponent<*> -> operations +=
                        DataFormOperation.set(rootKey, childPrefix, component.entry)
                }
            }
        }
        return operations
    }

    private fun addElement(index: Int): DataFormActionResult {
        if (elements.size >= DataFormConfig.getMaxItems(definition)) return DataFormActionResult()
        val insertAt = (index + 1).coerceIn(0, elements.size)
        elements.add(insertAt, createElement(DataCompoundDefinitions.createDefault(definition)))
        structureEdits += StructureEdit.Insert(insertAt)
        return DataFormActionResult(rebuild = true, scrollDelta = DataFormMetrics.ROW_HEIGHT)
    }

    private fun removeElement(index: Int): DataFormActionResult {
        if (elements.size <= DataFormConfig.getMinItems(definition) || index !in elements.indices) {
            return DataFormActionResult()
        }
        elements.removeAt(index)
        structureEdits += StructureEdit.Remove(index)
        return DataFormActionResult(rebuild = true)
    }

    private fun elementHeight(state: ElementState, fontRenderer: FontRenderer, width: Int): Int {
        val innerWidth = max(20, width - DataFormMetrics.CONTROL_MARGIN * 2)
        val cellWidth = innerWidth / elementForm.columns.coerceAtLeast(1)
        val contentHeight = DataFormGridLayout(elementForm, state.components, fontRenderer, cellWidth).contentHeight
        return ELEMENT_HEADER_HEIGHT + max(DataFormMetrics.ROW_HEIGHT, contentHeight) + ELEMENT_GAP
    }

    private fun initialElements(currentEntry: DataEntry<*>?): MutableList<ElementState> {
        val source = (currentEntry as? DataEntryList)
            ?.takeIf { it.elementType == DataType.COMPOUND }
            ?: DataTypeHandlers.createDefault(DataType.LIST, definition, 0) as DataEntryList
        return source.get().map { createElement(it as DataCompound) }.toMutableList()
    }

    private fun createElement(value: DataCompound): ElementState {
        val root = DataEntryCompound.fromCompound(value, 0)
        val components = elementForm.getFieldList().mapNotNull { childField ->
            val childDefinition = elementForm.getResolvedDefinition(childField)
            val current = DataFormTree.entryAt(
                root,
                childField.resolvedPath().map(DataFormPathSegment::Key)
            )
            DataFormComponentFactory.create(childField, childDefinition, current)
        }
        return ElementState(components)
    }

    private data class ElementState(val components: List<DataFormComponent>)
    private sealed class StructureEdit {
        data class Insert(val index: Int) : StructureEdit()
        data class Remove(val index: Int) : StructureEdit()
    }

    companion object {
        private const val ELEMENT_HEADER_HEIGHT = 22
        private const val ELEMENT_GAP = 4
    }
}
