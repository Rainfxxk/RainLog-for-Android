package com.rain.rainlog.ui.comment

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.rain.rainlog.R


class CommentItem(context: Context, attributeSet: AttributeSet) : LinearLayout(context, attributeSet) {

    init {
        inflate(context, R.layout.comment_item, this)
    }

}