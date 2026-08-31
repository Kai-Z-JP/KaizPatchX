package jp.ngt.rtm.render

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import jp.kaiz.kaizpatch.rtm.rail.util.RailChunkSectioner
import jp.kaiz.kaizpatch.rtm.rail.util.RailMapSection
import jp.ngt.rtm.rail.util.RailMapBasic
import jp.ngt.rtm.rail.util.RailPosition

class RailPartsRendererSamplingTest : FunSpec({
    test("renderer geometry uses logical indices and source poses") {
        listOf(16, 17).forEach { endX ->
            val source = RailMapBasic(
                RailPosition(0, 64, 0, 2),
                RailPosition(endX, 64, 0, 6),
                RailMapBasic.fixRTMRailMapVersionCurrent,
            )
            val sections = RailChunkSectioner.split(source)
            val geometries = sections.map { section ->
                val map = RailMapSection(
                    source, section.startRP, section.endRP, section.startRatio, section.endRatio,
                )
                section to createRailRenderGeometry(
                    map,
                    section.startRP.posX,
                    section.startRP.posZ,
                    minimumSplit = 1,
                    endOffset = 1,
                )
            }
            val split = railRenderSplit(source.length)

            geometries.flatMap { it.second.logicalIndices.toList() } shouldBe (0..split).toList()
            geometries.map { it.second.logicalSampleCount }.distinct() shouldBe listOf(split + 1)
            geometries.forEach { (section, geometry) ->
                geometry.logicalIndices.forEachIndexed { localIndex, logicalIndex ->
                    val position = geometry.positions[localIndex]
                    val expected = source.getRailPos(split, logicalIndex)
                    val worldX = section.startRP.posX + position[0]
                    val worldY = section.startRP.posY - 0.0625 + position[1]
                    val worldZ = section.startRP.posZ + position[2]

                    worldX shouldBe (expected[1] plusOrMinus 1.0E-6)
                    worldY shouldBe (source.getRailHeight(split, logicalIndex) - 0.0625 plusOrMinus 1.0E-6)
                    worldZ shouldBe (expected[0] plusOrMinus 1.0E-6)
                    position[3].toDouble() shouldBe
                            (source.getRailYaw(split, logicalIndex).toDouble() plusOrMinus 1.0E-5)
                    position[4].toDouble() shouldBe
                            (-source.getRailPitch(split, logicalIndex).toDouble() plusOrMinus 1.0E-5)
                    position[5].toDouble() shouldBe
                            (source.getRailRoll(split, logicalIndex).toDouble() plusOrMinus 1.0E-5)
                }
            }
        }
    }

    test("an empty section geometry is a valid cached render result") {
        val source = RailMapBasic(
            RailPosition(0, 64, 0, 2),
            RailPosition(20, 64, 0, 6),
            RailMapBasic.fixRTMRailMapVersionCurrent,
        )
        val split = railRenderSplit(source.length)
        val map = RailMapSection(source, source.startRP, source.endRP, 4.1 / split, 4.9 / split)
        val geometry = createRailRenderGeometry(
            map, source.startRP.posX, source.startRP.posZ, minimumSplit = 1, endOffset = 1,
        )

        geometry.positions.size shouldBe 0
        geometry.logicalIndices.toList() shouldBe emptyList()
        geometry.logicalSampleCount shouldBe split + 1
        geometry.allowsEmpty shouldBe true
        isRailRenderGeometryValid(geometry, minimumSplit = 1) shouldBe true
    }

    test("geometry keeps the global script index phase") {
        val source = RailMapBasic(
            RailPosition(0, 64, 0, 2),
            RailPosition(20, 64, 0, 6),
            RailMapBasic.fixRTMRailMapVersionCurrent,
        )
        val split = railRenderSplit(source.length)
        val map = RailMapSection(source, source.startRP, source.endRP, 14.2 / split, 18.2 / split)
        val geometry = createRailRenderGeometry(map, source.startRP.posX, source.startRP.posZ, minimumSplit = 1)

        geometry.logicalIndices.toList() shouldBe (15..18).toList()
        geometry.logicalSampleCount shouldBe split + 1
        geometry.logicalIndices.filter { it % 4 == 0 } shouldBe listOf(16)
    }
})
