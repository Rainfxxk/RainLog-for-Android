package com.rain.rainlog.ui.captchalogin

import com.rain.rainlog.ui.login.LoggedInUserView

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null,
    val fail: Boolean? = null
)