package com.rain.rainlog.ui.content.post

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.rain.rainlog.R
import com.rain.rainlog.databinding.PostImageBinding
import com.rain.rainlog.databinding.PostItemBinding

class PostImage(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet){
    lateinit var binding: PostImageBinding
    lateinit var image: ImageView
    init {
        binding = PostImageBinding.inflate(LayoutInflater.from(context), this, true)
        image = binding.image
    }
}