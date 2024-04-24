package com.example.motionviewapp.ePaper.template

import com.example.motionviewapp.ePaper.ext.OrientationEnum
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class Template(
    val name: String = "",
    val type: String = "",
    val width: Float = 0f,
    val height: Float = 0f,
    val bgColors: String = "",
    val orientation: OrientationEnum = OrientationEnum.LANDSCAPE,
    val thumbnail: String = "",
    val texts: PersistentList<TextTemplate> = persistentListOf(),
    val images: PersistentList<ImageTemplate> = persistentListOf()
)
