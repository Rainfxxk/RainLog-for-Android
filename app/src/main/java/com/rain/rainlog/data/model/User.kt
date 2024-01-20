package com.rain.rainlog.data.model

import java.io.Serializable

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
class User : Serializable {
    var userId: Int = 0
    lateinit var userName: String
    lateinit var avatarPath: String
    var email: String? = null
    var telephone: String? = null
    lateinit var personalitySignature: String
    var fanNum: Int? = null
    var followNum: Int? = null
    var isFollow: Boolean = false
}