package com.example.motionviewapp.ePaper.template

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class TemplateModel(
    val id: String = "",
    val title: String = "",
    val type: String = "",
    val listTemplate: PersistentList<Template> = persistentListOf()
)
