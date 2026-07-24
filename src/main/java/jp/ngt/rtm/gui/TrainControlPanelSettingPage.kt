package jp.ngt.rtm.gui

import jp.kaiz.kaizpatch.gui.GuiButtonWithScrollingListBox
import jp.ngt.rtm.entity.train.util.TrainState
import jp.ngt.rtm.entity.train.util.TrainState.TrainStateType
import jp.ngt.rtm.modelpack.cfg.TrainConfig
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBaseClient
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.resources.I18n

class TrainControlPanelSettingPage(gui: GuiTrainControlPanel) : TrainControlPanelPage(gui) {
    private var buttonChunkLoader: GuiButton? = null
    private val buttonDirection = arrayOfNulls<GuiButton>(3)

    override fun init() {
        val train = gui.panelTrain
        val modelset = gui.panelModelSet

        val interiorLight = TrainStateType.State_InteriorLight
        gui.addPanelButton(
            GuiButton(
                124,
                gui.panelLeft + 4,
                gui.panelTop + 4,
                82,
                20,
                gui.getFormattedText(interiorLight.id, train.getTrainStateData(interiorLight.id))
            )
        )
        gui.addPanelButton(
            GuiButton(
                125,
                gui.panelLeft + 90,
                gui.panelTop + 4,
                82,
                20,
                gui.getFormattedText(5, train.getTrainStateData(5))
            )
        )
        gui.addPanelButton(
            GuiButton(
                126,
                gui.panelLeft + 4,
                gui.panelTop + 28,
                82,
                20,
                gui.getFormattedText(6, train.getTrainStateData(6))
            )
        )

        val direction = train.getTrainStateData(TrainStateType.State_Direction.id).toInt()
        for (i in 0 until 3) {
            val button = GuiButton(
                140 + i,
                gui.panelLeft + 91 + 27 * i,
                gui.panelTop + 28,
                27,
                20,
                gui.getFormattedText(TrainStateType.State_Direction.id, i.toByte())
            )
            button.enabled = i != direction
            buttonDirection[i] = button
            gui.addPanelButton(button)
        }

        buttonChunkLoader = GuiButton(
            127,
            gui.panelLeft + 28,
            gui.panelTop + 52,
            120,
            20,
            gui.getFormattedText(7, train.getTrainStateData(7))
        )
        gui.addPanelButton(buttonChunkLoader)
        gui.addPanelButton(GuiButton(110, gui.panelLeft + 4, gui.panelTop + 52, 20, 20, "<"))
        gui.addPanelButton(GuiButton(111, gui.panelLeft + 152, gui.panelTop + 52, 20, 20, ">"))

        if ((modelset as ModelSetVehicleBaseClient<TrainConfig>).rollsignTexture != null) {
            gui.addPanelButton(
                GuiButtonWithScrollingListBox(
                    128,
                    gui.panelLeft + 28,
                    gui.panelTop + 76,
                    120,
                    20,
                    { train.getTrainStateData(8).toInt() },
                    modelset.config.rollsignNames.asList(),
                    I18n.format("state.destination") + " %s"
                ) {
                    gui.sendTrainState(8, toByte())
                }
            )
            gui.addPanelButton(GuiButton(112, gui.panelLeft + 4, gui.panelTop + 76, 20, 20, "<"))
            gui.addPanelButton(GuiButton(113, gui.panelLeft + 152, gui.panelTop + 76, 20, 20, ">"))
        }

        if (modelset.config.sound_Announcement != null) {
            gui.addPanelButton(
                GuiButtonWithScrollingListBox(
                    129,
                    gui.panelLeft + 28,
                    gui.panelTop + 100,
                    120,
                    20,
                    { train.getTrainStateData(9).toInt() },
                    modelset.config.sound_Announcement.map { it[0] },
                    I18n.format("state.announcement") + " %s"
                ) {
                    gui.sendTrainState(9, toByte())
                }
            )
            gui.addPanelButton(GuiButton(114, gui.panelLeft + 4, gui.panelTop + 100, 20, 20, "<"))
            gui.addPanelButton(GuiButton(115, gui.panelLeft + 152, gui.panelTop + 100, 20, 20, ">"))
        }
    }

