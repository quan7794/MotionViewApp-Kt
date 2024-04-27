package com.example.motionviewapp.utils

import android.content.Context
import android.graphics.ComposePathEffect
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.example.motionviewapp.R
import com.example.motionviewapp.motionviews.widget.content.BaseContent

object BorderUtil {
    private fun getComposePathEffect(context: Context): ComposePathEffect {
        val strokeRadius = context.resources.getDimensionPixelSize(R.dimen.stroke_corner_radius)
        val dashSize = context.resources.getDimensionPixelSize(R.dimen.dash_size)
        val spaceSize = context.resources.getDimensionPixelSize(R.dimen.space_size)
        val cornerPathEffect = CornerPathEffect(strokeRadius.toFloat())
        val dashPathEffect = DashPathEffect(floatArrayOf(dashSize.toFloat(), spaceSize.toFloat()), 0f)
        return ComposePathEffect(dashPathEffect, cornerPathEffect)
    }

    fun initEntityBorder(entity: BaseContent, context: Context, isRotating: Boolean) {
        val strokeSize = context.resources.getDimensionPixelSize(R.dimen.stroke_size)
        var strokeColor = ContextCompat.getColor(context, R.color.stroke_color)
        if (isRotating) strokeColor = ContextCompat.getColor(context, R.color.stroke_color_on_rotate)
        val borderPaint = Paint().apply {
            strokeWidth = strokeSize.toFloat()
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = strokeColor
            pathEffect = getComposePathEffect(context)
        }
        entity.setBorderPaint(borderPaint)
    }

    fun initEntityIconBackground(entity: BaseContent) {
        val iconBackground = Paint()
        iconBackground.isAntiAlias = true
        entity.setIconBackground(iconBackground)
    }
}