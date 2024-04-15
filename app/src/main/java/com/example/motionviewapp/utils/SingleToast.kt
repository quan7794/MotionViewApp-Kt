package com.samsung.android.plugin.tv.pages.sero.feature.content.photo.myphoto.decor.utils

import android.content.Context
import android.widget.Toast

object SingleToast {

    private var singleToast: Toast? = null

    fun show(context: Context, text: String, duration: Int) {
        singleToast?.cancel()
        singleToast = Toast.makeText(context, text, duration)
        singleToast?.show()
    }
}