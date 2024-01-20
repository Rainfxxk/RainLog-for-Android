package com.rain.rainlog.ui.content

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.rain.rainlog.R

class ContentItem(context: Context, attributeSet: AttributeSet) : LinearLayout(context, attributeSet){
    init {
        LayoutInflater.from(context).inflate(R.layout.content_item, this)
    }
}