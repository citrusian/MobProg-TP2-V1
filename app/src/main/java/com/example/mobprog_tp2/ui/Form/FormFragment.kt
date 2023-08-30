package com.example.mobprog_tp2.ui.Form

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.example.mobprog_tp2.databinding.FragmentFormBinding

import com.example.mobprog_tp2.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class FormFragment : Fragment() {

    private var _binding: FragmentFormBinding? = null
    private val binding get() = _binding!!

    // Using Login Preset for validation
    private lateinit var loginViewModel: LoginViewModel

    // Output Message (Local Session)
    private lateinit var nameOutput: TextView
    private lateinit var hobbyOutput: TextView
    private lateinit var namaAllPageOutput: TextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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

        // Use Login func to check input
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
                loginResult.error?.let {
                    showLoginFailed(it)
                }
                loginResult.success?.let {
                    updateUiWithUser(nameEditText.text.toString(), hobbyEditText.text.toString())
                }
            })


        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
                // it seems Android Studio prefer using "android:hint" from .xml
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
            loginViewModel.login(
                nameEditText.text.toString(),
                hobbyEditText.text.toString()
            )
        }
    }


    private fun updateUiWithUser(name: String, hobby: String) {
        val nameOutputStr = getString(R.string.action_name_message) + "\n" + name
        val hobbyOutputStr = getString(R.string.action_hobby_message) + "\n" + hobby

        // Name and Hobby Visibility
        nameOutput.text = nameOutputStr
        nameOutput.visibility = View.VISIBLE

        hobbyOutput.text = hobbyOutputStr
        hobbyOutput.visibility = View.VISIBLE

        // Name String on all page Visibility
        namaAllPageOutput.text = name
        namaAllPageOutput.visibility = View.VISIBLE

        /*
         Toast Manual
         https://developer.android.com/reference/android/widget/Toast
         */
        val appContext = context?.applicationContext ?: return
        val toast = Toast.makeText(appContext, nameOutputStr, Toast.LENGTH_LONG)
        val xOffset = 0 // Horizontal offset
        val yOffset = getBottomNavHeight()  // Get Vertical offset from BottomNav
        // Note : Doesn't work on smaller screen
        toast.setGravity(Gravity.BOTTOM, xOffset, yOffset)
        toast.show()
    }

    private fun getBottomNavHeight(): Int {
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        return bottomNav.height
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        val toast = Toast.makeText(appContext, errorString, Toast.LENGTH_LONG)
        val xOffset = 0 // Horizontal offset
        val yOffset = getBottomNavHeight()
        toast.setGravity(Gravity.BOTTOM, xOffset, yOffset)
        toast.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}