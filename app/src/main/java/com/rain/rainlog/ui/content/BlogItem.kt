package com.rain.rainlog.ui.content

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.rain.rainlog.R

class BlogItem(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs){
    init {
        LayoutInflater.from(context).inflate(R.layout.blog_item, this)
    }
}