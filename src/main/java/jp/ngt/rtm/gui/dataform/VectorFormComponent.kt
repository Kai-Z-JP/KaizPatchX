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
internal class VectorFormComponent(
    override val field: DataFormField,
    private val definition: ResourceConfig.DMInitValue,
    currentEntry: DataEntry<*>?
) : DataFormValueComponent<String> {
    private var value = currentEntry
        ?.takeIf { it.type == DataType.VEC }
        ?.toString()
        ?: definition.value.orEmpty()
    private var input: VectorFormInput? = null
    private val mutableLabels = ArrayList<DataFormLabel>(1)

    override val rowHeight: Int = DataFormMetrics.ROW_HEIGHT
    override val labels: List<DataFormLabel>
        get() = mutableLabels
    override val textFields: List<GuiTextField>
        get() = input?.fields.orEmpty()

    override fun build(context: DataFormBuildContext) {
        input = null
        mutableLabels.clear()
        context.addMainLabel("${field.resolvedLabel()} (X / Y / Z)", mutableLabels)

        val controlY = context.localY + DataFormMetrics.LABEL_HEIGHT
        if (!context.isControlVisible(controlY, DataFormMetrics.CONTROL_HEIGHT)) {
            return
        }
        input = context.createVectorInput(
            context.guiLeft + context.localX + DataFormMetrics.CONTROL_MARGIN,
            context.guiTop + controlY,
            context.controlWidth,
            parseVector(value)
                ?: parseVector(definition.value)
                ?: listOf("0", "0", "0")
        )
    }

    override fun syncValue() {
        input?.fields?.let { fields ->
            value = fields.joinToString(" ") { it.text.trim() }
        }
    }

    override val fieldValue: String
        get() = value

    override fun createEntry(value: String): DataEntry<*> =
        DataTypeHandlers.get(DataType.VEC).parse(value, definition, 0)

    override fun handleButton(button: GuiButton): DataFormActionResult? {
        val currentInput = input ?: return null
        if (button.id != currentInput.pasteButtonId) {
            return null
        }
        currentInput.pasteClipboard()
        return DataFormActionResult()
    }
}
