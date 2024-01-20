package com.rain.rainlog.data.model

import java.io.Serializable

class Post : Serializable
{
    var postId: Int = 0
    lateinit var content: String
    var imagePath: String? = null
    lateinit var publishTime: String
    var userId: Int = 0
    var commentNum: Int = 0
    var isBookmark: Boolean = false
    var bookmarkNum: Int = 0
    var isLike: Boolean = false
    var likeNum: Int = 0
    lateinit var user: User
}