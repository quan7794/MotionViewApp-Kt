package com.example.motionviewapp.ePaper.template

import android.graphics.Color
import androidx.annotation.ColorInt

data class TextTemplate(
    val name: String = "",
    val text: String = "",
    val textCaption: String = "",
    val fillColor: String = "",
    val positionX: Float = 0f,
    val fillOpacity: String = "",
    val positionY: Float = 0f,
    val opacity: String = "",
    val rotation: String = "",
    val width: Float = 0f,
    val height: Float = 0f,
    val textWordWrap: Boolean = false,
    val textDirection: String = "",
    val textFontName: String = "",
    val textColor: String = "",
    val textFontSize: String = "",
    val textItalic: Boolean = false,
    val textBold: Boolean = false,
    val textUnderLine: Boolean = false,
) {
    @ColorInt
    fun getTextColor(delimiter: String = ";"): Int {
        val components = textColor.split(delimiter).map { it.toInt() }
        val red = components[0]
        val green = components[1]
        val blue = components[2]
        return Color.rgb(red, green, blue)
    }
}