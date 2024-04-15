package com.example.motionviewapp.utils

import android.content.Context
import android.graphics.PointF
import android.util.TypedValue
import com.example.motionviewapp.motionviews.widget.entity.MotionEntity

object MathUtils {


    fun pointInTriangle(pt: PointF, v1: PointF, v2: PointF, v3: PointF): Boolean {
        val b1 = crossProduct(pt, v1, v2) < 0.0f
        val b2 = crossProduct(pt, v2, v3) < 0.0f
        val b3 = crossProduct(pt, v3, v1) < 0.0f
        return b1 == b2 && b2 == b3
    }

    private fun crossProduct(a: PointF, b: PointF, c: PointF): Float {
        return (a.x - c.x) * (b.y - c.y) - (b.x - c.x) * (a.y - c.y)
    }

    //y = ax + b and x = c
    fun calculateIntersectionBetweenMainLineAndHorizontalAxis(slopeMainLine: Float, bounceMainLine: Float, px: Float): PointF {
        val x = px
        val y = slopeMainLine * x + bounceMainLine
        return PointF(x, y)
    }

    fun checkIntersection(pointOne: PointF, pointTwo: PointF, intersection: PointF, pointOneInLayer: PointF, pointTwoInLayer: PointF): Boolean {
        if (checkPointInRange(pointOne, pointTwo, intersection)) {
            if (checkPointInRange(pointOneInLayer, pointTwoInLayer, intersection))
                return true
        }
        return false
    }

    private fun checkPointInRange(pointOne: PointF, pointTwo: PointF, intersection: PointF): Boolean {
        if (pointOne.y < pointTwo.y) {
            if (intersection.y >= pointOne.y && intersection.y <= pointTwo.y)
                if (pointOne.x < pointTwo.x) {
                    if (intersection.x >= pointOne.x && intersection.x <= pointTwo.x)
                        return true
                } else {
                    if (intersection.x >= pointTwo.x && intersection.x <= pointOne.x)
                        return true
                }
        } else {
            if (intersection.y >= pointTwo.y && intersection.y <= pointOne.y)
                if (pointOne.x < pointTwo.x) {
                    if (intersection.x >= pointOne.x && intersection.x <= pointTwo.x)
                        return true
                } else {
                    if (intersection.x >= pointTwo.x && intersection.x <= pointOne.x)
                        return true
                }
        }
        return false
    }

    fun calculateSlope(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        if (x1 - x2 == 0f) return 1f
        if (y1 - y2 == 0f) return 0f
        return (y1 - y2) / (x1 - x2)
    }

    fun calculateAngledBounce(a: Float, x1: Float, y1: Float, x2: Float): Float {
        if (x1 - x2 == 0f) return -x1
        if (x1 == 0f) return y1
        return y1 - a * x1
    }

    fun calculateIntersectionBetweenMainLine(a1: Float, b1: Float, a2: Float, b2: Float): PointF {
        val x = (b2 - b1) / (a1 - a2)
        val y = a1 * x + b1
        return PointF(x, y)
    }

    //y = ax + b and y = c
    fun calculateIntersectionBetweenMainLineAndVerticalAxis(slopeMainLine: Float, bounceMainLine: Float, py: Float): PointF {
        val y = py
        val x = (y - bounceMainLine) / slopeMainLine
        return PointF(x, y)
    }

    /**
     * check distance between two points:
     * @param pointOne previous point
     * @param pointTwo current point
     * @return the distance between two points is normal or not
     */
    fun checkValidMovement(pointOne: PointF, pointTwo: PointF): Boolean {
        val normalDistanceRatio = 0f..MotionEntity.RATIO_POINTER
        if (pointOne.x >= pointTwo.x) {
            if (pointOne.y >= pointTwo.y) {
                if (pointOne.x / pointTwo.x in normalDistanceRatio && pointOne.y / pointTwo.y in normalDistanceRatio)
                    return true
            } else {
                if (pointOne.x / pointTwo.x in normalDistanceRatio && pointTwo.y / pointOne.y in normalDistanceRatio)
                    return true
            }
        } else {
            if (pointOne.y >= pointTwo.y) {
                if (pointTwo.x / pointOne.x in normalDistanceRatio && pointOne.y / pointTwo.y in normalDistanceRatio)
                    return true
            } else {
                if (pointTwo.x / pointOne.x in normalDistanceRatio && pointTwo.y / pointOne.y in normalDistanceRatio)
                    return true
            }
        }
        return false
    }

    fun convertToPositivePoint(point: PointF): PointF {
        return PointF(Math.abs(point.x), Math.abs(point.y))
    }
}