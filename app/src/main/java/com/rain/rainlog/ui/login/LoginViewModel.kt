package com.rain.rainlog.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.google.gson.Gson
import com.rain.rainlog.data.LoginRepository

import com.rain.rainlog.R
import com.rain.rainlog.data.model.User
import com.rain.rainlog.http.HttpClient
import com.rain.rainlog.http.HttpConfig
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _userInfo = MutableLiveData<User>()
    val userInfo: LiveData<User> = _userInfo

    fun login(username: String, password: String) {
        var formBody = FormBody.Builder()
            .add("account", username)
            .add("password", password)
            .build()

        HttpClient.send("/user/login", formBody, object : Callback {
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

                if (jsonStr != null) {
                    Log.d("login", jsonStr)
                }

                if (loginResult) {
                    val user = Gson().fromJson<User>(jsonObject.getString("user"), User::class.java)
                    _userInfo.postValue(user)
                }
                else {
                }

                _loginResult.postValue(loginResult)
            }
        })
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}