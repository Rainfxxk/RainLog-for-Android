package com.rain.rainlog.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rain.rainlog.data.model.User

class UserViewModel: ViewModel() {
    val loginState = MutableLiveData<Boolean>(false)
    val user = MutableLiveData<User>()
}