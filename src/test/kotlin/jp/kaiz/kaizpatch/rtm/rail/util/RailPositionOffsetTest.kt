package jp.kaiz.kaizpatch.rtm.rail.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import jp.ngt.rtm.rail.util.RailPosition
import net.minecraft.nbt.NBTTagCompound

class RailPositionOffsetTest : FunSpec({
    test("legacy position is unchanged without offset tags") {
        val legacy = NBTTagCompound().apply {
            setIntArray("BlockPos", intArrayOf(10, 64, -5))
            setByte("Direction", 2)
            setByte("SwitchType", 0)
            setByte("Height", 3)
        }

        val position = RailPosition.readFromNBT(legacy)

        position.posX shouldBe 10.0
        position.posY shouldBe 64.25
        position.posZ shouldBe -4.5
        position.offsetX shouldBe 0.0
        position.offsetY shouldBe 0.0
        position.offsetZ shouldBe 0.0
    }

    test("arbitrary world position round-trips through NBT") {
        val position = RailPosition(15, 70, -17, 6)
        position.setPosition(16.0, 70.375, -16.25)

        val saved = position.writeToNBT()
        val restored = RailPosition.readFromNBT(saved)

        restored.posX shouldBe (16.0 plusOrMinus 1.0E-12)
        restored.posY shouldBe (70.375 plusOrMinus 1.0E-12)
        restored.posZ shouldBe (-16.25 plusOrMinus 1.0E-12)
        (restored.offsetX == 0.0 && restored.offsetY == 0.0 && restored.offsetZ == 0.0) shouldBe false
    }

    test("moving position keeps its relative offset") {
        val position = RailPosition(15, 70, 0, 6)
        position.setPosition(16.0, 70.2, 0.75)
        val offset = doubleArrayOf(position.offsetX, position.offsetY, position.offsetZ)

        position.movePos(-32, 4, 16)

        position.posX shouldBe (-16.0 plusOrMinus 1.0E-12)
        position.posY shouldBe (74.2 plusOrMinus 1.0E-12)
        position.posZ shouldBe (16.75 plusOrMinus 1.0E-12)
        position.offsetX shouldBe offset[0]
        position.offsetY shouldBe offset[1]
        position.offsetZ shouldBe offset[2]
    }

    test("height changes keep offsets and refresh world position") {
        val position = RailPosition(3, 64, 7, 0)
        position.setPosition(3.75, 64.4, 7.25)
        val offsets = doubleArrayOf(position.offsetX, position.offsetY, position.offsetZ)

        position.setHeight(4)
        position.offsetX shouldBe offsets[0]
        position.offsetY shouldBe offsets[1]
        position.offsetZ shouldBe offsets[2]
        position.posY shouldBe (64.65 plusOrMinus 1.0E-12)

        position.addHeight(0.125)
        position.offsetY shouldBe offsets[1]
        position.posY shouldBe (64.775 plusOrMinus 1.0E-12)
    }
})
