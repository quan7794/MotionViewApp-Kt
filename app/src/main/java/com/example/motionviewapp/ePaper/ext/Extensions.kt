package com.example.motionviewapp.ePaper.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Base64
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatDelegate
import org.w3c.dom.NodeList
import timber.log.Timber
import kotlin.math.abs

fun createImageContentPlaceHolder(width: Number, height: Number, borderColor: Int = Color.GRAY, borderWidth: Float = 2f, padding: Float = 4f): Bitmap {
    val bitmapWithBorderAndPadding = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmapWithBorderAndPadding)
    val paint = Paint()
    paint.style = Paint.Style.STROKE
    paint.color = borderColor
    paint.strokeWidth = borderWidth
    val drawRect = RectF(padding, padding, width.toFloat() - padding, height.toFloat() - padding)
    canvas.drawRect(drawRect, paint) // Vẽ border
    return bitmapWithBorderAndPadding
}

fun Int.mapToNearestNumber(setOfNumbers: Set<Int>): Int {
    var nearestNumber = 0
    var smallestDifference = Int.MAX_VALUE

    for (number in setOfNumbers) {
        val difference = abs(this - number)
        if (difference < smallestDifference) {
            smallestDifference = difference
            nearestNumber = number
        }
    }
    return nearestNumber
}

fun String.base64ToBitmap(): Bitmap? {
    if (this.isEmpty()) return null
    return try {
        val imageBytes = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } catch (ex: Exception) {
        null
    }
}

fun String.decodeBase64(): ByteArray? {
    if (this.isEmpty()) return null
    return try {
        Base64.decode(this, Base64.DEFAULT)
    } catch (ex: Exception) {
        null
    }
}

fun Int.mapNightModeToSeekBar() = when (this) {
    AppCompatDelegate.MODE_NIGHT_YES -> 0
    AppCompatDelegate.MODE_NIGHT_NO -> 1
    else -> 2
}

fun Context.showKeyboard(view: View) {
    this.getSystemService(Context.INPUT_METHOD_SERVICE)?.let { imm ->
        (imm as InputMethodManager).showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Context.readAssets(fileName: String): String {
    return this.assets.open(fileName).bufferedReader().use {
        Timber.d("readAssets")
        it.readText()
    }
}

fun EditText.onDone(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

fun NodeList.parseValueToString(): String {
    var value = ""
    for (i in 0 until this.length) {
        value = this.item(i).textContent.trim()
    }
    return value
}
