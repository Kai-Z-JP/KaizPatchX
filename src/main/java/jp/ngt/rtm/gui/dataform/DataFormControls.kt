package jp.ngt.rtm.gui.dataform

import jp.ngt.rtm.modelpack.cfg.DataFormConfig
import jp.ngt.rtm.modelpack.cfg.DataFormValidator
import jp.ngt.rtm.modelpack.state.DataEntry
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.resources.I18n

internal data class DataFormSubmission(
    val entries: Map<String, DataEntry<*>> = emptyMap(),
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
        components: Collection<DataFormComponent>
    ): DataFormSubmission {
        val entries = LinkedHashMap<String, DataEntry<*>>(components.size)
        components.filterIsInstance<DataFormValueComponent<*>>().forEach { component ->
            try {
                entries[component.key] = component.entry
            } catch (_: RuntimeException) {
                return DataFormSubmission(error = "Invalid value: ${component.key}")
            }
        }
        val validation = DataFormValidator.validate(definition, entries)
        if (!validation.isValid) {
            return DataFormSubmission(error = validation.error)
        }
        return DataFormSubmission(
            validation.values.associateTo(LinkedHashMap()) { it.key to it.entry }
        )
    }

    fun focusNextTextField(fields: List<GuiTextField>) {
        val currentIndex = fields.indexOfFirst { it.isFocused }
        fields.forEach { it.isFocused = false }
        val next = fields[(currentIndex + 1).mod(fields.size)]
        next.isFocused = true
        next.cursorPosition = next.text.length
    }
}
