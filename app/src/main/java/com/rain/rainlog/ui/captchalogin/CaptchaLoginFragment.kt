package com.rain.rainlog.ui.captchalogin

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.rain.rainlog.R
import com.rain.rainlog.databinding.FragmentCaptchaLoginBinding
import com.rain.rainlog.http.HttpClient
import com.rain.rainlog.http.HttpConfig
import com.rain.rainlog.ui.main.UserViewModel
import com.rain.rainlog.ui.login.afterTextChanged
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Response
import java.io.IOException
import java.util.Timer
import java.util.TimerTask

class CaptchaLoginFragment : Fragment() {

    private lateinit var binding: FragmentCaptchaLoginBinding
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var captchaLoginViewModel: CaptchaLoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        captchaLoginViewModel = CaptchaLoginViewModel()
        binding = FragmentCaptchaLoginBinding.inflate(layoutInflater)

        var address = binding.address
        var captcha = binding.captcha
        var sendCaptcha = binding.sendCaptcha
        var userNameLogin = binding.userNameLogin
        var register = binding.register
        var login = binding.login

        captchaLoginViewModel.addressState.observe(viewLifecycleOwner, Observer {
            var state = it

            if (state) {
                captchaLoginViewModel.sendCaptchaState.postValue(true)
                if (captchaLoginViewModel.captchaState.value == true) {
                    captchaLoginViewModel.loginState.postValue(true)
                }
            }
            else {
                address.error = "格式错误"
                captchaLoginViewModel.loginState.postValue(false)
            }
        })

        captchaLoginViewModel.sendCaptchaState.observe(viewLifecycleOwner, Observer {
            sendCaptcha.isEnabled = it
        })

        captchaLoginViewModel.second.observe(viewLifecycleOwner, Observer {
            sendCaptcha.text = it.toString()
        })

        captchaLoginViewModel.captchaState.observe(viewLifecycleOwner, Observer {
            var state = it

            if (state) {
                if (captchaLoginViewModel.addressState.value == true) {
                    captchaLoginViewModel.loginState.postValue(true)
                }
            }
            else {
                captcha.error = "请输入六位验证码"
                captchaLoginViewModel.loginState.postValue(false)
            }
        })

        captchaLoginViewModel.loginState.observe(viewLifecycleOwner, Observer {
            login.isEnabled = it
        })


        captchaLoginViewModel.loginResult.observe(viewLifecycleOwner, Observer {
            val loginResult = it ?: return@Observer

            //loading.visibility = View.GONE

            if (loginResult) {
                userViewModel.loginState.postValue(true)
                userViewModel.user.postValue(captchaLoginViewModel.userInfo.value)
                activity?.runOnUiThread {
                    val navController = findNavController()
                    val startDestination = navController.graph.startDestinationId
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()
                    navController.navigate(startDestination, null, navOptions)
                }
            }
            else {
                activity?.runOnUiThread {
                    Toast.makeText(activity, "验证码错误", Toast.LENGTH_SHORT).show()
                }
            }

        })

        address.afterTextChanged {
            val addressStr = address.text.toString()
            captchaLoginViewModel.addressState.postValue(checkAddress(addressStr))
        }

        captcha.afterTextChanged {
            val captchaStr = captcha.text.toString()
            captchaLoginViewModel.captchaState.postValue(checkCaptcha(captchaStr))
        }

        sendCaptcha.setOnClickListener {
            var addressStr = address.text.toString()
            var type = if (addressStr.contains('@')) "email" else "sms"

            var formBody = FormBody.Builder()
                .add("address", addressStr)
                .add("type", type)
                .build()

            HttpClient.send("/user/sendCaptcha", formBody, object: Callback {
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
            captchaLoginViewModel.second.postValue(60)

            var timer = Timer()
            timer.schedule(object: TimerTask() {
                override fun run() {
                    val second = captchaLoginViewModel.second.value?.minus(1)
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
                    captchaLoginViewModel.second.postValue(second)
                }

            }, 1000, 1000)
        }

        login.setOnClickListener {
            captchaLoginViewModel.login(address.text.toString(), captcha.text.toString())
        }

        userNameLogin?.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.login)
        }

        register?.setOnClickListener {
            findNavController().navigate(R.id.register)
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}