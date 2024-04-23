package com.example.motionviewapp.motionviews.widget.entity

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.example.motionviewapp.motionviews.model.Layer
import kotlin.math.min


class ImageEntity(
    layer: Layer,
    var bitmap: Bitmap,
    val resId: Int,
    canvasWidth: Int,
    canvasHeight: Int,
) : MotionEntity(layer, canvasWidth, canvasHeight) {

    override val bmWidth = bitmap.width
    override val bmHeight = bitmap.height

    init {
//        bitmapWidth = bitmap.width.toFloat()
//        val height = bitmap.height.toFloat()
        val widthAspect = 1.0f * canvasWidth / bmWidth
        val heightAspect = 1.0f * canvasHeight / bmHeight
        holyScale = min(widthAspect.toDouble(), heightAspect.toDouble()).toFloat()
//        layer.initialScale = 1f * initialWidth / canvasWidth
        layer.maxScale = layer.initialScale * Layer.MAX_SCALE_RATIO_TIMES

        srcPoints[0] = 0f
        srcPoints[1] = 0f
        srcPoints[2] = bmWidth.toFloat()
        srcPoints[3] = 0f
        srcPoints[4] = bmWidth.toFloat()
        srcPoints[5] = bmHeight.toFloat()
        srcPoints[6] = 0f
        srcPoints[7] = bmHeight.toFloat()
        srcPoints[8] = 0f
    }

    override fun drawContent(canvas: Canvas, drawingPaint: Paint?) {
        bitmap.let {
            if (drawingPaint == null) {
                val paint = Paint().apply {
                    isAntiAlias = true
                    isDither = true
                    isFilterBitmap = true
                }
                canvas.drawBitmap(it, matrix, paint)
            } else canvas.drawBitmap(it, matrix, drawingPaint)
        }
    }

    override fun clone(): MotionEntity {
        return ImageEntity(layer.clone(), bitmap, resId, canvasWidth, canvasHeight)
    }

    override fun release() {
        bitmap.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
    }

    companion object {
        const val STICKER_WIDTH = 800
        const val STICKER_HEIGHT = 800
        const val MAX_SIZE_TO_SAVE = 2500f
    }
}