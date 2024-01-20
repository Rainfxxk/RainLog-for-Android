package com.rain.rainlog.ui.captchalogin

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val addressState: Boolean? = null,
    val captchaState: Boolean? = null,
    val isDataValid: Boolean = false
)