package com.example.motionviewapp.ePaper

import android.content.Context
import com.example.motionviewapp.ePaper.ext.OrientationEnum
import com.example.motionviewapp.ePaper.template.TemplateModel
import com.example.motionviewapp.ePaper.template.ImageTemplate
import com.example.motionviewapp.ePaper.template.Template
import com.example.motionviewapp.ePaper.template.TextTemplate
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import timber.log.Timber
import java.io.IOException
import java.io.StringReader
import java.util.UUID

fun parseTemplate(xmlString: String, fileName: String): Template {
    try {
        Timber.d("start calculatorTime  $fileName")
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser: XmlPullParser = factory.newPullParser()
        parser.setInput(StringReader(xmlString))

        val listText = arrayListOf<TextTemplate>()
        val listImage = arrayListOf<ImageTemplate>()
        var displayWidth = ""
        var displayHeight = ""
        var backgroundColor = ""

        var eventType = parser.eventType
        var tagName: String
        var elementType: String
        Timber.d("parseTemplate fileName $fileName")
        Timber.d("parseTemplate eventTypeXML $eventType && parser.name: ${parser.name}")
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    tagName = parser.name
                    Timber.d("tagName $tagName")
                    when (tagName) {
                        "Element" -> {
                            elementType = parser.getAttributeValue(null, "type")
                            Timber.d("elementType $elementType")
                            when (elementType) {
                                "Text" -> {
                                    listText.add(parseTextTemplate(parser))
                                }

                                "Image" -> {
                                    listImage.add(parseImageTemplate(parser))
                                }
                            }
                        }

                        "DisplayWidth" -> {
                            parser.nextTag()
                            displayWidth = parser.nextText()
                        }

                        "DisplayHeight" -> {
                            parser.nextTag()
                            displayHeight = parser.nextText()
                        }

                        "BGColor" -> {
                            parser.nextTag()
                            backgroundColor = parser.nextText()
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        Timber.d("eventTypeXML $eventType")
        val orientationFileName = if (fileName.split("_")[0] == "L") OrientationEnum.LANDSCAPE else OrientationEnum.PORTRAIT

        Timber.e("totalData listText  ${listText.size} listImage  ${listImage.size}")
        Timber.d("end calculatorTime  $fileName")
        return Template(
            width = displayWidth.toFloat(),
            height = displayHeight.toFloat(),
            bgColors = backgroundColor,
            thumbnail = "file:///android_asset/image_template/${fileName.split(".")[0]}.png",
            orientation = orientationFileName,
            texts = listText.toPersistentList(),
            images = listImage.toPersistentList()
        )
    } catch (ex: Exception) {
        Timber.e("parseTemplate ${ex.printStackTrace()}")
        return Template()
    }
}

private fun parseTextTemplate(parser: XmlPullParser): TextTemplate {
    try {
        var name = ""
        var textCaption = ""
        var fillColor = ""
        var positionX = ""
        var positionY = ""
        var fillOpacity = ""
        var opacity = ""
        var width = ""
        var height = ""
        var rotation = ""
        var textDirection = ""
        var textFontName = ""
        var textColor = ""
        var textFontSize = ""
        var textItalic = false
        var textBold = false
        var textUnderLine = false

        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    Timber.d("parseImageTemplate parser.name ${parser.name}")
                    when (parser.name) {
                        "Name" -> {
                            parser.nextTag()
                            name = parser.nextText()
                        }

                        "TextCaption" -> {
                            parser.nextTag()
                            textCaption = parser.nextText()
                        }

                        "FillColor" -> {
                            parser.nextTag()
                            fillColor = parser.nextText()
                        }

                        "PositionX" -> {
                            parser.nextTag()
                            positionX = parser.nextText()
                        }

                        "PositionY" -> {
                            parser.nextTag()
                            positionY = parser.nextText()
                        }

                        "FillOpacity" -> {
                            parser.nextTag()
                            fillOpacity = parser.nextText()
                        }

                        "Opacity" -> {
                            parser.nextTag()
                            opacity = parser.nextText()
                        }

                        "Width" -> {
                            parser.nextTag()
                            width = parser.nextText()
                        }

                        "Height" -> {
                            parser.nextTag()
                            height = parser.nextText()
                        }

                        "Rotation" -> {
                            parser.nextTag()
                            rotation = parser.nextText()
                        }

                        "TextDirection" -> {
                            parser.nextTag()
                            textDirection = parser.nextText()
                        }

                        "TextFontName" -> {
                            parser.nextTag()
                            textFontName = parser.nextText()
                        }

                        "TextColor" -> {
                            parser.nextTag()
                            textColor = parser.nextText()
                        }

                        "TextFontSize" -> {
                            parser.nextTag()
                            textFontSize = parser.nextText()
                        }

                        "TextItalic" -> {
                            parser.nextTag()
                            textItalic = parser.nextText().toBoolean()
                        }

                        "TextBold" -> {
                            parser.nextTag()
                            textBold = parser.nextText().toBoolean()
                        }

                        "TextUnderLine" -> {
                            parser.nextTag()
                            textUnderLine = parser.nextText().toBoolean()
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "Element" -> break;
                    }
                }
            }
            eventType = parser.next()
        }
        Timber.e("parseTextTemplate eventType $eventType")
        return TextTemplate(
            name = name,
            textCaption = textCaption,
            fillColor = fillColor,
            positionX = positionX.toFloat(),
            positionY = positionY.toFloat(),
            fillOpacity = fillOpacity,
            opacity = opacity,
            width = width.toFloat(),
            height = height.toFloat(),
            rotation = rotation,
            textDirection = textDirection,
            textFontName = textFontName,
            textColor = textColor,
            textFontSize = textFontSize,
            textItalic = textItalic,
            textBold = textBold,
            textUnderLine = textUnderLine
        )
    } catch (ex: Exception) {
        Timber.e("parseTextTemplate ${ex.message}")
        return TextTemplate()
    }
}

