package com.example.mobprog_tp2.ui.login

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.mobprog_tp2.databinding.FragmentFormBinding

import com.example.mobprog_tp2.R

class FormFragment : Fragment() {

    // validation in here
    private lateinit var loginViewModel: LoginViewModel
    private var _binding: FragmentFormBinding? = null

    // Ouput Message (Local)
    // !! note IntelliJ IDE keeps outputting warning if using Uppercase even when user-warning === suppressed
    private lateinit var nameOutput: TextView
    private lateinit var hobbyOutput: TextView
    private lateinit var namaAllPageOutput: TextView

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFormBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameOutput = binding.nameOutput
        hobbyOutput = binding.hobbyOutput
        namaAllPageOutput = activity?.findViewById(R.id.namaAllPageOutput) ?: TextView(context)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        val nameEditText = binding.name
        val hobbyEditText = binding.hobby
        val loginButton = binding.login
//        val loadingProgressBar = binding.loading

        loginViewModel.loginFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                loginButton.isEnabled = loginFormState.isDataValid
                loginFormState.nameError?.let {
                    nameEditText.error = getString(it)
                }
                loginFormState.hobbyError?.let {
                    hobbyEditText.error = getString(it)
                }
            })

        loginViewModel.loginResult.observe(viewLifecycleOwner,
            Observer { loginResult ->
                loginResult ?: return@Observer
//                loadingProgressBar.visibility = View.GONE
                loginResult.error?.let {
                    showLoginFailed(it)
                }
                loginResult.success?.let {
//                    updateUiWithUser(it)
                    updateUiWithUser(it, nameEditText.text.toString(), hobbyEditText.text.toString())
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(
                    nameEditText.text.toString(),
                    hobbyEditText.text.toString()
                )
            }
        }
        nameEditText.addTextChangedListener(afterTextChangedListener)
        hobbyEditText.addTextChangedListener(afterTextChangedListener)
        hobbyEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(
                    nameEditText.text.toString(),
                    hobbyEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
//            loadingProgressBar.visibility = View.VISIBLE
            loginViewModel.login(
                nameEditText.text.toString(),
                hobbyEditText.text.toString()
            )
        }
    }


    private fun updateUiWithUser(model: LoggedInUserView, name: String, hobby: String) {
//        val welcome = getString(R.string.welcome) + model.displayName
        val nameOutputStr = getString(R.string.action_name_message) + name
        val hobbyOutputStr = getString(R.string.action_hobby_message) + hobby

        // Text Output & make textview visible
        nameOutput.text = nameOutputStr
        nameOutput.visibility = View.VISIBLE

        hobbyOutput.text = hobbyOutputStr
        hobbyOutput.visibility = View.VISIBLE

        namaAllPageOutput.text = name
        namaAllPageOutput.visibility = View.VISIBLE

        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, nameOutputStr, Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}