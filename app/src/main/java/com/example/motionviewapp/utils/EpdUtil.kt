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
import com.example.motionviewapp.motionviews.widget.MotionView
import com.example.motionviewapp.motionviews.widget.entity.ImageEntity
import com.example.motionviewapp.motionviews.widget.entity.TextEntity
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs

class EpdUtil {
}

fun MotionView.importEdpTemplate(epdTemplate: Template, fontProvider: FontProvider) {
    post {
        epdTemplate.images.forEach { image ->
            val layer = image.imLayerFromEPD(epdTemplate.width, epdTemplate.height)
            val bitmap = createImageContentPlaceHolder(image.width, image.height)
            val entity = ImageEntity(layer, bitmap, R.drawable.pokecoin, width, height)
            addEntity(entity)
        }
        //            addTextContent()
        epdTemplate.texts.forEach { text ->
            val textLayer = text.getTextLayer(epdTemplate.width, epdTemplate.height)
            val textEntity = TextEntity(textLayer, width, height, fontProvider)
            addEntity(textEntity)
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
        val entity = ImageEntity(layer, image, R.drawable.pokecoin, width, height)
        addEntity(entity, MotionView.AddAction.TO_CENTER)
    }
}

fun MotionView.addTextContent(fontProvider: FontProvider) {
    val textLayer = createTextLayer(fontProvider.defaultFontName)
    val textEntity = TextEntity(textLayer, width, height, fontProvider)
    addEntity(textEntity, MotionView.AddAction.TO_CENTER)

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

fun MotionView.saveEpdImage(imageSize: Point, imageName: String = "exportedImage"): Bitmap {
    val rootBitmap = Bitmap.createBitmap(imageSize.x, imageSize.y, Bitmap.Config.ARGB_8888)
    val outputBm = getFinalBitmap(rootBitmap)

    val file = File(context.cacheDir, "$imageName.jpg")
    try {
        FileOutputStream(file).use { outputBm.compress(Bitmap.CompressFormat.PNG, 100, it) }
        Toast.makeText(context, "Export done: ${file.path}", Toast.LENGTH_LONG).show()
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return outputBm
}

fun MotionView.saveImage() {
    val rootBitmap = Bitmap.createBitmap(3840, 2160, Bitmap.Config.ARGB_8888)
    val outputBm = getFinalBitmap(rootBitmap)

    val file = File(context.cacheDir, "out.jpg")
    try {
        FileOutputStream(file).use { outputBm.compress(Bitmap.CompressFormat.PNG, 100, it) }
        Toast.makeText(context, "Done: ${file.path}", Toast.LENGTH_LONG).show()
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun MotionView.setCurrentTextColor(@ColorInt color: Int) {
    val textEntity1 = currentTextEntity()
    if (textEntity1 != null) {
        textEntity1.textLayer.font.color = color
        textEntity1.updateEntity()
        invalidate()
    }
}

fun MotionView.setCurrentTextFont(fontName: String?) {
    val textEntity = currentTextEntity()
    if (textEntity != null) {
        textEntity.textLayer.font.typefaceName = fontName
        textEntity.updateEntity()
        invalidate()
    }
}

fun MotionView.currentTextEntity(): TextEntity? {
    return if (selectedEntity is TextEntity) {
        selectedEntity as TextEntity
    } else null
}

fun MotionView.updateSelectedContentImage(newImage: Bitmap) {
    if (selectedEntity == null || selectedEntity is TextEntity) return
    val entity = selectedEntity as ImageEntity
    entity.bitmap = newImage
    entity.updateMatrix(editorInfo)
    invalidate()
    entity.updateMatrix(editorInfo)

}

fun Uri.getBitmap(contentResolver: ContentResolver): Bitmap? {
    return try {
        MediaStore.Images.Media.getBitmap(contentResolver, this)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}