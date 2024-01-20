package com.rain.rainlog.ui.content

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.RecyclerView
import com.rain.rainlog.R
import com.rain.rainlog.databinding.PostImageBinding
import com.rain.rainlog.http.HttpConfig
import com.squareup.picasso.Picasso

class ImageAdapter(val imageList: List<String>) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    private lateinit var imagePath: String

    interface ImageClickListener {
        fun imageClick(name: String, bitmap: Bitmap)
    }

    private lateinit var imageClickListener: ImageClickListener

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var image : ImageView
        lateinit var binding: PostImageBinding

        init {
            binding = PostImageBinding.bind(itemView)
            image = binding.image
        }

        fun setImage(url: String) {
            Picasso.get().load(HttpConfig.BASEURL + url).into(image)
            image.setOnClickListener {
                Log.d("ImageAdapter", "imagePath " + imagePath + url.substring(url.indexOfLast{it == '/'} + 1, url.length))
                imageClickListener.imageClick(imagePath + url.substring(url.indexOfLast{it == '/'} + 1, url.length), image.drawable.toBitmap())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val image = LayoutInflater.from(parent.context).inflate(R.layout.post_image, parent, false)
        return ViewHolder(image)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setImage(imageList[position])
    }

    fun setImagePath(imagePath: String) {
        this.imagePath = imagePath
    }

    fun setImageClickListener(imageClickListener: ImageClickListener) {
        this.imageClickListener = imageClickListener
    }
}