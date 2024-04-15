package com.example.motionviewapp.utils

import android.content.res.Resources
import android.graphics.Typeface
import android.text.TextUtils
import android.util.TypedValue

/**
 * extracting Typeface from Assets is a heavy operation,
 * we want to make sure that we cache the typefaces for reuse
 */
class FontProvider(private val resources: Resources) {
    val defaultFontName: String  = "Helvetica"
    private val typefaces: MutableMap<String?, Typeface?>
    private val fontNameToTypefaceFile: MutableMap<String?, String>

    /**
     * use [to get Typeface for the font name][FontProvider.getTypeface]
     *
     * @return list of available font names
     */
    val fontNames: List<String?>

    init {
        typefaces = HashMap()

        // populate fonts
        fontNameToTypefaceFile = HashMap()
        fontNameToTypefaceFile["Arial"] = "Arial.ttf"
        fontNameToTypefaceFile["Eutemia"] = "Eutemia.ttf"
        fontNameToTypefaceFile["GREENPIL"] = "GREENPIL.ttf"
        fontNameToTypefaceFile["Grinched"] = "Grinched.ttf"
        fontNameToTypefaceFile["Helvetica"] = "Helvetica.ttf"
        fontNameToTypefaceFile["Libertango"] = "Libertango.ttf"
        fontNameToTypefaceFile["Metal Macabre"] = "MetalMacabre.ttf"
        fontNameToTypefaceFile["Parry Hotter"] = "ParryHotter.ttf"
        fontNameToTypefaceFile["SCRIPTIN"] = "SCRIPTIN.ttf"
        fontNameToTypefaceFile["The Godfather v2"] = "TheGodfather_v2.ttf"
        fontNameToTypefaceFile["Aka Dora"] = "akaDora.ttf"
        fontNameToTypefaceFile["Waltograph"] = "waltograph42.ttf"
        fontNames = ArrayList(fontNameToTypefaceFile.keys)
    }

    /**
     * @param typefaceName must be one of the font names provided from [FontProvider.getFontNames]
     * @return the Typeface associated with `typefaceName`, or [Typeface.DEFAULT] otherwise
     */
    fun getTypeface(typefaceName: String?): Typeface? {
        return if (TextUtils.isEmpty(typefaceName)) {
            Typeface.DEFAULT
        } else {
            if (typefaces[typefaceName] == null) {
                typefaces[typefaceName] = Typeface.createFromAsset(resources.assets, "fonts/" + fontNameToTypefaceFile[typefaceName])
            }
            typefaces[typefaceName]
        }
    }


    fun spToPx(sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
    }
}