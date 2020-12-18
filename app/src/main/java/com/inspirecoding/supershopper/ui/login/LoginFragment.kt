package com.inspirecoding.supershopper.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.MainActivity
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.LoginFragmentBinding
import com.inspirecoding.supershopper.utils.Status
import com.inspirecoding.supershopper.utils.dismissKeyboard
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.login_fragment) {

    private val viewModel by viewModels<LoginViewModel>()

    private lateinit var binding: LoginFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LoginFragmentBinding.bind(view)

        fieldAnimationHandler()

        setupTextChangeListeners()
        setupLoginEvents()
        processLoginResult()

        binding.tvRegisterNow.setOnClickListener {
            viewModel.onRegistrationSelected()
        }
        binding.tvForgotPassword.setOnClickListener {
            viewModel.onForgotPasswordSelected()
        }
        binding.btnLogIn.setOnClickListener {
            context?.dismissKeyboard(it)
            if (viewModel.validateFields()) {
                viewModel.loginUser()
            }
        }
        binding.btnFacebook.setOnClickListener {
            viewModel.signInWithFacebook(this)
        }
        binding.btnGoogle.setOnClickListener {
            (activity as MainActivity).let {
                viewModel.signInWithGoogle(it)
            }
        }
    }

    private fun setupTextChangeListeners() {
        binding.tietEmailAddress.addTextChangedListener { _emailAddress ->
            viewModel.email = _emailAddress.toString().trim()
        }
        binding.tietPassword.addTextChangedListener { _password ->
            viewModel.password = _password.toString().trim()
        }
    }


    private fun fieldAnimationHandler() {
        binding.tietEmailAddress.setOnFocusChangeListener { tietEmailAddress, isFocused ->
            if (isFocused) binding.motionLayout.transitionToState(R.id.emailSelected)
        }
        binding.tietPassword.setOnFocusChangeListener { tietPassword, isFocused ->
            if (isFocused) binding.motionLayout.transitionToState(R.id.passwordSelected)
        }
    }

    private fun processLoginResult() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.userResource.collect {  userResource ->
                when (userResource.status) {
                    Status.LOADING -> {
                        binding.progressBar.makeItVisible()
                    }
                    Status.SUCCESS -> {
                        binding.progressBar.makeItInVisible()
                        userResource.data?.let { _user ->
                            viewModel.onSuccessfulLogin(_user)
                        }
                    }
                    Status.ERROR -> {
                        binding.progressBar.makeItInVisible()
                        userResource.message?.let {
                            viewModel.onShowErrorMessage(userResource.message)
                        }
                    }
                }
            }
        }
    }

    private fun setupLoginEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.loginEventChannel.collect { event ->
                when(event)
                {
                    LoginViewModel.LoginEvent.NavigateToRegisterFragment -> {
                        navigateToRegisterFragment()
                    }
                    LoginViewModel.LoginEvent.NavigateToForgotPasswordDialog ->  {
                        openForgotPasswordDialog()
                    }
                    is LoginViewModel.LoginEvent.ShowErrorMessage ->  {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                    is LoginViewModel.LoginEvent.LoginCompletedEvent ->  {
                        navigateToShoppingListsFragment(event.user)
                    }
                }
            }

        }
    }


    /** Navigation methods **/
    private fun openForgotPasswordDialog() {
        findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordDialog)
    }

    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action =
            LoginFragmentDirections.actionLoginFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }

    private fun navigateToRegisterFragment() {
        findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
    }

    private fun navigateToShoppingListsFragment(user: User) {
        findNavController().popBackStack(R.id.registerFragment, true)
        val action = LoginFragmentDirections.actionLoginFragmentToShoppingListsFragment(user)
        findNavController().navigate(action)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

}