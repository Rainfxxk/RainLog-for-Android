package com.rain.rainlog.ui.content.post

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.GridLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.rain.rainlog.R

class PostItem(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {
    init {
        LayoutInflater.from(context).inflate(R.layout.post_item, this)

        var layoutParams = GridLayout.LayoutParams()

        layoutParams.width = 0
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT
        layoutParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
        layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)

        this.layoutParams = layoutParams
    }
}