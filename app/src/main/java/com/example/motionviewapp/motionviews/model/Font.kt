package com.example.motionviewapp.motionviews.model

import android.graphics.Color


data class Font(
    /**
     * color value (ex: 0xFF00FF)
     */
    var color: Int = DEFAULT_TEXT_COLOR,
    /**
     * name of the font
     */
    var typefaceName: String? = null,
    /**
     * size of the font, relative to parent
     */
    var size: Float = INITIAL_FONT_SIZE
    ) {

    fun increaseSize(diff: Float) {
        size += diff
    }

    fun decreaseSize(diff: Float) {
        if (size - diff >= Limits.MIN_FONT_SIZE) {
            size -= diff
        }
    }

    var initialSize = INITIAL_FONT_SIZE

    companion object {
        const val TEXT_QUALITY = 0.5f
        const val INITIAL_FONT_SIZE = 0.075f
        const val MAX_FONT_SIZE_FOR_PREVIEW = 100f
        const val MAX_FONT_SIZE_FOR_SAVE = 200f
        val DEFAULT_TEXT_COLOR = Color.parseColor("#252525")
        const val DEFAULT_COLOR_POSITION = 0f
    }

    private interface Limits {
        companion object {
            const val MIN_FONT_SIZE = 0.01f
        }
    }


}