package com.example.motionviewapp.utils

import android.graphics.Bitmap
import android.graphics.Canvas

fun Bitmap.copyBitmap(): Bitmap {
    val width = width
    val height = height

    val copiedBitmap = Bitmap.createBitmap(width, height, config)

    val canvas = Canvas(copiedBitmap)
    canvas.drawBitmap(this, 0f, 0f, null)
    return copiedBitmap
}