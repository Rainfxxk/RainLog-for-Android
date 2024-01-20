package com.rain.rainlog.ui.page.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rain.rainlog.data.model.Post

class PagePostViewModel : ViewModel() {
    private val _postList = MutableLiveData<MutableList<Post>>(mutableListOf())
    val postList: LiveData<MutableList<Post>> = _postList

    fun clearPostList() {
        _postList.value?.clear()
    }

    fun removePost(postId: Int) {
        _postList.value?.removeIf { it.postId == postId }
    }

    fun addPost(post: Post) {
        _postList.value?.add(post)
    }

    fun isPostListEmpty(): Boolean {
        return _postList.value?.isEmpty()?: true
    }
}