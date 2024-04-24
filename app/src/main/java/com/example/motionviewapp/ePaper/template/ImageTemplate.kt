package com.example.motionviewapp.ePaper.template

data class ImageTemplate(
    val name: String = "",
    val filePath: String = "",
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
    val originalWidth: Float = 0f,
    val originalHeight: Float = 0f,
    val rotation: String = "",
    val clipLeft: String = "",
    val clipRight: String = "",
    val clipTop: String = "",
    val clipBottom: String = "",
    val opacity: String = "",
    val imageRatio: String = "",
)