private fun parseImageTemplate(parser: XmlPullParser): ImageTemplate {
    try {
        var name = ""
        var filePath = ""
        var positionX = ""
        var positionY = ""
        var width = ""
        var height = ""
        var rotation = ""
        var originalWidth = ""
        var originalHeight = ""
        var clipLeft = ""
        var clipRight = ""
        var clipTop = ""
        var clipBottom = ""
        var imageRatio = ""
        var opacity = ""

        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    Timber.d("parseImageTemplate parser.name ${parser.name}")
                    when (parser.name) {
                        "Name" -> {
                            parser.nextTag()
                            name = parser.nextText()
                        }

                        "FilePath" -> {
                            parser.nextTag()
                            filePath = parser.nextText()
                        }

                        "PositionX" -> {
                            parser.nextTag()
                            positionX = parser.nextText()
                        }

                        "PositionY" -> {
                            parser.nextTag()
                            positionY = parser.nextText()
                        }

                        "Width" -> {
                            parser.nextTag()
                            width = parser.nextText()
                        }

                        "Height" -> {
                            parser.nextTag()
                            height = parser.nextText()
                        }

                        "Rotation" -> {
                            parser.nextTag()
                            rotation = parser.nextText()
                        }

                        "OrginalWidth" -> {
                            parser.nextTag()
                            originalWidth = parser.nextText()
                        }

                        "OriginalHeight" -> {
                            parser.nextTag()
                            originalHeight = parser.nextText()
                        }

                        "ClipLeft" -> {
                            parser.nextTag()
                            clipLeft = parser.nextText()
                        }

                        "ClipRight" -> {
                            parser.nextTag()
                            clipRight = parser.nextText()
                        }

                        "ClipTop" -> {
                            parser.nextTag()
                            clipTop = parser.nextText()
                        }

                        "ClipBottom" -> {
                            parser.nextTag()
                            clipBottom = parser.nextText()
                        }

                        "ImageRatio" -> {
                            parser.nextTag()
                            imageRatio = parser.nextText()
                        }

                        "Opacity" -> {
                            parser.nextTag()
                            opacity = parser.nextText()
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "Element" -> break;
                    }
                }
            }
            eventType = parser.next()
        }
        Timber.e("parseImageTemplate eventType $eventType")
        return ImageTemplate(
            name,
            filePath,
            positionX.toFloat(),
            positionY.toFloat(),
            width.toFloat(),
            height.toFloat(),
            originalWidth.toFloatOrNull() ?: 0f,
            originalHeight.toFloatOrNull() ?: 0f,
            rotation,
            clipLeft,
            clipRight,
            clipTop,
            clipBottom,
            opacity,
            imageRatio
        )
    } catch (ex: Exception) {
        Timber.e("parseImageTemplate ${ex.message}")
        return ImageTemplate()
    }
}

fun getTemplateFilesNames(context: Context): List<String> {
    val assetManager = context.assets

    try {
        // List all files under "template" directory
        val templateFiles = assetManager.list("template")

        templateFiles?.let { list ->
            return list.toList()
        }
        return emptyList()
    } catch (e: IOException) {
        e.printStackTrace()
        return emptyList()
    }
}

suspend fun getListTemplate(listNameFileTemplate: List<String>): PersistentList<TemplateModel> {
    return coroutineScope {
        try {
            Timber.d("start getListTemplate")
            val templates = listNameFileTemplate.map { fileName ->
                async {
                    val orientation = fileName.split("_")[0]
                    val numberType = fileName.split("_")[1]

                    Template(
                        name = fileName,
                        type = numberType,
                        thumbnail = "file:///android_asset/image_template/${fileName.split(".")[0]}.png",
                        orientation = if (orientation == "L") OrientationEnum.LANDSCAPE else OrientationEnum.PORTRAIT
                    )
                }
            }.awaitAll()

            val listGroup = templates.groupBy { it.type }
            listGroup.toSortedMap(compareBy { it }).map {
                TemplateModel(
                    id = UUID.randomUUID().toString(),
                    title = "Lorem",
                    type = it.key,
                    listTemplate = it.value.toPersistentList()
                )
            }.toPersistentList()
        } catch (ex: Exception) {
            ex.printStackTrace()
            Timber.e("end getListTemplate ex ${ex.message}")
            persistentListOf<TemplateModel>()
        }
    }
}

