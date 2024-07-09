package com.example.motionviewapp.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.ColorInt
import com.example.motionviewapp.R
import com.example.motionviewapp.ePaper.ext.createImageContentPlaceHolder
import com.example.motionviewapp.ePaper.template.ImageTemplate
import com.example.motionviewapp.ePaper.template.Template
import com.example.motionviewapp.ePaper.template.TextTemplate
import com.example.motionviewapp.motionviews.model.Font
import com.example.motionviewapp.motionviews.model.Layer
import com.example.motionviewapp.motionviews.model.TextLayer
import com.example.motionviewapp.motionviews.ui.AndroidBmpUtil
import com.example.motionviewapp.motionviews.widget.MotionView
import com.example.motionviewapp.motionviews.widget.content.ImageContent
import com.example.motionviewapp.motionviews.widget.content.TextContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs

fun MotionView.importEdpTemplate(epdTemplate: Template, fontProvider: FontProvider) {
    post {
        epdTemplate.images.forEach { image ->
            val layer = image.imLayerFromEPD(epdTemplate.width, epdTemplate.height)
            val bitmap = createImageContentPlaceHolder(image.width, image.height)
            addContent(ImageContent(layer, bitmap, R.drawable.pokecoin, width, height))
        }
        epdTemplate.texts.forEach { text ->
            val textLayer = text.getTextLayer(epdTemplate.width, epdTemplate.height)
            addContent(TextContent(textLayer, width, height, fontProvider))
        }
    }
}

private fun ImageTemplate.imLayerFromEPD(epdWidth: Float, epdHeight: Float): Layer {
    val holyScale = minOf(epdWidth / width, epdHeight / height)
    val wMapped = width * holyScale
    val hMapped = height * holyScale
    val xMapped = positionX - abs(width - wMapped) / 2
    val yMapped = positionY - abs(height - hMapped) / 2

    return Layer().apply {
        x = xMapped / epdWidth
        y = yMapped / epdHeight
        scale = 1f / holyScale
    }
}

private fun TextTemplate.getTextLayer(epdWidth: Float, epdHeight: Float): TextLayer {
    val holyScale = minOf(epdWidth / width, epdHeight / height)
    val wMapped = width * holyScale
    val hMapped = height * holyScale
    val xMapped = positionX - abs(width - wMapped) / 2
    val yMapped = positionY - abs(height - hMapped) / 2

    return TextLayer().apply {
        x = xMapped / epdWidth
        y = yMapped / epdHeight
        scale = 1f / holyScale
        font = Font().apply {
            font.color = getTextColor()
            font.size = textFontSize.toFloat() * TextLayer.Limits.FONT_SIZE_STEP
            font.typefaceName = "Helvetica"
        }
    }
}

fun MotionView.addImageContent(image: Bitmap) {
    Timber.tag("AAA").d("addImageContent Entry")
    post {
        val layer = Layer()
        addContent(ImageContent(layer, image, R.drawable.pokecoin, width, height), MotionView.AddAction.TO_CENTER)
    }
}

fun MotionView.addTextContent(fontProvider: FontProvider) {
    val textLayer = createTextLayer(fontProvider.defaultFontName)
    addContent(TextContent(textLayer, width, height, fontProvider), MotionView.AddAction.TO_CENTER)
//        // move text sticker up so that its not hidden under keyboard
//        val center = textEntity.absoluteCenter()
//        center.y = center.y * 0.5f
//        textEntity.moveCenterTo(center)
//
//        // redraw
//        motionView!!.invalidate()
}

private fun createTextLayer(fontName: String): TextLayer {
    val textLayer = TextLayer()
    val font = Font()

    font.color = TextLayer.Limits.INITIAL_FONT_COLOR
    font.size = TextLayer.Limits.INITIAL_FONT_SIZE
    font.typefaceName = fontName

    textLayer.font = font

    return textLayer
}

suspend fun MotionView.saveImage(imageSize: Point = Point(1920, 1080), imageName: String = "exportedImage"): String? {
    val rootBitmap = Bitmap.createBitmap(imageSize.x, imageSize.y, Bitmap.Config.ARGB_8888)
    val outputBm = getFinalBitmap(rootBitmap)
    val bmByte = AndroidBmpUtil().getBmpByte(outputBm)

    val file = File(context.cacheDir, "$imageName.bmp")
    return try {
//        withContext(Dispatchers.IO) { FileOutputStream(file).use { outputBm.compress(Bitmap.CompressFormat.PNG, 100, it) } }
        withContext(Dispatchers.IO) { FileOutputStream(file).use { it.write(bmByte) } }
        Toast.makeText(context, "Done: ${file.path}", Toast.LENGTH_LONG).show()
        rootBitmap.recycle()
        file.path
    } catch (ex: Exception) {
        ex.printStackTrace()
        rootBitmap.recycle()
        null
    }
}

fun MotionView.setCurrentTextColor(@ColorInt color: Int) {
    currentTextContent()?.let {
        it.textLayer.font.color = color
        it.updateContent()
    }
    invalidate()
}

fun MotionView.setCurrentTextFont(fontName: String?) {
    currentTextContent()?.let {
        it.textLayer.font.typefaceName = fontName
        it.updateContent()
    }
    invalidate()
}

fun MotionView.currentTextContent(): TextContent? {
    return if (selectedContent is TextContent) {
        selectedContent as TextContent
    } else null
}

fun MotionView.setImageForSelectedContent(newImage: Bitmap) {
    if (selectedContent == null || selectedContent is TextContent) return
    (selectedContent as ImageContent).let {
        it.bmWidth = newImage.width
        it.bmHeight = newImage.height
        it.bitmap = newImage
        it.initInfo()
    }
    invalidate()
}

fun Uri.getBitmap(contentResolver: ContentResolver): Bitmap? {
    return try {
        MediaStore.Images.Media.getBitmap(contentResolver, this)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}