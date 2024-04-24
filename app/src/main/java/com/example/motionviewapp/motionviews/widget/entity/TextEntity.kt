package com.example.motionviewapp.motionviews.widget.entity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.text.Layout
import android.text.Spannable
import android.text.StaticLayout
import android.text.TextPaint
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.EmojiSpan
import com.example.motionviewapp.motionviews.model.TextLayer
import com.example.motionviewapp.utils.FontProvider
import kotlin.math.min

class TextEntity(
    layer: TextLayer,
    canvasWidth: Int,
    canvasHeight: Int,
    private val fontProvider: FontProvider
) : MotionEntity(layer, canvasWidth, canvasHeight) {

    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var bitmap: Bitmap? = null
    private var isOneCharacter = false

    var textLayer = layer

    init {
        updateEntity()
    }

    override val bmWidth: Int = bitmap?.width ?: 0
    override val bmHeight: Int = bitmap?.height ?: 0

    override fun drawContent(canvas: Canvas, drawingPaint: Paint?) {
        bitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, matrix, drawingPaint)
        }
    }

    override fun clone(): MotionEntity {
        val cloneTextLayer = textLayer.cloneTextLayer()
        cloneTextLayer.apply {
            x = textLayer.x
            y = textLayer.y
            rotationInDegrees = textLayer.rotationInDegrees
            scale = textLayer.scale

            initialScale = textLayer.initialScale
            maxScale = textLayer.maxScale
            minScale = textLayer.minScale
            isEditing = textLayer.isEditing
        }

        return TextEntity(cloneTextLayer, canvasWidth, canvasHeight, fontProvider)
    }

    override fun release() {
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
    }

    fun updateEntity() {
        val newBmp: Bitmap = createBitmap(textLayer, bitmap)

        bitmap?.let { bitmap ->
            if (bitmap != newBmp && !bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        bitmap = newBmp

        bitmapWidth = bitmap?.width?.toFloat() ?: 0f
        val height = bitmap?.height?.toFloat() ?: 0f

        val widthAspect = 1f * canvasWidth / bitmapWidth

        holyScale = widthAspect

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

    private fun initTextPaint(textLayer: TextLayer) {
        textPaint.apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
            style = Paint.Style.FILL
            textSize = textLayer.font.size * canvasWidth
            color = textLayer.font.color
            typeface = fontProvider.getTypeface(textLayer.font.typefaceName)

//            if (isStringContainEmoji(textLayer.text) && textSize > MAX_SIZE_OF_EMOJI) {
//                textSize = MAX_SIZE_OF_EMOJI
//            }
        }
    }

    private fun createBitmap(textLayer: TextLayer, reuseBmp: Bitmap?): Bitmap {
        val boundsWidth = canvasWidth

        // init params - size, color, typeface
        initTextPaint(textLayer)

        // drawing text guide : http://ivankocijan.xyz/android-drawing-multiline-text-on-canvas/
        // Static layout which will be drawn on canvas
        val sl = StaticLayout(
            textLayer.text,  // - text which will be drawn
            textPaint,
            boundsWidth,  // - width of the layout
            Layout.Alignment.ALIGN_NORMAL,  // - layout alignment
            1f,  // 1 - text spacing multiply
            1f,  // 1 - text spacing add
            true
        ) // true - include padding

        // calculate height for the entity, min - Limits.MIN_BITMAP_HEIGHT
        val boundsHeight = sl.height

        // create bitmap not smaller than TextLayer.Limits.MIN_BITMAP_HEIGHT
        val bmpHeight = (canvasHeight * Math.max(TextLayer.Limits.MIN_BITMAP_HEIGHT, 1.0f * boundsHeight / canvasHeight)).toInt()

        // create bitmap where text will be drawn
        val bmp: Bitmap
        if (reuseBmp != null && reuseBmp.width == boundsWidth && reuseBmp.height == bmpHeight) {
            // if previous bitmap exists, and it's width/height is the same - reuse it
            bmp = reuseBmp
            bmp.eraseColor(Color.TRANSPARENT) // erase color when reusing
        } else {
            bmp = Bitmap.createBitmap(boundsWidth, bmpHeight, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bmp)
        canvas.save()

        // move text to center if bitmap is bigger that text
        if (boundsHeight < bmpHeight) {
            //calculate Y coordinate - In this case we want to draw the text in the
            //center of the canvas so we move Y coordinate to center.
            val textYCoordinate = ((bmpHeight - boundsHeight) / 2).toFloat()
            canvas.translate(0f, textYCoordinate)
        }

        //draws static layout on canvas
        sl.draw(canvas)
        canvas.restore()
        return bmp
    }

    private fun getMaxLengthOfText(text: String): Int {
        val lines = text.lines()
        if (text.count() == 1) {
            isOneCharacter = true
        }
        var maxLength = 0f
        for (i in lines) {
            val lineLength = textPaint.measureText(i)
            if (lineLength > maxLength) maxLength = lineLength
        }
        return maxLength.toInt()
    }

    // Apply gaussian blur to remove jagged edge of bitmap, maximum radius: 25.0
    private fun applyGaussianBlurToBitmap(bitmap: Bitmap, radius: Float, context: Context): Bitmap {
        val rs = RenderScript.create(context)

        val allocation = Allocation.createFromBitmap(rs, bitmap)

        val t = allocation.type

        val blurredAllocation = Allocation.createTyped(rs, t)

        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        blurScript.setRadius(radius)
        blurScript.setInput(allocation)
        blurScript.forEach(blurredAllocation)

        blurredAllocation.copyTo(bitmap)

        allocation.destroy()
        blurredAllocation.destroy()
        blurScript.destroy()
        // t.destroy()
        rs.destroy()
        return bitmap
    }

    private fun isStringContainEmoji(charSequence: CharSequence): Boolean {
        var result = false
        EmojiCompat.get().let {
            val processed = EmojiCompat.get().process(charSequence, 0, charSequence.length - 1, Integer.MAX_VALUE, EmojiCompat.REPLACE_STRATEGY_ALL)
            if (processed is Spannable) {
                result = processed.getSpans(0, processed.length - 1, EmojiSpan::class.java).isNotEmpty()
            }
        }
        return result
    }

    companion object {
        private const val MAX_SIZE_OF_EMOJI = 256f
    }
}