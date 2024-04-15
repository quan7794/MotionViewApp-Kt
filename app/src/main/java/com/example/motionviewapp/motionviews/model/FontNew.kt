package com.example.motionviewapp.motionviews.model

import android.graphics.Color

data class FontNew(var color: Int = DEFAULT_TEXT_COLOR,
                   var size: Float = INITIAL_FONT_SIZE
) {

    var initialSize = INITIAL_FONT_SIZE

    companion object {
        const val TEXT_QUALITY = 0.5f
        const val INITIAL_FONT_SIZE = 40f * TEXT_QUALITY
        const val MAX_FONT_SIZE_FOR_PREVIEW = 100f
        const val MAX_FONT_SIZE_FOR_SAVE = 200f
        val DEFAULT_TEXT_COLOR = Color.parseColor("#252525")
        const val DEFAULT_COLOR_POSITION = 0f
    }
}