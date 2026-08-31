package jp.ngt.rtm.render

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.EmptyChunk

class RailBrightnessSamplingTest : FunSpec({
    test("fractional render origin selects the block across a chunk boundary") {
        RailPartsRenderer.getRailBrightnessCoordinate(15.75, 0.5F) shouldBe 16
        RailPartsRenderer.getRailBrightnessCoordinate(-16.25, 0.5F) shouldBe -16
    }

    test("an unreceived empty chunk is never ready for brightness sampling") {
        val chunk = EmptyChunk(null, 0, 0).apply {
            isChunkLoaded = true
            isLightPopulated = true
        }

        RailPartsRenderer.isRailBrightnessChunkReady(chunk) shouldBe false
    }

    test("a received chunk becomes ready only after its lighting data is populated") {
        val chunk = Chunk(null, 0, 0).apply {
            isChunkLoaded = true
        }

        RailPartsRenderer.isRailBrightnessChunkReady(chunk) shouldBe false

        chunk.isLightPopulated = true
        RailPartsRenderer.isRailBrightnessChunkReady(chunk) shouldBe true

        chunk.isChunkLoaded = false
        RailPartsRenderer.isRailBrightnessChunkReady(chunk) shouldBe false
    }
})
