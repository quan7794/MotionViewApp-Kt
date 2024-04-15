//package com.example.motionviewapp.utils
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import com.caverock.androidsvg.SVG
//
//object StickerUtil {
//    fun getBitmapFromSVG(context: Context, resourceId: Int, bitmapWidth: Int, bitmapHeight: Int): Bitmap {
//        val bm = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bm)
//        val svg = SVG.getFromResource(context.resources, resourceId)
//        svg.renderToCanvas(canvas)
//        return bm
//    }
//}