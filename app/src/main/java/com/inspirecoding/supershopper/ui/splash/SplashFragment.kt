package com.inspirecoding.supershopper.ui.splash

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.SplashFragmentBinding
import com.inspirecoding.supershopper.ui.register.RegisterFragmentDirections
import com.inspirecoding.supershopper.ui.register.RegisterViewModel
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.splash_fragment) {

    private lateinit var binding : SplashFragmentBinding
    private val viewModel by viewModels<SplashViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SplashFragmentBinding.bind(view)
        binding.apply {
            tvWelcome
        }

        setupSplashEvents()
        checkUserLoggedIn()
        processUserLoggedInResult()

        binding.btnCreateAccount.setOnClickListener {
            viewModel.onRegistrationSelected()
        }
        binding.tvLogIn.setOnClickListener {
            viewModel.onLoginSelected()
        }
    }

    private fun checkUserLoggedIn() {
        viewModel.checkUserLoggedIn()
    }

    private fun processUserLoggedInResult() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.userResource.collect { userResource ->
                when(userResource)
                {
                    is Resource.Success ->  {
                        handleUserLoggedInResult(userResource.data)
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

    private fun handleUserLoggedInResult(user: User?) {
        if (user != null) {
            viewModel.onNavigateToShoppingListsFragment(user)
        } else {
            binding.motionLayout.transitionToState(R.id.end)
        }
    }

    private fun setupSplashEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.splashEventChannel.collect { event ->
                when(event)
                {
                    SplashViewModel.SplashEvent.NavigateToLoginFragment ->  {
                        navigateToLoginFragment()
                    }
                    SplashViewModel.SplashEvent.NavigateToRegisterFragment ->  {
                        navigateToRegisterFragment()
                    }
                    is SplashViewModel.SplashEvent.NavigateToShoppingListsFragment ->  {
                        navigateToShoppingListsFragment(event.user)
                    }
                    is SplashViewModel.SplashEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }

    /** Navigation methods **/
    private fun navigateToLoginFragment() {
        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
    }
    private fun navigateToRegisterFragment() {
        findNavController().navigate(R.id.action_splashFragment_to_registerFragment)
    }
    private fun navigateToShoppingListsFragment(user: User) {
        val action = SplashFragmentDirections.actionSplashFragmentToShoppingListsFragment(user)
        findNavController().navigate(action)
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = SplashFragmentDirections.actionSplashFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }
}