package com.rain.rainlog.ui.register

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.rain.rainlog.R
import com.rain.rainlog.databinding.FragmentRegisterBinding
import com.rain.rainlog.http.HttpClient
import com.rain.rainlog.http.HttpConfig
import com.rain.rainlog.ui.main.UserViewModel
import com.rain.rainlog.ui.login.afterTextChanged
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Timer
import java.util.TimerTask

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        registerViewModel = RegisterViewModel()

        binding = FragmentRegisterBinding.inflate(layoutInflater)

        val address = binding.address
        val sendCaptcha = binding.sendCaptcha
        val captcha = binding.captcha
        val userName = binding.userName
        val password = binding.password
        val confirmPassword = binding.comfirmPassword
        val login = binding.login
        val register = binding.register

        registerViewModel.addressState.observe(viewLifecycleOwner, Observer {
            sendCaptcha.isEnabled = it
            if (it) {
                registerViewModel.checkRegisterState()
            }
            else {
                address.error = "格式错误"
            }
        })

        registerViewModel.captchaState.observe(viewLifecycleOwner, Observer {
            if (it) {
                registerViewModel.checkRegisterState()
            }
            else {
                captcha.error = "请输入六位验证码"
            }
        })

        registerViewModel.second.observe(viewLifecycleOwner, Observer {
            sendCaptcha.text = it.toString()
        })

        registerViewModel.userNameState.observe(viewLifecycleOwner, Observer {
            if (it) {
                registerViewModel.checkRegisterState()
            }
            else {
                userName.error = "该用户名已存在"
            }
        })

        registerViewModel.passwordState.observe(viewLifecycleOwner, Observer {
            if (it) {
                registerViewModel.checkRegisterState()
            }
            else {
                password.error = "至少包含字母、数字、特殊字符，1-9位"
            }
        })

        registerViewModel.confirmPasswordState.observe(viewLifecycleOwner, Observer {
            if (it) {
                registerViewModel.checkRegisterState()
            }
            else {
                confirmPassword.error = "两次密码不一致"
            }
        })

        registerViewModel.registerState.observe(viewLifecycleOwner, Observer {
            register.isEnabled = it
        })

        address.afterTextChanged {
            registerViewModel.addressState.postValue(checkAddress(address.text.toString()))
        }

        captcha.afterTextChanged {
            registerViewModel.captchaState.postValue(checkCaptcha(captcha.text.toString()))
        }

        userName.afterTextChanged {
            checkUserName(userName.text.toString(), registerViewModel.userNameState)
        }

        password.afterTextChanged {
            registerViewModel.passwordState.postValue(checkPassword(password.text.toString()))
        }

        confirmPassword.afterTextChanged {
            registerViewModel.confirmPasswordState.postValue(checkConfirmPassowrd(password.text.toString(), confirmPassword.text.toString()))
        }

        login.setOnClickListener {
            findNavController().navigate(R.id.login)
        }

        sendCaptcha.setOnClickListener {
            var addressStr = address.text.toString()
            var type = if (addressStr.contains('@')) "email" else "sms"

            var formBody = FormBody.Builder()
                .add("address", addressStr)
                .add("type", type)
                .build()

            HttpClient.send("/user/sendCaptcha", formBody, object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                }

                override fun onResponse(call: Call, response: Response) {
                    if (HttpConfig.SESSIONID == null) {
                        var headers = response.headers;
                        Log.d("info_headers", "header " + headers);
                        var cookies = headers.values("Set-Cookie");
                        var session = cookies.get(0);
                        Log.d("info_cookies", "onResponse-size: " + cookies);
                        var sessionId = session.substring(0, session.indexOf(";"));

                        HttpConfig.SESSIONID = sessionId
                    }
                }
            })

            sendCaptcha.isEnabled = false
            registerViewModel.second.postValue(60)

            var timer = Timer()
            timer.schedule(object: TimerTask() {
                override fun run() {
                    val second = registerViewModel.second.value?.minus(1)
                    Log.d("CaptchaLoginActivity", "timer")
                    if (second == 0) {
                        activity?.runOnUiThread {
                            sendCaptcha.text = "发送验证码"
                        }
                        timer.cancel()
                        activity?.runOnUiThread {
                            sendCaptcha.isEnabled = true
                        }
                        return
                    }
                    registerViewModel.second.postValue(second)
                }

            }, 1000, 1000)
        }

        register.setOnClickListener {
            Log.d("RegisterActivity", "register")
            var type = if(address.text.toString().contains('@')) "email" else "sms"

            var formBody = FormBody.Builder()
                .add("address", address.text.toString())
                .add("type", type)
                .add("captcha", captcha.text.toString())
                .add("password", password.text.toString())
                .add("userName", userName.text.toString())
                .build()

            HttpClient.send("/user/register", formBody, object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val jsonObject = JSONObject(response.body?.string())

                        if (jsonObject.getBoolean("result")) {
                            val navOptions = NavOptions.Builder()
                                .setPopUpTo(R.id.login, true)
                                .build()
                            findNavController().navigate(R.id.login, null, navOptions)
                        } else {
                            activity?.runOnUiThread {
                                Toast.makeText(
                                    activity,
                                    "验证码错误",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    catch (e: Exception) {
                        Log.d("RegisterActivity", e.stackTraceToString())
                    }

                }
            })
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun checkUserName(username: String, usernameState: MutableLiveData<Boolean>) {
        var formBody = FormBody.Builder()
            .add("userName", username)
            .build()

        HttpClient.send("/user/isUserNameExit", formBody, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                var jsonObserver = JSONObject(response.body?.string())
                usernameState.postValue(jsonObserver.getBoolean("result"))
            }
        })
    }

    private fun checkAddress(address: String): Boolean {
        if (address.contains('@')) {
            return Patterns.EMAIL_ADDRESS.matcher(address).matches()
        } else {
            return address.length == 11
        }
    }

    private fun checkCaptcha(captcha: String): Boolean {
        return captcha.length == 6
    }

    private fun checkPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%\\.*#?&])[A-Za-z\\d$@$!\\.%*#?&]{8,}$")
        return password.contains(regex)
    }

    private fun checkConfirmPassowrd(password: String, comfirmPassword: String):Boolean {
        return password.equals(comfirmPassword)
    }
}