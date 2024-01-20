package com.rain.rainlog.ui.content.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rain.rainlog.data.model.Comment
import com.rain.rainlog.data.model.Post

class PostViewModel : ViewModel(){
    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post

    private val _commentList = MutableLiveData<MutableList<Comment>>(mutableListOf())
    val commentList: LiveData<MutableList<Comment>> = _commentList

    fun setPost(post: Post) {
        _post.postValue(post)
    }

    fun addComment(comment: Comment) {
        _commentList.value?.add(comment)
    }

    fun removeComment(position: Int) {
        _commentList.value?.removeAt(position)
    }

    fun ifCommentListEmpty(): Boolean {
        return _commentList.value?.isEmpty() == true
    }
}