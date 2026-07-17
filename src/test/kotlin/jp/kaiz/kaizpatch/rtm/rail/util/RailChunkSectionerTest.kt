package jp.kaiz.kaizpatch.rtm.rail.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import jp.ngt.rtm.rail.util.RailMapBasic
import jp.ngt.rtm.rail.util.RailPosition
import kotlin.math.floor

class RailChunkSectionerTest : FunSpec({
    test("rail inside one chunk is not sectioned") {
        val map = straightRail(0, 10)

        val sections = RailChunkSectioner.split(map)

        sections.size shouldBe 1
        sections.single().startRatio shouldBe 0.0
        sections.single().endRatio shouldBe 1.0
    }

    test("straight rail creates one section per traversed chunk") {
        val map = straightRail(0, 40)

        val sections = RailChunkSectioner.split(map)

        sections.size shouldBe 3
        sections.map { it.startRP.blockX shr 4 } shouldBe listOf(0, 1, 2)
        assertContinuous(map, sections)
    }

    test("negative coordinates use Minecraft chunk flooring") {
        val map = straightRail(-40, 0)

        val sections = RailChunkSectioner.split(map)

        (sections.size >= 3) shouldBe true
        (sections.first().startRP.blockX shr 4) shouldBe -3
        (sections.last().startRP.blockX shr 4) shouldBe 0
        assertContinuous(map, sections)
    }

    test("diagonal corner crossing produces one boundary") {
        val map = RailMapBasic(
            RailPosition(0, 64, 0, 1),
            RailPosition(31, 64, 31, 5),
            RailMapBasic.fixRTMRailMapVersionCurrent,
        )

        val sections = RailChunkSectioner.split(map)

        sections.size shouldBe 2
        (sections[0].startRP.blockX shr 4) shouldBe 0
        (sections[0].startRP.blockZ shr 4) shouldBe 0
        (sections[1].startRP.blockX shr 4) shouldBe 1
        (sections[1].startRP.blockZ shr 4) shouldBe 1
        assertContinuous(map, sections)
    }

    test("sloped section keeps exact boundary height in offset") {
        val start = RailPosition(0, 64, 0, 2)
        val end = RailPosition(40, 72, 0, 6)
        val map = RailMapBasic(start, end, RailMapBasic.fixRTMRailMapVersionCurrent)

        val sections = RailChunkSectioner.split(map)

        (sections.size > 1) shouldBe true
        sections.drop(1).forEach { section ->
            section.startRP.posY shouldBe (map.getRailHeight(section.startRatio) plusOrMinus 1.0E-7)
            (section.startRP.offsetY != 0.0 || section.startRP.posY % 0.0625 == 0.0) shouldBe true
        }
        assertContinuous(map, sections)
    }

    test("curved sections delegate position, yaw, pitch, and cant to source") {
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
        val sections = RailChunkSectioner.split(source)

        (sections.size > 1) shouldBe true
        sections.forEach { section ->
            val map = RailMapSection(source, section.startRP, section.endRP, section.startRatio, section.endRatio)
            val localRatio = 0.37
            val sourceRatio = map.sourceRatio(localRatio)
            val split = 1000
            val index = (localRatio * split).toInt()
            val expected = source.getRailPos(sourceRatio)
            val actual = map.getRailPos(split, index)
            actual[0] shouldBe (expected[0] plusOrMinus 1.0E-7)
            actual[1] shouldBe (expected[1] plusOrMinus 1.0E-7)
            map.getRailHeight(split, index) shouldBe (source.getRailHeight(sourceRatio) plusOrMinus 1.0E-7)
            map.getRailYaw(split, index).toDouble() shouldBe
                    (source.getRailYaw(sourceRatio).toDouble() plusOrMinus 1.0E-5)
            map.getRailPitch(split, index).toDouble() shouldBe
                    (source.getRailPitch(sourceRatio).toDouble() plusOrMinus 1.0E-5)
            map.getRailRoll(split, index).toDouble() shouldBe
                    (source.getRailRoll(sourceRatio).toDouble() plusOrMinus 1.0E-5)
        }
        assertContinuous(source, sections)
    }
})

private fun straightRail(startX: Int, endX: Int): RailMapBasic {
    return RailMapBasic(
        RailPosition(startX, 64, 0, 2),
        RailPosition(endX, 64, 0, 6),
        RailMapBasic.fixRTMRailMapVersionCurrent,
    )
}

private fun assertContinuous(source: RailMapBasic, sections: List<RailSection>) {
    sections.forEachIndexed { index, section ->
        val sectionMap = RailMapSection(source, section.startRP, section.endRP, section.startRatio, section.endRatio)
        val start = sectionMap.getRailPos(100, 0)
        val end = sectionMap.getRailPos(100, 100)
        val expectedStart = source.getRailPos(section.startRatio)
        val expectedEnd = source.getRailPos(section.endRatio)
        start[0] shouldBe (expectedStart[0] plusOrMinus 1.0E-7)
        start[1] shouldBe (expectedStart[1] plusOrMinus 1.0E-7)
        end[0] shouldBe (expectedEnd[0] plusOrMinus 1.0E-7)
        end[1] shouldBe (expectedEnd[1] plusOrMinus 1.0E-7)
        if (index + 1 < sections.size) {
            sections[index + 1].startRatio shouldBe (section.endRatio plusOrMinus 1.0E-12)
            sections[index + 1].startRP.posX shouldBe (section.endRP.posX plusOrMinus 1.0E-7)
            sections[index + 1].startRP.posY shouldBe (section.endRP.posY plusOrMinus 1.0E-7)
            sections[index + 1].startRP.posZ shouldBe (section.endRP.posZ plusOrMinus 1.0E-7)
            floor(sections[index + 1].startRP.posX) shouldBe floor(section.endRP.posX)
            section.endRP.direction.toInt() shouldBe
                    ((sections[index + 1].startRP.direction.toInt() + 4) and 7)
        }
    }
}
