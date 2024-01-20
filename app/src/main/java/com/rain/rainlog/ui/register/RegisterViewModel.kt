package com.rain.rainlog.ui.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel: ViewModel() {

    var addressState = MutableLiveData<Boolean>()
    var captchaState = MutableLiveData<Boolean>()
    var userNameState = MutableLiveData<Boolean>()
    var passwordState = MutableLiveData<Boolean>()
    var confirmPasswordState = MutableLiveData<Boolean>()
    var registerState = MutableLiveData<Boolean>()
    val second = MutableLiveData<Int>()

    fun checkRegisterState() {
        if (addressState.value == true && captchaState.value == true && passwordState.value == true && confirmPasswordState.value == true) {
            registerState.postValue(true)
        }
    }
}