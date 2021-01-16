package com.inspirecoding.supershopper.ui.settings

import android.content.Intent
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


        setupEvents()
        setupNotificationSettingsObserver()
        setupNightModeSettingsObserver()

        binding.tvCategories.setOnClickListener {
            viewModel.onCategoriesSelected()
        }

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            viewModel.saveNotificationsSettingsToDataStore(isChecked)
        }

        binding.tvShareTheApp.setOnClickListener {
            viewModel.onShareTheAppSelected()
        }

        binding.tvTermsAndConditions.setOnClickListener {
            viewModel.onTermsAndConditionSelected()
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            viewModel.onPrivacyPolicySelected()
        }

        binding.switchNightMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.saveNightModeSettingsToDataStore(isChecked)
        }
    }

    private fun setupNotificationSettingsObserver() {
        viewModel.notificationsSettingsFromDataStore.observe(viewLifecycleOwner, { areTurnedOn ->
            areTurnedOn?.let { _areTurnedOn ->
                binding.switchNotification.isChecked = _areTurnedOn
            }
        })
    }

    private fun setupNightModeSettingsObserver() {
        viewModel.nightModeSettingsFromDataStore.observe(viewLifecycleOwner, { isTurnedOn ->
            isTurnedOn?.let { _isTurnedOn ->
                binding.switchNightMode.isChecked = _isTurnedOn
            }
        })
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
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_the_app))
        intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.inspirecoding.supershopper")
        startActivity(Intent.createChooser(intent, getString(R.string.share_the_app_via)))
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