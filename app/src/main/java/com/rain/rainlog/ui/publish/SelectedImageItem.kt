package com.rain.rainlog.ui.publish

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.rain.rainlog.R

class SelectedImageItem(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet){

    init {
        LayoutInflater.from(context).inflate(R.layout.selected_image_item, this)
    }
}