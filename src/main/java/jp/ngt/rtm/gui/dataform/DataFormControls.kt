package jp.ngt.rtm.gui.dataform

import jp.ngt.rtm.modelpack.cfg.DataFormConfig
import jp.ngt.rtm.modelpack.cfg.DataFormOperation
import jp.ngt.rtm.modelpack.cfg.DataFormPathSegment
import jp.ngt.rtm.modelpack.cfg.DataFormValidator
import jp.ngt.rtm.modelpack.state.DataEntry
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.resources.I18n

internal data class DataFormSubmission(
    val operations: List<DataFormOperation> = emptyList(),
    val error: String? = null
) {
    val isValid: Boolean
        get() = error == null
}

internal object DataFormControls {
    fun footerButtons(guiLeft: Int, guiTop: Int, width: Int, height: Int): List<GuiButton> {
        val halfWidth = (width - DataFormMetrics.GRID_PADDING * 3) / 2
        return listOf(
            GuiButton(
                DataFormMetrics.APPLY_BUTTON_ID,
                guiLeft + DataFormMetrics.GRID_PADDING,
                guiTop + height - DataFormMetrics.FOOTER_BUTTON_OFFSET,
                halfWidth,
                DataFormMetrics.BUTTON_HEIGHT,
                I18n.format("gui.done")
            ),
            GuiButton(
                DataFormMetrics.CANCEL_BUTTON_ID,
                guiLeft + DataFormMetrics.GRID_PADDING * 2 + halfWidth,
                guiTop + height - DataFormMetrics.FOOTER_BUTTON_OFFSET,
                halfWidth,
                DataFormMetrics.BUTTON_HEIGHT,
                I18n.format("gui.cancel")
            )
        )
    }

    fun validate(
        definition: DataFormConfig?,
        components: Collection<DataFormComponent>,
        initialEntries: Map<String, DataEntry<*>>
    ): DataFormSubmission {
        val operations = ArrayList<DataFormOperation>()
        try {
            components.filterNot { it.field.isTextElement() }.forEach { component ->
                val rootKey = component.field.resolvedKey()
                val prefix = component.field.resolvedPath().map(DataFormPathSegment::Key)
                when (component) {
                    is DataFormOperationSource -> operations += component.collectOperations(rootKey, prefix)
                    is DataFormValueComponent<*> -> operations +=
                        DataFormOperation.set(rootKey, prefix, component.entry)
                }
            }
        } catch (_: RuntimeException) {
            return DataFormSubmission(error = "Invalid form value")
        }
        val validation = DataFormValidator.validate(definition, operations, initialEntries)
        if (!validation.isValid) {
            return DataFormSubmission(error = validation.error)
        }
        return DataFormSubmission(operations)
    }

    fun focusNextTextField(fields: List<GuiTextField>) {
        val currentIndex = fields.indexOfFirst { it.isFocused }
        fields.forEach { it.isFocused = false }
        val next = fields[(currentIndex + 1).mod(fields.size)]
        next.isFocused = true
        next.cursorPosition = next.text.length
    }
}
