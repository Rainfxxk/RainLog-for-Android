package com.rain.rainlog.ui.page.bookmark

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rain.rainlog.data.model.Post

class PageBookmarkViewModel : ViewModel(){
    private val _bookmarkList = MutableLiveData<MutableList<Post>>(mutableListOf())
    val bookmarkList: MutableLiveData<MutableList<Post>> = _bookmarkList

    fun clearBookmarkList(){
        _bookmarkList.value?.clear()
    }

    fun addPost(post: Post){
        _bookmarkList.value?.add(post)
    }

    fun isBookmarkListEmpty(): Boolean {
        return _bookmarkList.value?.isEmpty() == true
    }
}