package com.rain.rainlog.ui.publish

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rain.rainlog.R
import com.rain.rainlog.databinding.SelectedImageItemBinding

class SelectedImageItemAdapter(val imageList: List<Bitmap>) : RecyclerView.Adapter<SelectedImageItemAdapter.ViewHolder>() {

    private lateinit var onCloseListener: OnCloseListener

    interface OnCloseListener {
        fun onClose(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = SelectedImageItemBinding.bind(itemView)
        val image = binding.image
        val close = binding.close

        fun setclose(position: Int) {
            close.setOnClickListener {
                onCloseListener.onClose(position)
            }
        }

        fun setBitmap(bitmap: Bitmap) {
            image.setImageBitmap(bitmap)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selected_image_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setBitmap(imageList[position])
        holder.setclose(position)
    }

    fun setCloseListener(onCloseListener: OnCloseListener) {
        this.onCloseListener = onCloseListener
    }
}