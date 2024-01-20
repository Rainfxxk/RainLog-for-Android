package com.rain.rainlog.data.model

class Comment {
    var commentId: Int = 0
    lateinit var commentContent: String
    lateinit var commentTime: String
    lateinit var topicType: String
    var topicId: Int = 0
    var authorId: Int = 0
    var userId: Int = 0
    lateinit var user: User
}