package com.rain.rainlog.ui.captchalogin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.rain.rainlog.data.model.User
import com.rain.rainlog.http.HttpClient
import com.rain.rainlog.http.HttpConfig
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class CaptchaLoginViewModel() : ViewModel(){
    val addressState = MutableLiveData<Boolean>()
    val captchaState = MutableLiveData<Boolean>()
    val sendCaptchaState = MutableLiveData<Boolean>()
    val loginState = MutableLiveData<Boolean>()
    val second = MutableLiveData<Int>()

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult
    private val _userInfo = MutableLiveData<User>()
    val userInfo: LiveData<User> = _userInfo

    fun login(address: String, captcha: String) {
        var formBody = FormBody.Builder()
            .add("type", "email")
            .add("address", address)
            .add("captcha", captcha)
            .build()

        HttpClient.send("/user/loginByCaptcha", formBody, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                if (HttpConfig.SESSIONID == null) {
                    var headers = response.headers;
                    var cookies = headers.values("Set-Cookie");
                    var session = cookies.get(0);
                    var sessionId = session.substring(0, session.indexOf(";"));

                    HttpConfig.SESSIONID = sessionId
                }

                val jsonStr = response.body?.string()
                var jsonObject = JSONObject(jsonStr)


                val loginResult = jsonObject.getBoolean("loginResult")
                if (loginResult) {
                    val user = Gson().fromJson<User>(jsonObject.getJSONObject("user").toString(), User::class.java)
                    _userInfo.postValue(user)
                }
                _loginResult.postValue(loginResult)
            }
        })
    }
}