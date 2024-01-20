package com.rain.rainlog.data

import android.util.Log
import com.rain.rainlog.data.model.User
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(userName: String, password: String): Result<User> {
        try {
            return Result.Fail
        } catch (e: Throwable) {
            Log.d("LoginActivity", e.toString())

            return Result.Error(IOException("Error logging in", e))
        }

    }

    fun logout() {
        // TODO: revoke authentication
    }
}