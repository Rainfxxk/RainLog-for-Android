package com.rain.rainlog.ui.publish

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64
import com.rain.rainlog.http.HttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class PublishViewModel : ViewModel() {

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    private val _images = MutableLiveData<MutableList<Bitmap>>(mutableListOf())
    val images: LiveData<MutableList<Bitmap>> = _images

    private val _imgNum = MutableLiveData<Int>()
    val imgNum: LiveData<Int> = _imgNum

    private val _publishResult = MutableLiveData<Boolean>()
    val publishResult: LiveData<Boolean> = _publishResult

    public fun setText(text: String) {
        _text.postValue(text)
    }

    fun addBitMap(bitmap: Bitmap) {
        images.value?.add(bitmap)
        _imgNum.postValue(images.value?.size!!)
    }

    public fun getBitMap(index: Int): Bitmap {
        return images.value!![index]
    }

    public fun removeBitMap(index: Int) {
        images.value?.removeAt(index)
        _imgNum.postValue(images.value?.size!!)
    }

    public fun isImagesEmpty(): Boolean {
        return images.value?.isEmpty()!!
    }

    public fun publish(content: String) {
        val formBodyBuilder = FormBody.Builder()
            .add("content", content)

        if (imgNum.value != null) {
            for (i in 0 until imgNum.value!!) {
                getBase64(images.value!![i])?.let { it1 ->
                    formBodyBuilder.add("images",
                        it1
                    )
                }
            }
        }

        val formBody = formBodyBuilder.build()

        HttpClient.send("/post/publishPost", formBody, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonStr = response.body?.string()
                if (jsonStr != null) {
                    Log.d("PublishFragment", jsonStr)
                }
                val jsonObject = JSONObject(jsonStr)

                if (jsonObject.getBoolean("postResult")) {
                    _publishResult.postValue(true)
                }
                else {
                    _publishResult.postValue(false)
                }
            }
        })
    }

    fun getBase64(bitmap: Bitmap): String? {

        if (bitmap != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            val base64 = Base64.encodeBase64String(byteArray)

            return "data:image/jpeg;base64," + base64
        }

        return null
    }
}