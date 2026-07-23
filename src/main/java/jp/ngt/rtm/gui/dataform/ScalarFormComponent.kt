package jp.ngt.rtm.gui.dataform

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import jp.ngt.rtm.modelpack.cfg.DataFormField
import jp.ngt.rtm.modelpack.cfg.ResourceConfig
import jp.ngt.rtm.modelpack.state.DataEntry
import jp.ngt.rtm.modelpack.state.DataType
import jp.ngt.rtm.modelpack.state.DataTypeHandlers
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField

@SideOnly(Side.CLIENT)
internal class ScalarFormComponent(
    override val field: DataFormField,
    private val definition: ResourceConfig.DMInitValue,
    currentEntry: DataEntry<*>?
) : DataFormValueComponent<String> {
    private val type = requireNotNull(DataType.getType(definition.type))
    private var value = currentEntry?.takeIf { it.type == type }?.toString() ?: definition.value.orEmpty()
    private var fields: List<GuiTextField> = emptyList()
    private var buttonId: Int? = null
    private var buttonFormatter: ((String, Int) -> String)? = null
    private val mutableLabels = ArrayList<DataFormLabel>(1)

    override val rowHeight: Int = DataFormMetrics.ROW_HEIGHT
    override val labels: List<DataFormLabel>
        get() = mutableLabels
    override val textFields: List<GuiTextField>
        get() = fields

    override fun build(context: DataFormBuildContext) {
        fields = emptyList()
        buttonId = null
        mutableLabels.clear()
        context.addMainLabel(field.resolvedLabel(), mutableLabels)

        val controlY = context.localY + DataFormMetrics.LABEL_HEIGHT
        val usesButton = type == DataType.BOOLEAN || !definition.suggestions.isNullOrEmpty()
        val controlHeight = if (usesButton) {
            DataFormMetrics.BUTTON_HEIGHT
        } else {
            DataFormMetrics.CONTROL_HEIGHT
        }
        if (!context.isControlVisible(controlY, controlHeight)) {
            return
        }
        val absoluteX = context.guiLeft + context.localX + DataFormMetrics.CONTROL_MARGIN
        val absoluteY = context.guiTop + controlY
        val suggestions = definition.suggestions?.asList().orEmpty()
        when {
            type == DataType.BOOLEAN -> {
                val id = context.allocateControlId()
                val button = context.createButton(
                    id,
                    absoluteX,
                    absoluteY,
                    context.controlWidth,
                    DataFormMetrics.BUTTON_HEIGHT,
                    context.formatButtonValue(value, context.controlWidth)
                )
                context.addButton(button)
                buttonId = id
                buttonFormatter = context::formatButtonValue
            }

            suggestions.isNotEmpty() -> {
                val id = context.allocateControlId()
                val button = context.createSuggestionButton(
                    id,
                    absoluteX,
                    absoluteY,
                    context.controlWidth,
                    DataFormMetrics.BUTTON_HEIGHT,
                    { suggestions.indexOf(value) },
                    suggestions
                ) selection@{
                    val selectedValue = suggestions.getOrNull(this) ?: return@selection
                    this@ScalarFormComponent.value = selectedValue
                }
                context.addButton(button)
                buttonId = id
            }

            else -> fields = listOf(context.createTextField(absoluteX, absoluteY, context.controlWidth, value))
        }
    }

    override fun syncValue() {
        if (fields.isNotEmpty()) {
            value = fields.single().text
        }
    }

    override val fieldValue: String
        get() = value

    override fun createEntry(value: String): DataEntry<*> =
        DataTypeHandlers.get(type).parse(value, definition, 0)

    override fun handleButton(button: GuiButton): DataFormActionResult? {
        if (button.id != buttonId) {
            return null
        }
        if (type != DataType.BOOLEAN) {
            return DataFormActionResult()
        }
        value = (!value.toBoolean()).toString()
        button.displayString = buttonFormatter?.invoke(value, button.width) ?: value
        return DataFormActionResult()
    }
}
