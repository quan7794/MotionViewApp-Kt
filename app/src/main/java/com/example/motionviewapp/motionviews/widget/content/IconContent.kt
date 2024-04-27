package com.example.motionviewapp.motionviews.widget.content

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import androidx.annotation.IntDef

class IconContent(val bitmapIcon: Bitmap, @Gravity val gravity: Int) {
    @IntDef(LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Gravity

    val width = bitmapIcon.width * 1.0f
    val height = bitmapIcon.height * 1.0f
    val radius = bitmapIcon.width / 2f

    val matrix = Matrix()
    val destPoints = FloatArray(10) // x0, y0, x1, y1, x2, y2, x3, y3, x0, y0
    val srcPoints = FloatArray(10) // x0, y0, x1, y1, x2, y2, x3, y3, x0, y0

    val pA = PointF()
    val pB = PointF()
    val pC = PointF()
    val pD = PointF()

    init {
        // initial position of the entity
        srcPoints[0] = 0f
        srcPoints[1] = 0f
        srcPoints[2] = width
        srcPoints[3] = 0f
        srcPoints[4] = width
        srcPoints[5] = height
        srcPoints[6] = 0f
        srcPoints[7] = height
        srcPoints[8] = 0f
        srcPoints[9] = 0f
    }

    companion object {
        const val LEFT_TOP = 0
        const val RIGHT_TOP = 1
        const val LEFT_BOTTOM = 2
        const val RIGHT_BOTTOM = 3
    }
}