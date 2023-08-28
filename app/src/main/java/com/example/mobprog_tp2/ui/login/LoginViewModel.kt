package com.example.mobprog_tp2.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.mobprog_tp2.data.LoginRepository
import com.example.mobprog_tp2.data.Result

import com.example.mobprog_tp2.R

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, hobby: String) {
        val result = loginRepository.login(username, hobby)

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun loginDataChanged(username: String, hobby: String) {
        if (!isNameValid(username)) {
            _loginForm.value = LoginFormState(nameError = R.string.invalid_name)
        } else if (!isHobbyValid(hobby)) {
            _loginForm.value = LoginFormState(hobbyError = R.string.invalid_hobby)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // check if input has symbol
    private fun isNameValid(username: String): Boolean {
        // Android cant using literal array [] ?
//        val specialCharacters = ['@', '&']
        val specialCharacters = arrayOf("@", "#", "$", "%", "^", "&")
        return if (specialCharacters.any { it in username }) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    private fun isHobbyValid(hobby: String): Boolean {
        val specialCharacters = arrayOf("@", "#", "$", "%", "^", "&")
        return if (specialCharacters.any { it in hobby }) {
            Patterns.EMAIL_ADDRESS.matcher(hobby).matches()
        } else {
            hobby.isNotBlank()
        }
    }
}