package com.rain.rainlog.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64
import com.rain.rainlog.data.model.User
import com.rain.rainlog.databinding.FragmentProfileBinding
import com.rain.rainlog.http.HttpClient
import com.rain.rainlog.http.HttpConfig
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class ProfileFragment : Fragment() {

    private lateinit var _binding: FragmentProfileBinding
    private val binding get() = _binding

    lateinit var avatar: CircleImageView
    lateinit var userName: TextView
    lateinit var personalitySignature: TextView
    lateinit var profile: Button

    companion object {
        val fromAlbum = 0
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):
            View? {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        avatar = binding.avatar
        userName = binding.userName
        personalitySignature = binding.personalitySignature
        profile = binding.profile

        val user = arguments?.getSerializable("user", User::class.java)

        Picasso.get().load(HttpConfig.BASEURL + user?.avatarPath).into(avatar)

        avatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            // 指定只显示图片
            intent.type = "image/*"
            startActivityForResult(intent, fromAlbum)
            Log.d("ProfileFragment", "avatarOnClick")
        }

        userName.text = user?.userName
        personalitySignature.text = user?.personalitySignature

        profile.setOnClickListener {
            val formBody = FormBody.Builder()
                .add("userName", userName.text.toString())
                .add("personalitySignature", personalitySignature.text.toString())
                .add("avatar", getBase64()!!)
                .build()

            HttpClient.send("/user/changeInfo", formBody, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val json = response.body?.string()
                    if (json != null) {

                        val jsonObject = JSONObject(json)
                        if (jsonObject.getBoolean("changeResult")) {
                            activity?.runOnUiThread {
                                Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    activity?.runOnUiThread {
                        findNavController().popBackStack()
                    }
                }
            })

        }

        return binding.root
    }

    fun getBase64(): String? {
        var bitmap = avatar.drawable.toBitmap()

        if (bitmap != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            val base64 = Base64.encodeBase64String(byteArray)

            return "data:image/jpeg;base64," + base64
        }

        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            fromAlbum -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { uri ->
                        // 将选择的图片显示
                        val bitmap = getBitmapFromUri(uri)
                        avatar.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    fun getBitmapFromUri(uri: Uri) = requireActivity().contentResolver
        .openFileDescriptor(uri, "r")?.use {
            BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
        }
}

