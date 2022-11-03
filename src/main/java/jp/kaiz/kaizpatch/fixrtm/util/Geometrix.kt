/// Copyright (c) 2022 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package jp.kaiz.kaizpatch.fixrtm.util

import kotlin.math.sqrt

data class Vec2d(val x: Double, val y: Double) {
    private val normSquared get() = this dot this
    private val norm get() = sqrt(normSquared)

    operator fun minus(other: Vec2d): Vec2d = Vec2d(x - other.x, y - other.y)
    operator fun plus(other: Vec2d): Vec2d = Vec2d(x + other.x, y + other.y)
    infix fun dot(other: Vec2d): Double = x * other.x + y * other.y


    infix fun distanceTo(other: Vec2d) = (this - other).norm
}

fun crossStraightLineAndLineSegments(
    straight1: Vec2d,
    straight2: Vec2d,
    segment1: Vec2d,
    segment2: Vec2d,
): Boolean {
    val straightVec = straight1 - straight2
    return (straightVec dot (segment1 - straight2)) * (straightVec dot (segment2 - straight2)) < 0
}

fun crossLineSegments(
    line11: Vec2d,
    line12: Vec2d,
    line21: Vec2d,
    line22: Vec2d,
): Boolean {
    return crossStraightLineAndLineSegments(line11, line12, line21, line22)
            && crossStraightLineAndLineSegments(line21, line22, line11, line12)
}