package com.example.motionviewapp.motionviews.ui.adapter

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.motionviewapp.motionviews.utils.FontProvider

class FontsAdapter(context: Context?, fontNames: List<String?>?, private val fontProvider: FontProvider) : ArrayAdapter<String?>(
    context!!, 0, fontNames!!
) {
    // save LayoutInflater for later reuse
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var mConvertView = convertView
        val vh: ViewHolder
        if (mConvertView == null) {
            mConvertView = inflater.inflate(R.layout.simple_list_item_1, parent, false)
            vh = ViewHolder(mConvertView)
            mConvertView.tag = vh
        } else {
            vh = mConvertView.tag as ViewHolder
        }

        val fontName = getItem(position)

        vh.textView.typeface = fontProvider.getTypeface(fontName)
        vh.textView.text = fontName

        return mConvertView!!
    }

    private class ViewHolder(rootView: View?) {
        var textView: TextView = rootView!!.findViewById<View>(R.id.text1) as TextView
    }
}