package com.example.motionviewapp.motionviews.model

import androidx.annotation.ColorInt
import com.example.motionviewapp.motionviews.model.TextLayer.Limits.Companion.INITIAL_FONT_COLOR
import com.example.motionviewapp.motionviews.model.TextLayer.Limits.Companion.INITIAL_FONT_SIZE


data class Font(
    /**
     * color value (ex: 0xFF00FF)
     */
    @ColorInt var color: Int = INITIAL_FONT_COLOR,
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
        if (size - diff >= TextLayer.Limits.MIN_FONT_SIZE) {
            size -= diff
        }
    }
}