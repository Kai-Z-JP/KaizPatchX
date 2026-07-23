package jp.ngt.rtm.gui

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import jp.ngt.rtm.RTMCore
import jp.ngt.rtm.gui.dataform.*
import jp.ngt.rtm.modelpack.DataFormProvider
import jp.ngt.rtm.modelpack.cfg.DataFormConfig
import jp.ngt.rtm.network.PacketDataForm
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import kotlin.math.max
import kotlin.math.min

@SideOnly(Side.CLIENT)
class GuiDataForm(
    private val provider: DataFormProvider
) : GuiScreen() {
    private val formDefinition: DataFormConfig? = provider.dataFormConfig
    private val initialEntries = provider.resourceState.dataMap.getEntries()
    private val fallbackTitle: String = provider.modelName
    private var xSize = 0
    private var ySize = 0
    private var guiLeft = 0
    private var guiTop = 0
    private val components = ArrayList<DataFormComponent>()
    private var componentsInitialized = false
    private var scrollOffset = 0
    private var nextControlId = DataFormMetrics.FORM_CONTROL_ID
    private var validationError: String? = null
    private var mouseClickInProgress = false
    private var rebuildPending = false
    private var pendingScrollDelta = 0

    private val visibleTextFields: List<GuiTextField>
        get() = components.flatMap(DataFormComponent::textFields)

    private val visibleLabels: List<DataFormLabel>
        get() = components.flatMap(DataFormComponent::labels)

    override fun initGui() {
        super.initGui()
        val columns = formDefinition?.columns ?: 1
        xSize = min(
            width - 20,
            max(
                220,
                DataFormMetrics.GRID_PADDING * 2 + columns * DataFormMetrics.PREFERRED_CELL_WIDTH
            )
        )
        ySize = min(height - 20, DataFormMetrics.PREFERRED_HEIGHT)
        guiLeft = (width - xSize) / 2
        guiTop = (height - ySize) / 2
        Keyboard.enableRepeatEvents(true)
        initializeComponents()
        rebuildControls()
    }

    private fun initializeComponents() {
        if (componentsInitialized) {
            return
        }
        componentsInitialized = true
        val definition = formDefinition ?: return
        definition.getFieldList().forEach { field ->
            val key = field.resolvedKey()
            val defaultValue = definition.getDefaultValue(key)
            val component = DataFormComponentFactory.create(
                field,
                defaultValue,
                initialEntries[key]
            ) ?: return@forEach
            components += component
        }
    }

    private fun rebuildControls() {
        buttonList.clear()
        nextControlId = DataFormMetrics.FORM_CONTROL_ID
        buttonList.addAll(DataFormControls.footerButtons(guiLeft, guiTop, xSize, ySize))

        val definition = formDefinition ?: return
        val columns = definition.columns.coerceAtLeast(1)
        val cellWidth = (xSize - DataFormMetrics.GRID_PADDING * 2) / columns
        val layout = DataFormGridLayout(definition, components, fontRendererObj, cellWidth)
        scrollOffset = scrollOffset.coerceIn(0, max(0, layout.contentHeight - viewportHeight))

        components.forEach { component ->
            val field = component.field
            val rowTop = layout.rowOffsets.getOrNull(field.row) ?: return@forEach
            val localX = DataFormMetrics.GRID_PADDING + field.column * cellWidth
            val localY = DataFormMetrics.LIST_TOP + rowTop - scrollOffset
            val controlWidth = max(
                20,
                cellWidth * field.columnSpan - DataFormMetrics.CONTROL_MARGIN * 2
            )
            component.build(
                DataFormBuildContext(
                    fontRendererObj,
                    guiLeft,
                    guiTop,
                    localX,
                    localY,
                    controlWidth,
                    scrollClip,
                    nextControlId = { nextControlId++ },
                    isVisible = ::isControlVisible,
                    addButton = { buttonList.add(it) }
                )
            )
        }
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            DataFormMetrics.APPLY_BUTTON_ID -> applyChanges()
            DataFormMetrics.CANCEL_BUTTON_ID -> closeScreen()
            else -> handleComponentButton(button)
        }
    }

    private fun handleComponentButton(button: GuiButton) {
        syncComponents()
        components.forEach { component ->
            val result = component.handleButton(button) ?: return@forEach
            validationError = null
            if (result.rebuild) {
                requestControlRebuild(result.scrollDelta)
            }
            return
        }
    }

    private fun requestControlRebuild(scrollDelta: Int) {
        rebuildPending = true
        pendingScrollDelta += scrollDelta
        if (!mouseClickInProgress) {
            applyPendingControlRebuild()
        }
    }

    private fun applyPendingControlRebuild() {
        if (!rebuildPending) {
            return
        }
        rebuildPending = false
        val scrollDelta = pendingScrollDelta
        pendingScrollDelta = 0
        scrollOffset = (scrollOffset + scrollDelta).coerceIn(0, maxScrollOffset)
        rebuildControls()
    }

    private fun applyChanges() {
        syncComponents()
        val submission = DataFormControls.validate(formDefinition, components)
        validationError = submission.error
        if (!submission.isValid) {
            return
        }
        RTMCore.NETWORK_WRAPPER.sendToServer(PacketDataForm(provider, submission.entries))
        closeScreen()
    }

    private fun syncComponents() {
        components.forEach(DataFormComponent::syncValue)
    }

    private fun closeScreen() {
        mc.displayGuiScreen(null)
        mc.setIngameFocus()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        mouseClickInProgress = true
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton)
        } finally {
            mouseClickInProgress = false
        }
        applyPendingControlRebuild()
        val clip = scrollClip
        visibleTextFields.forEach { field ->
            if (clip.contains(mouseX, mouseY)) {
                field.mouseClicked(mouseX, mouseY, mouseButton)
            } else {
                field.isFocused = false
            }
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            closeScreen()
            return
        }
        val fields = visibleTextFields
        if (keyCode == Keyboard.KEY_TAB && fields.isNotEmpty()) {
            DataFormControls.focusNextTextField(fields)
            return
        }

        var handled = false
        fields.filter { it.isFocused }.forEach { field ->
            handled = field.textboxKeyTyped(character, keyCode) || handled
        }
        if (!handled) {
            super.keyTyped(character, keyCode)
        } else {
            validationError = null
        }
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val wheel = Mouse.getEventDWheel()
        if (wheel == 0 || maxScrollOffset == 0) {
            return
        }
        syncComponents()
        val delta = if (wheel > 0) -DataFormMetrics.SCROLL_STEP else DataFormMetrics.SCROLL_STEP
        scrollOffset = (scrollOffset + delta).coerceIn(0, maxScrollOffset)
        rebuildControls()
    }

    override fun updateScreen() {
        super.updateScreen()
        visibleTextFields.forEach(GuiTextField::updateCursorCounter)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        DataFormRenderer.drawBackground(renderState)
        super.drawScreen(mouseX, mouseY, partialTicks)
        DataFormRenderer.drawForeground(mc, fontRendererObj, renderState)
        scrollClip.withScissor(mc) {
            visibleTextFields.forEach(GuiTextField::drawTextBox)
        }
    }

    override fun onGuiClosed() {
        Keyboard.enableRepeatEvents(false)
        super.onGuiClosed()
    }

    override fun doesGuiPauseGame(): Boolean = false

    private fun isControlVisible(localY: Int, height: Int): Boolean =
        scrollClip.intersects(scrollClip.left, guiTop + localY, 1, height)

    private val scrollClip: DataFormClip
        get() = DataFormClip(
            guiLeft + DataFormMetrics.GRID_PADDING,
            guiTop + DataFormMetrics.LIST_TOP,
            guiLeft + xSize - DataFormMetrics.GRID_PADDING,
            guiTop + listBottom
        )

    private val listBottom: Int
        get() = ySize - DataFormMetrics.FOOTER_HEIGHT

    private val viewportHeight: Int
        get() = max(1, listBottom - DataFormMetrics.LIST_TOP)

    private val contentHeight: Int
        get() = formDefinition?.let { definition ->
            val columns = definition.columns.coerceAtLeast(1)
            val cellWidth = (xSize - DataFormMetrics.GRID_PADDING * 2) / columns
            DataFormGridLayout(definition, components, fontRendererObj, cellWidth).contentHeight
        } ?: 0

    private val maxScrollOffset: Int
        get() = max(0, contentHeight - viewportHeight)

    private val renderState: DataFormRenderState
        get() = DataFormRenderState(
            guiLeft = guiLeft,
            guiTop = guiTop,
            width = xSize,
            height = ySize,
            viewportHeight = viewportHeight,
            contentHeight = contentHeight,
            maxScrollOffset = maxScrollOffset,
            scrollOffset = scrollOffset,
            title = formDefinition?.title.orEmpty().ifEmpty { fallbackTitle },
            hasFields = formDefinition?.getFieldList()?.isNotEmpty() == true,
            labels = visibleLabels,
            clip = scrollClip,
            error = validationError
        )
}
