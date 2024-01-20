package com.rain.rainlog.ui.page

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.rain.rainlog.data.model.User
import com.rain.rainlog.http.HttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class PageViewModel : ViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _isFollow = MutableLiveData<Boolean?>()
    val isFollow: MutableLiveData<Boolean?> = _isFollow

    private val _logoutResult = MutableLiveData<Boolean>()
    val logoutResult: LiveData<Boolean> = _logoutResult

    fun setUser(user: User) {
        _user.postValue(user)
        _isFollow.postValue(user.isFollow)
    }

    fun setFollow(isFollow: Boolean) {
        _isFollow.postValue(isFollow)
    }

    fun getUserInfo(userId: Int) {
        val formBody = FormBody.Builder()
            .add("userId", userId!!.toString())
            .build()

        HttpClient.send("/user/getUserInfo", formBody, object : Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.d("NotificationsFragment", "onFailure: " + e.toString())
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val json = response.body?.string()

                if (json != null) {
                    Log.d("NotificationsFragment", json)
                }
                else {
                    Log.d("NotificationsFragment", "json is null")
                }

                val gson = Gson()
                val user = gson.fromJson(json, User::class.java)
                setUser(user)
            }
        })
    }

    fun follow(userId: Int) {
        val formBody = FormBody.Builder()
            .add("toId", userId.toString())
            .build()

        HttpClient.send("/follow/followUser", formBody, object : Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.d("NotificationsFragment", "onFailure: " + e.toString())
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                _isFollow.postValue(true)
            }
        })
    }

    fun cancelFollow(userId: Int) {
        val formBody = FormBody.Builder()
           .add("toId", userId.toString())
           .build()

        HttpClient.send("/follow/cancelFollowUser", formBody, object : Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.d("NotificationsFragment", "onFailure: " + e.toString())
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                _isFollow.postValue(false)
            }
        })
    }

    fun logout() {
        HttpClient.send("/user/logout", object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                if (json!= null) {
                    val jsonObject = JSONObject(json)
                    if (jsonObject.getBoolean("result")) {
                        _logoutResult.postValue(true)
                    }
                    else {
                        _logoutResult.postValue(false)
                    }
                }
            }
        })
    }

    fun setLogoutResult(result: Boolean) {
        _logoutResult.postValue(result)
    }
}