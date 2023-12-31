package com.example.mobprog_tp2.ui.Form

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val nameError: Int? = null,
    val hobbyError: Int? = null,
    val isDataValid: Boolean = false
)