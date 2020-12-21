package com.inspirecoding.supershopper.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.SettingsFragmentBinding
import com.inspirecoding.supershopper.ui.register.RegisterFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.settings_fragment) {

    private val viewModel by viewModels<SettingsViewModel>()
    private lateinit var binding: SettingsFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SettingsFragmentBinding.bind(view)

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvCategories.setOnClickListener {
            viewModel.onCategoriesSelected()
        }

        setupEvents()
    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.settingsEvents.collect { event ->
                when(event)
                {
                    SettingsViewModel.SettingsEvent.NavigateToCategoriesFragment -> {
                        navigateToCategoriesFragment()
                    }
                    SettingsViewModel.SettingsEvent.NavigateToNotificationsFragment -> {
                        navigateToNotificationsFragment()
                    }
                    SettingsViewModel.SettingsEvent.ShareTheAppClicked -> {
                        shareTheApp()
                    }
                    SettingsViewModel.SettingsEvent.RateTheAppClicked -> {
                        rateTheApp()
                    }
                    SettingsViewModel.SettingsEvent.NavigateToTermsAndConditionsFragment -> {
                        navigateToTermsAndConditionFragment()
                    }
                    SettingsViewModel.SettingsEvent.NavigateToPrivacyPolicyFragment -> {
                        navigateToPrivacyPolicyFragment()
                    }
                    is SettingsViewModel.SettingsEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }


    /** Navigation methods **/
    private fun navigateToCategoriesFragment() {
        findNavController().navigate(R.id.action_settingsFragment_to_categoriesFragment)
    }
    private fun navigateToNotificationsFragment() {

    }
    private fun shareTheApp() {

    }
    private fun rateTheApp() {

    }
    private fun navigateToTermsAndConditionFragment() {
        findNavController().navigate(R.id.action_settingsFragment_to_termsAndConditionsFragment)
    }
    private fun navigateToPrivacyPolicyFragment() {
        findNavController().navigate(R.id.action_settingsFragment_to_privacyPolicyFragment)
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = SettingsFragmentDirections.actionSettingsFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }
}