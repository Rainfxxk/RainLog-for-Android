package com.rain.rainlog.ui.application

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.rain.rainlog.ui.main.UserViewModel

class RainLogApplication: Application() {
    private lateinit var userViewModel: UserViewModel

    override fun onCreate() {
        super.onCreate()
        userViewModel = ViewModelProvider.AndroidViewModelFactory(this).create(UserViewModel::class.java)
    }

    public fun getUserViewModel(): UserViewModel {
        return userViewModel
    }
}