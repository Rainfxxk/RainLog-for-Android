package com.rain.rainlog.ui.page.follow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rain.rainlog.data.model.User

class PageFollowViewModel : ViewModel() {
    private val _followList = MutableLiveData<MutableList<User>>(mutableListOf())
    val followList: MutableLiveData<MutableList<User>> = _followList

    fun clearFollowList() {
        _followList.value?.clear()
    }

    fun addUser(user: User) {
        _followList.value?.add(user)
    }

    fun isFollowListEmpty(): Boolean {
        return _followList.value?.isEmpty() == true
    }
}