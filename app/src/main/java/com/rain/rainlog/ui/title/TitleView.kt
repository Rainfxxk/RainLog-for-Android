package com.rain.rainlog.ui.title

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.rain.rainlog.R
import com.rain.rainlog.databinding.TitleBinding
import com.rain.rainlog.http.HttpConfig
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class TitleView(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet){
    private lateinit var binding : TitleBinding
    private lateinit var avatar: CircleImageView
    private lateinit var title: TextView
    private lateinit var publish: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.title, this)
        binding = TitleBinding.inflate(LayoutInflater.from(context), this, true)
        avatar = binding.avatar
        title = binding.title
        publish = binding.publish
    }

    public fun setAvatarSrc(resId: Int) {
        avatar.setImageResource(resId)
    }

    public fun setAvatarSrc(avatarPath: String) {
        Log.d("TitleView", "setAvatarSrv: $avatarPath")
        Picasso.get().load(HttpConfig.BASEURL + avatarPath).into(avatar)
    }

    public fun setAvatarClickListener(onClickListener: OnClickListener) {
        avatar.setOnClickListener(onClickListener)
    }

    public fun setPublishClickListener(onClickListener: OnClickListener) {
        publish.setOnClickListener(onClickListener)
    }

    public fun setTitleText(titleText: String) {
        title.setText(titleText)
    }
}