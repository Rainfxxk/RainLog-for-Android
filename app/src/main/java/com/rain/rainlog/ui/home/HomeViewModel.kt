package com.rain.rainlog.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rain.rainlog.data.model.Post

class HomeViewModel : ViewModel() {

    private val _pageNum = MutableLiveData<Int>(0)
    val pageNum: LiveData<Int> = _pageNum

    private val _nextPage = MutableLiveData<Boolean>(true)
    val nextPage: LiveData<Boolean> = _nextPage

    private val _contentList = MutableLiveData<MutableList<Post>>(mutableListOf())
    val contentList: LiveData<MutableList<Post>> = _contentList

    fun setNextPage(nextPage: Boolean) {
        _nextPage.postValue(nextPage)
    }

    fun incPage() {
        _pageNum.postValue(_pageNum.value?.plus(1) ?: 0)
    }

    fun refreshPage() {
        _pageNum.postValue(0)
    }

    fun addContent(post: Post) {
        _contentList.value?.add(post)
    }

    fun clearContentList() {
        _contentList.value?.clear()
    }
}