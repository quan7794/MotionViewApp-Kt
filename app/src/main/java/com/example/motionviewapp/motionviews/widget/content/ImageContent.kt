package com.example.motionviewapp.motionviews.widget.content

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.example.motionviewapp.motionviews.model.Layer
import kotlin.math.min


class ImageContent(
    layer: Layer,
    var bitmap: Bitmap,
    val resId: Int,
    canvasWidth: Int,
    canvasHeight: Int,
) : BaseContent(layer, canvasWidth, canvasHeight) {

    override var bmWidth = bitmap.width
    override var bmHeight = bitmap.height

    init {
        initInfo()
    }

    fun initInfo() {
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

    override fun clone(): BaseContent {
        return ImageContent(layer.clone(), bitmap, resId, canvasWidth, canvasHeight)
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