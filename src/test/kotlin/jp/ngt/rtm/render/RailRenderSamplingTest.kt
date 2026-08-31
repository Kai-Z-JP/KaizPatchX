package jp.ngt.rtm.render

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import jp.kaiz.kaizpatch.rtm.rail.util.RailChunkSectioner
import jp.kaiz.kaizpatch.rtm.rail.util.RailMapSection
import jp.ngt.rtm.rail.util.RailMapBasic
import jp.ngt.rtm.rail.util.RailPosition

class RailRenderSamplingTest : FunSpec({
    test("one and two block tails keep the logical rail render grid") {
        val cases = listOf(
            Triple(16, 33, listOf(0..31, 32..33)),
            Triple(17, 35, listOf(0..31, 32..35)),
        )

        cases.forEach { (endX, expectedSplit, expectedIndices) ->
            val source = straightRenderRail(endX)
            val plans = sectionPlans(source)

            plans.size shouldBe 2
            plans.map { it.split }.distinct() shouldBe listOf(expectedSplit)
            plans.map { it.indices.toList() } shouldBe expectedIndices.map { it.toList() }
            assertMatchesUnsplit(source, plans)
        }
    }

    test("curved sections reproduce every unsplit pose") {
        val start = RailPosition(0, 64, 0, 2).apply {
            anchorYaw = 35.0F
            anchorPitch = 4.0F
            anchorLengthHorizontal = 18.0F
            anchorLengthVertical = 12.0F
            cantEdge = 1.5F
            cantCenter = 4.0F
            cantRandom = 0.2F
        }
        val end = RailPosition(40, 68, 24, 6).apply {
            anchorYaw = -120.0F
            anchorPitch = -3.0F
            anchorLengthHorizontal = 16.0F
            anchorLengthVertical = 10.0F
            cantEdge = -2.0F
        }
        val source = RailMapBasic(start, end, RailMapBasic.fixRTMRailMapVersionCurrent)
        val plans = sectionPlans(source)

        (plans.size > 1) shouldBe true
        assertMatchesUnsplit(source, plans)
    }

    test("a section without a global sample stays empty without resetting later indices") {
        val source = straightRenderRail(20)
        val split = railRenderSplit(source.length)
        val firstEnd = 4.1 / split
        val lastStart = 4.9 / split
        val plans = listOf(
            manualPlan(source, 0.0, firstEnd),
            manualPlan(source, firstEnd, lastStart),
            manualPlan(source, lastStart, 1.0),
        )

        plans[0].indices.toList() shouldBe (0..4).toList()
        plans[1].indices.toList() shouldBe emptyList()
        plans[2].indices.toList() shouldBe (5..split).toList()
        plans.flatMap { it.indices.toList() } shouldBe (0..split).toList()
        plans.map { it.logicalSampleCount }.distinct() shouldBe listOf(split + 1)
    }

    test("a boundary immediately before the end does not duplicate the final sample") {
        val source = straightRenderRail(20)
        val boundary = 0.99999995
        val plans = listOf(
            manualPlan(source, 0.0, boundary),
            manualPlan(source, boundary, 1.0),
        )

        plans.flatMap { it.indices.toList() } shouldBe (0..railRenderSplit(source.length)).toList()
    }

    test("a regular rail keeps legacy split quantization and minimum split") {
        val start = RailPosition(0, 64, 0, 2)
        val end = RailPosition(0, 64, 0, 2).apply {
            setPosition(start.posX + 0.25, start.posY, start.posZ)
        }
        val source = RailMapBasic(start, end, RailMapBasic.fixRTMRailMapVersionCurrent)
        val legacyPlan = createRailRenderSamplePlan(source)
        val minimumPlan = createRailRenderSamplePlan(source, minimumSplit = 1)

        (legacyPlan.source === source) shouldBe true
        legacyPlan.isSectioned shouldBe false
        legacyPlan.split shouldBe 0
        legacyPlan.indices.toList() shouldBe listOf(0)
        minimumPlan.split shouldBe 1
        minimumPlan.indices.toList() shouldBe listOf(0, 1)
    }
})

private fun straightRenderRail(endX: Int): RailMapBasic = RailMapBasic(
    RailPosition(0, 64, 0, 2),
    RailPosition(endX, 64, 0, 6),
    RailMapBasic.fixRTMRailMapVersionCurrent,
)

private fun sectionPlans(source: RailMapBasic): List<RailRenderSamplePlan> =
    RailChunkSectioner.split(source).map { section ->
        createRailRenderSamplePlan(
            RailMapSection(source, section.startRP, section.endRP, section.startRatio, section.endRatio),
        )
    }

private fun manualPlan(source: RailMapBasic, startRatio: Double, endRatio: Double): RailRenderSamplePlan =
    createRailRenderSamplePlan(
        RailMapSection(source, source.startRP, source.endRP, startRatio, endRatio),
    )

private fun assertMatchesUnsplit(source: RailMapBasic, plans: List<RailRenderSamplePlan>) {
    val fullPlan = createRailRenderSamplePlan(source)
    val samples = plans.flatMap { plan -> plan.indices.map { plan to it } }

    samples.map { it.second } shouldBe fullPlan.indices.toList()
    plans.map { it.logicalSampleCount }.distinct() shouldBe listOf(fullPlan.logicalSampleCount)
    samples.forEach { (plan, index) ->
        val expectedPosition = source.getRailPos(fullPlan.split, index)
        val actualPosition = plan.getRailPos(index)
        actualPosition[0] shouldBe (expectedPosition[0] plusOrMinus 1.0E-7)
        actualPosition[1] shouldBe (expectedPosition[1] plusOrMinus 1.0E-7)
        plan.getRailHeight(index) shouldBe (source.getRailHeight(fullPlan.split, index) plusOrMinus 1.0E-7)
        plan.getRailYaw(index).toDouble() shouldBe
                (source.getRailYaw(fullPlan.split, index).toDouble() plusOrMinus 1.0E-5)
        plan.getRailPitch(index).toDouble() shouldBe
                (source.getRailPitch(fullPlan.split, index).toDouble() plusOrMinus 1.0E-5)
        plan.getRailRoll(index).toDouble() shouldBe
                (source.getRailRoll(fullPlan.split, index).toDouble() plusOrMinus 1.0E-5)
    }
}
