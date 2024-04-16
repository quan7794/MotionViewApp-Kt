package com.example.motionviewapp.motionviews.widget.entity

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.NonNull
import com.example.motionviewapp.motionviews.model.Layer

class ImageEntity(
    layer: Layer,
    @NonNull var bitmap: Bitmap,
    val resId: Int,
    canvasWidth: Int,
    canvasHeight: Int,
    private val initialWidth: Int = canvasWidth/3,
    private val initialHeight: Int = canvasHeight/3,
) : MotionEntity(layer, canvasWidth, canvasHeight) {

    init {
        bitmapWidth = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val widthAspect = 1f * canvasWidth / bitmapWidth

        holyScale = widthAspect

        layer.initialScale = 1f * initialWidth / canvasWidth
        layer.maxScale = layer.initialScale * Layer.MAX_SCALE_RATIO_TIMES

        srcPoints[0] = 0f
        srcPoints[1] = 0f
        srcPoints[2] = bitmapWidth
        srcPoints[3] = 0f
        srcPoints[4] = bitmapWidth
        srcPoints[5] = height
        srcPoints[6] = 0f
        srcPoints[7] = height
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

    override val width: Int
        get() = bitmap.width
    override val height: Int
        get() = bitmap.height

    override fun clone(): MotionEntity {
        return ImageEntity(layer.clone(), bitmap, resId, canvasWidth, canvasHeight, initialWidth, initialHeight)
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