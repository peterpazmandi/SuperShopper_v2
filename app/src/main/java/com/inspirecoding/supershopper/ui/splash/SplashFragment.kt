package com.inspirecoding.supershopper.ui.splash

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.SplashFragmentBinding
import com.inspirecoding.supershopper.notification.FirebaseService
import com.inspirecoding.supershopper.utils.Status
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.splash_fragment) {

    private lateinit var binding : SplashFragmentBinding
    private val viewModel by viewModels<SplashViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SplashFragmentBinding.bind(view)

        setupSplashEvents()
        processUserLoggedInResult()
        checkUserLoggedIn()

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
                when(userResource.status)
                {
                    Status.SUCCESS ->  {
                        val currentUser = userResource.data

                        if(currentUser != null) {
                            viewModel.updateFirebaseInstanceTokenOFUserInFirestore(currentUser)
                        } else {
                            binding.motionLayout.transitionToState(R.id.end)
                            binding.progressBar.makeItInVisible()
                        }
                    }
                    Status.LOADING ->  {
                        binding.progressBar.makeItVisible()
                    }
                    Status.ERROR ->  {
                        userResource.message?.let {
                            viewModel.onShowErrorMessage(it)
                        }
                        binding.progressBar.makeItInVisible()
                    }
                }
            }
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
                    is SplashViewModel.SplashEvent.TurnNightMode -> {
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
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            delay(500)
            val action = SplashFragmentDirections.actionSplashFragmentToShoppingListsFragment(user)
            findNavController().navigate(action)
        }
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = SplashFragmentDirections.actionSplashFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }
}