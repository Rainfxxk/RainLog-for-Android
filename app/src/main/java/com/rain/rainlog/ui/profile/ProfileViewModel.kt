package com.rain.rainlog.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rain.rainlog.data.model.User
import com.rain.rainlog.http.HttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Response
import java.io.IOException

class ProfileViewModel : ViewModel(){
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _avatar = MutableLiveData<String>()
    val avatar: LiveData<String> = _avatar

    fun changeInfo(user: User) {


    }
}