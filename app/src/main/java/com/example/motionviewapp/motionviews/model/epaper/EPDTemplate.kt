package com.example.motionviewapp.motionviews.model.epaper

import android.graphics.Color
import androidx.annotation.ColorInt
import com.google.gson.annotations.SerializedName

data class EPDTemplate(
    @SerializedName("bgColors") val bgColors: String = "255;255;255", // 255;255;255
    @SerializedName("width") val width: Float = 1920f, // 1920 todo: Output image size
    @SerializedName("height") val height: Float = 1080f, // 1080 todo: Output image size
    @SerializedName("images") val images: List<EPDImage> = listOf(EPDImage()),
    @SerializedName("name") val name: String? = "",
    @SerializedName("orientation") val orientation: String? = "LANDSCAPE", // LANDSCAPE
    @SerializedName("texts") val texts: List<EPDText> = listOf(EPDText()),
    @SerializedName("thumbnail") val thumbnail: String? = "file:///android_asset/image_template/L_02_002_M(1)_ALL.png, // file:///android_asset/image_template/L_02_002_M(1)_ALL.png",
    @SerializedName("type") val type: String? = "",
) {
    @ColorInt
    fun bgColor() = bgColors.toColor()
}

data class EPDImage(
    @SerializedName("clipBottom") val clipBottom: String? = "0", // 0
    @SerializedName("clipLeft") val clipLeft: String? = "0", // 0
    @SerializedName("clipRight") val clipRight: String? = "0", // 0
    @SerializedName("clipTop") val clipTop: String? = "0", // 0
    @SerializedName("filePath") val filePath: String? = "",
    @SerializedName("positionX") val positionX: Float = 960f, // 0 todo: image location
    @SerializedName("positionY") val positionY: Float = 540f, // 0 todo: image location
    @SerializedName("width") val width: Float = 960f, // 960 todo: image size
    @SerializedName("height") val height: Float = 540f, // 1080 todo: image size
    @SerializedName("imageRatio") val imageRatio: String? = "0", // 0
    @SerializedName("name") val name: String? = "Image1", // Image1
    @SerializedName("opacity") val opacity: String? = "100", // 100
    @SerializedName("originalHeight") val originalHeight: String? = "0", // 0
    @SerializedName("originalWidth") val originalWidth: String? = "0", // 0
    @SerializedName("rotation") val rotation: String? = "0", // 0 todo: image rotation status
)

data class EPDText(
    @SerializedName("fillColor") val fillColor: String = "255;255;255", // 255;255;255
    @SerializedName("fillOpacity") val fillOpacity: String? = "0", // 0
    @SerializedName("width") val width: String? = "555", // 555
    @SerializedName("height") val height: String? = "93.75", // 93.75 //todo: Text height
    @SerializedName("name") val name: String? = "Menu1_1", // Menu1_1
    @SerializedName("opacity") val opacity: String? = "100", // 100
    @SerializedName("positionX") val positionX: String? = "1040", // 1040 //todo: Text location
    @SerializedName("positionY") val positionY: String? = "185", // 185 //todo: Text location
    @SerializedName("rotation") val rotation: String? = "0", // 0 //todo: Text rotation
    @SerializedName("textBold") val textBold: Boolean? = false, // false
    @SerializedName("textCaption") val textCaption: String? = "",
    @SerializedName("textColor") val textColor: String = "0;0;0", // 0;0;0 //todo: Text color
    @SerializedName("textDirection") val textDirection: String? = "0", // 0
    @SerializedName("textFontName") val textFontName: String? = "Noto Sans", // Noto Sans
    @SerializedName("textFontSize") val textFontSize: String? = "35", // 35 //todo: Text font size
    @SerializedName("textItalic") val textItalic: Boolean? = false, // false
    @SerializedName("textUnderLine") val textUnderLine: Boolean? = false, // false
    @SerializedName("textWordWrap") val textWordWrap: Boolean? = false, // false

) {
    @ColorInt
    fun parsedTextColor() = textColor.toColor()

    @ColorInt
    fun parsedFillColor() = fillColor.toColor()
}

@ColorInt
fun String.toColor(delimiter: String = ";"): Int {
    val components = split(delimiter).map { it.toInt() }
    val red = components[0]
    val green = components[1]
    val blue = components[2]
    return Color.rgb(red, green, blue)
}