    override fun actionPerformed(button: GuiButton): Boolean {
        if ((button.id !in 110..115) && (button.id !in 124..129) && (button.id !in 140..142)) {
            return false
        }
        if (button.id == 128 || button.id == 129) {
            return true
        }

        val train = gui.panelTrain
        val modelset = gui.panelModelSet
        val stateId: Int
        var data: Int

        when (button.id) {
            110 -> {
                stateId = 7
                data = train.getTrainStateData(stateId) - 1
            }

            111 -> {
                stateId = 7
                data = train.getTrainStateData(stateId) + 1
            }

            112 -> {
                stateId = 8
                data = train.getTrainStateData(stateId) - 1
                if (data < 0) {
                    data = modelset.config.rollsignNames.size - 1
                }
            }

            113 -> {
                stateId = 8
                data = train.getTrainStateData(stateId) + 1
                if (data >= modelset.config.rollsignNames.size) {
                    data = 0
                }
            }

            114 -> {
                val announce = modelset.config.sound_Announcement
                stateId = 9
                data = train.getTrainStateData(stateId) - 1
                if (announce != null && data < 0) {
                    data = announce.size - 1
                }
            }

            115 -> {
                val announce = modelset.config.sound_Announcement
                stateId = 9
                data = train.getTrainStateData(stateId) + 1
                if (announce != null && data >= announce.size) {
                    data = 0
                }
            }

            in 124..129 -> {
                stateId = if (button.id == 124) TrainStateType.State_InteriorLight.id else button.id - 120
                data = train.getTrainStateData(stateId) + 1
            }

            else -> {
                stateId = TrainStateType.State_Direction.id
                data = button.id - 140
                for (i in 0 until 3) {
                    buttonDirection[i]?.enabled = i != data
                }
            }
        }

        val stateType = TrainState.getStateType(stateId)
        val wrappedData = if (data < stateType.min) stateType.max else if (data > stateType.max) stateType.min else data
        gui.sendTrainState(stateId, wrappedData.toByte())

        if (button.id == 110 || button.id == 111) {
            buttonChunkLoader?.displayString = gui.getFormattedText(stateId, wrappedData.toByte())
        } else if (button.id != 112 && button.id != 113 && button.id != 114 && button.id != 115) {
            button.displayString = gui.getFormattedText(stateId, wrappedData.toByte())
        }
        return true
    }

    override fun handleMouseWheel(delta: Int): Boolean {
        val step = if (delta > 0) -1 else 1
        val button = gui.panelButtons.firstOrNull { it.func_146115_a() && it.id in 124..129 } ?: return false

        var stateId = button.id - 120
        if (button.id == 124) {
            stateId = TrainStateType.State_InteriorLight.id
        }
        val prevData = gui.panelTrain.getTrainStateData(stateId)
        var data = prevData + step

        val stateType = TrainState.getStateType(stateId)
        val min: Int
        val max: Int
        if (stateType == TrainStateType.State_Destination) {
            val rollSignNames = gui.panelModelSet.config.rollsignNames
            min = 0
            max = if (rollSignNames != null) rollSignNames.size - 1 else 0
        } else if (stateType == TrainStateType.State_Announcement) {
            val announce = gui.panelModelSet.config.sound_Announcement
            min = 0
            max = if (announce != null) announce.size - 1 else 0
        } else {
            min = stateType.min.toInt()
            max = stateType.max.toInt()
        }

        data = if (data < min) max else if (data > max) min else data
        if (prevData.toInt() != data) {
            gui.sendTrainState(stateId, data.toByte())
            button.func_146113_a(gui.panelMinecraft.soundHandler)
            button.displayString = gui.getFormattedText(stateId, data.toByte())
        }
        return true
    }
}
