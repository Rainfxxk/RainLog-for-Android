package com.rain.rainlog.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.rain.rainlog.R
import com.rain.rainlog.databinding.DialogImageBinding
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths

class ImageDialog(val name: String, val bitmap: Bitmap): DialogFragment(){
    private lateinit var _binding: DialogImageBinding
    val binding get() = _binding

    lateinit var image: ImageView
    lateinit var download: CircleImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogImageBinding.inflate(inflater, container, false)

        image = binding.image
        download = binding.download

        image.setImageBitmap(bitmap)
        image.setOnClickListener {
            dismissNow()
        }

        download.setOnClickListener {
            saveBitmap()
        }

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;
            builder.setView(inflater.inflate(R.layout.dialog_image, null))
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun saveBitmap() {
        val savePath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        if (!Files.exists(Paths.get(savePath))) {
            Log.d("ImageDialog", "${savePath}不存在!")
        } else {
            val saveFile = File(savePath, name)

            try {
                val saveImgOut = FileOutputStream(saveFile)
                //压缩
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, saveImgOut)
                saveImgOut.flush()
                saveImgOut.close()
                MediaScannerConnection.scanFile(context, arrayOf(saveFile.absolutePath), arrayOf("image/jpeg")) { path, uri ->
                    activity?.runOnUiThread {
                        Toast.makeText(context, "图片保存成功", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.d("ImageDialog", "Bitmap保存至 ${saveFile.absoluteFile.toPath()}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}