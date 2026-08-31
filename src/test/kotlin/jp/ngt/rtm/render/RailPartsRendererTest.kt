package jp.ngt.rtm.render

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import jp.ngt.rtm.rail.util.RailPosition

class RailPartsRendererTest : FunSpec({
    test("script-facing render origin remains marker-local") {
        val firstMarker = RailPosition(80, 64, -12, 0).apply {
            setPosition(80.25, 64.1875, -12.75)
        }

        val origin = RailPartsRenderer.getRailRenderOrigin(firstMarker)

        origin[0] shouldBe (0.25 plusOrMinus 1.0E-12)
        origin[1] shouldBe (0.125 plusOrMinus 1.0E-12)
        origin[2] shouldBe (-0.75 plusOrMinus 1.0E-12)
    }

    test("Java geometry origin composes without a marker-order offset") {
        val firstMarker = RailPosition(80, 64, -12, 0)
        val worldPoint = doubleArrayOf(96.5, -24.25)

        val renderOrigin = RailPartsRenderer.getRailRenderOrigin(firstMarker)
        val geometryOrigin = RailPartsRenderer.getRailWorldRenderOrigin(firstMarker, 120, -40)
        val localX = worldPoint[0] - geometryOrigin[0]
        val localZ = worldPoint[1] - geometryOrigin[1]

        renderOrigin[0] + localX shouldBe (worldPoint[0] - 120 plusOrMinus 1.0E-12)
        renderOrigin[2] + localZ shouldBe (worldPoint[1] + 40 plusOrMinus 1.0E-12)
    }

    test("Java geometry origin preserves section endpoint offsets") {
        val sectionStart = RailPosition(120, 64, -40, 0).apply {
            setPosition(120.875, 64.0625, -39.625)
        }

        val geometryOrigin = RailPartsRenderer.getRailWorldRenderOrigin(sectionStart, 120, -40)

        geometryOrigin[0] shouldBe (sectionStart.posX plusOrMinus 1.0E-12)
        geometryOrigin[1] shouldBe (sectionStart.posZ plusOrMinus 1.0E-12)
    }
})
