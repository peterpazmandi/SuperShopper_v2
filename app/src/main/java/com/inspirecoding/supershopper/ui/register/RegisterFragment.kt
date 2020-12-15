package com.inspirecoding.supershopper.ui.register

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
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.RegisterFragmentBinding
import com.inspirecoding.supershopper.utils.dismissKeyboard
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.register_fragment) {

    private val TAG = this.javaClass.simpleName

    private val viewModel by viewModels<RegisterViewModel>()

    private lateinit var binding : RegisterFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = RegisterFragmentBinding.bind(view)
        binding.apply {
            tietUsername.setText(viewModel.username)
            tietEmailAddress.setText(viewModel.email)
        }

        fieldAnimationHandler()

        setupTextChangeListeners()

        binding.tvLogInNow.setOnClickListener {
            viewModel.onLoginSelected()
        }
        binding.chbTerms.setOnClickListener {
            viewModel.onTermsAndConditionSelected()
            removeEditTextfocus()
        }
        binding.chbPrivacyPolicy.setOnClickListener {
            viewModel.onPrivacyPolicySelected()
            removeEditTextfocus()
        }
        binding.btnRegister.setOnClickListener {
            context?.dismissKeyboard(it)
            if (viewModel.areTheFieldsValid()) {
                viewModel.registerUser()
            }
        }
        binding.btnFacebook.setOnClickListener {
            viewModel.signInWithFacebook(this)
        }
        binding.btnGoogle.setOnClickListener {
            (activity as MainActivity).let { activity ->
                viewModel.signInWithGoogle(activity)
            }
        }

        processRegistrationResult()
        setupRegistrationEvents()
    }

    private fun setupRegistrationEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {

            viewModel.registrationEventChannel.collect { event ->
                when(event)
                {
                    RegisterViewModel.RegistrationEvent.NavigateToLoginFragment -> {
                        navigateToLoginFragment()
                    }
                    RegisterViewModel.RegistrationEvent.NavigateToTermsAndConditionFragment -> {
                        navigateToTermsAndConditionFragment()
                    }
                    RegisterViewModel.RegistrationEvent.NavigateToPrivacyPolicyFragment -> {
                        navigateToPrivacyPolicyFragment()
                    }
                    is RegisterViewModel.RegistrationEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                    is RegisterViewModel.RegistrationEvent.RegistrationCompletedEvent -> {
                        navigateToShoppingListsFragment(event.user)
                    }
                }
            }

        }
    }

    private fun processRegistrationResult() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.userResource.collect { userResource ->
                when(userResource)
                {
                    is Resource.Success ->  {
                        userResource.data?.let {
                            viewModel.onSuccessfulRegistration(it)
                        }
                        binding.progressBar.makeItInVisible()
                    }
                    is Resource.Loading ->  {
                        binding.progressBar.makeItVisible()
                    }
                    is Resource.Error ->  {
                        userResource.message?.let {
                            viewModel.onShowErrorMessage(it)
                        }
                        binding.progressBar.makeItInVisible()
                    }
                }
            }
        }
    }

    private fun setupTextChangeListeners() {
        binding.tietUsername.addTextChangedListener { _username ->
            viewModel.username = _username.toString().trim()
        }
        binding.tietEmailAddress.addTextChangedListener { _emailAddress ->
            viewModel.email = _emailAddress.toString().trim()
        }
        binding.tietPassword.addTextChangedListener { _password ->
            viewModel.password = _password.toString().trim()
        }
    }

    private fun fieldAnimationHandler() {
        binding.tietUsername.setOnFocusChangeListener { tietEmailAddress, isFocused ->
            if (isFocused) binding.motionLayout.transitionToState(R.id.usernameSelected)
        }
        binding.tietEmailAddress.setOnFocusChangeListener { tietEmailAddress, isFocused ->
            if (isFocused) binding.motionLayout.transitionToState(R.id.emailSelected)
        }
        binding.tietPassword.setOnFocusChangeListener { tietPassword, isFocused ->
            if(isFocused) binding.motionLayout.transitionToState(R.id.passwordSelected)
        }
    }
    private fun removeEditTextfocus() {
        binding.motionLayout.transitionToState(R.id.startState)
        binding.tietUsername.clearFocus()
        binding.tietEmailAddress.clearFocus()
        binding.tietPassword.clearFocus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }


    /** Navigation methods **/
    private fun navigateToTermsAndConditionFragment() {
        findNavController().navigate(R.id.action_registerFragment_to_termsAndConditionsFragment)
    }
    private fun navigateToPrivacyPolicyFragment() {
        findNavController().navigate(R.id.action_registerFragment_to_privacyPolicyFragment)
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = RegisterFragmentDirections.actionRegisterFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }
    private fun navigateToShoppingListsFragment(user: User) {
        findNavController().popBackStack(R.id.loginFragment, true)
        val action = RegisterFragmentDirections.actionRegisterFragmentToShoppingListsFragment(user)
        findNavController().navigate(action)
    }
    private fun navigateToLoginFragment() {
        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
    }
}