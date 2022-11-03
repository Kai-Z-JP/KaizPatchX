/// Copyright (c) 2022 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package jp.kaiz.kaizpatch.fixrtm.rtm.rail.util

import jp.kaiz.kaizpatch.fixrtm.util.Vec2d
import jp.kaiz.kaizpatch.fixrtm.util.crossLineSegments
import jp.ngt.rtm.rail.util.*
import kotlin.math.abs

class SwitchTypeSingleCrossFixRTMV1(fixRTMRailMapVersion: Int) : SwitchType.SwitchSingleCross(fixRTMRailMapVersion) {
    init {
        // this is only for version 1...
        assert(fixRTMRailMapVersion >= 1)
    }

    override fun init(switchList: List<RailPosition>, normalList: List<RailPosition>): Boolean {
        check(switchList.size == 2)
        check(normalList.size == 2)

        class RailPosInfo(val railPos: RailPosition) {
            val xzPos = Vec2d(railPos.posX, railPos.posZ)
        }

        val switch0 = RailPosInfo(switchList[0])
        val switch1 = RailPosInfo(switchList[1])

        val normal0 = RailPosInfo(normalList[0])
        val normal1 = RailPosInfo(normalList[1])

        val normalPairs = mutableListOf(
            normal0 to normal1,
            normal1 to normal0,
        )

        // first, check the lines will never cross
        // in the case the lines cross, switch - switch line is a edge (expected to be diagonal)
        // so return false
        for ((normalA, normalB) in normalPairs) {
            if (crossLineSegments(switch0.xzPos, normalA.xzPos, switch1.xzPos, normalB.xzPos)) {
                return false
            }
        }

        // find better switch
        fun lineScore(normalA: RailPosInfo, normalB: RailPosInfo): Double {
            var score = 0.0
            // initial score: sum of distance
            score += (switch0.xzPos distanceTo normalA.xzPos)
            score += (switch1.xzPos distanceTo normalB.xzPos)
            // if line maker is opposite direction, that can be better. add 10
            if (abs(switch0.railPos.direction - normalA.railPos.direction) == 4)
                score += 10
            if (abs(switch1.railPos.direction - normalB.railPos.direction) == 4)
                score += 10
            return score
        }
        normalPairs.sortByDescending { (a, b) -> lineScore(a, b) }

        // best pair found.
        val (normalA, normalB) = normalPairs[0]

        val directionLine0 = switch0.railPos.getDir(switch1.railPos, normalA.railPos)
        val rmsLine0 = RailMapSwitch(switch0.railPos, normalA.railPos, directionLine0.invert(), RailDir.NONE, 1)

        val directionLine1 = switch1.railPos.getDir(switch0.railPos, normalB.railPos)
        val rmsLine1 = RailMapSwitch(switch1.railPos, normalB.railPos, directionLine1.invert(), RailDir.NONE, 1)

        // assertion: directionLine0 or directionLine1 cannot be NONE
        if (directionLine0 == RailDir.NONE || directionLine1 == RailDir.NONE)
            return false

        val rmsSlashLine = RailMapSwitch(switch0.railPos, switch1.railPos, directionLine0, directionLine1, 1)

        railMaps = arrayOf(rmsLine0, rmsLine1, rmsSlashLine)
        points = arrayOf(
            Point(switchList[0], rmsLine0, rmsSlashLine),
            Point(switchList[1], rmsLine1, rmsSlashLine),
            Point(rmsLine0.endRP, rmsLine0),
            Point(rmsLine1.endRP, rmsLine1),
        )
        return true
    }

    override fun getName(): String {
        return "CrossoverFixRTMv1"
    }
}
