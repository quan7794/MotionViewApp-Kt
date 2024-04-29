package com.example.motionviewapp.motionviews.model

import com.example.motionviewapp.motionviews.model.TextLayer.Limits.Companion.DEFAULT_TEXT

data class TextLayer(
    var text: String = DEFAULT_TEXT,
    var font: Font = Font(),
) : Layer() {

    override var initialScale = DEFAULT_INITIAL_SCALE
    override var minScale = DEFAULT_MIN_SCALE
    override var maxScale = DEFAULT_MAX_SCALE
    var isEditing = false

    override fun reset() {
        super.reset()
        text = "Hello, world!"
        font = Font()
    }

    override fun postScale(scaleDiff: Float) {
        val newVal = scale + scaleDiff

        val isOutOfMaxRange = newVal > maxScale && scale > newVal
        val isOutOfMinRange = newVal < minScale && scale < newVal
        val isInNormalRange = newVal in minScale..maxScale
        if (isOutOfMaxRange || isOutOfMinRange || isInNormalRange) {
            scale = newVal
        }
    }

    fun cloneTextLayer(): TextLayer {
        return TextLayer(text, font.copy())
    }

    interface Limits {
        companion object {
            const val DEFAULT_TEXT ="Hello, world!"
            const val MAX_SCALE = 1.0f
            const val MIN_SCALE = 0.2f
            const val MIN_BITMAP_HEIGHT = 0.13f
            const val FONT_SIZE_STEP = 0.08f
            const val MIN_FONT_SIZE = 0.01F
            const val INITIAL_FONT_SIZE = 0.075f
            const val INITIAL_FONT_COLOR = -0x1000000
            const val INITIAL_SCALE = 0.8f // set the same to avoid text scaling
        }
    }

}


