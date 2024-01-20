package com.rain.rainlog.ui.follow

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.rain.rainlog.R

class FollowItem(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {

    init {
        inflate(context, R.layout.follow_item, this)
    }

}