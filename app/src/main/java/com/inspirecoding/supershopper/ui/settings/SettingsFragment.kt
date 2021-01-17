package com.inspirecoding.supershopper.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.inspirecoding.supershopper.MainActivity
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.SettingsFragmentBinding
import com.inspirecoding.supershopper.ui.register.RegisterFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.settings_fragment) {

    // CONST
    private val TAG = this.javaClass.simpleName

    private val viewModel by viewModels<SettingsViewModel>()
    private lateinit var binding: SettingsFragmentBinding
    private lateinit var manager: ReviewManager
    private var reviewInfo: ReviewInfo? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SettingsFragmentBinding.bind(view)

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }


        setupEvents()
        setupNotificationSettingsObserver()
        setupNightModeSettingsObserver()
        initReviews()

        binding.tvCategories.setOnClickListener {
            viewModel.onCategoriesSelected()
        }

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            viewModel.saveNotificationsSettingsToDataStore(isChecked)
        }

        binding.tvShareTheApp.setOnClickListener {
            viewModel.onShareTheAppSelected()
        }

        binding.tvRateTheApp.setOnClickListener {
            viewModel.onRateTheAppSelected()
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
                    is SettingsViewModel.SettingsEvent.ShareTheAppClicked -> {
                        shareTheApp(event.currentUser)
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


    /** Google Play In-App Review **/
    private fun initReviews() {
        context?.let { _context ->
            manager = ReviewManagerFactory.create(_context)
            manager.requestReviewFlow().addOnCompleteListener { request ->
                if(request.isSuccessful) {
                    reviewInfo = request.result
                } else {
                    request.exception?.message?.let { errorMessage ->
                        Log.e(TAG, errorMessage)
                    }
                }
            }
        }
    }


    /** Navigation methods **/
    private fun navigateToCategoriesFragment() {
        findNavController().navigate(R.id.action_settingsFragment_to_categoriesFragment)
    }
    private fun shareTheApp(currentUser: User) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_the_app))
        intent.putExtra(Intent.EXTRA_TEXT, "${getString(R.string.user_has_shared_with_you_the_supershopper_app_you_can_download_it_via_the_link, currentUser.name)}\nhttps://play.google.com/store/apps/details?id=com.inspirecoding.supershopper")
        startActivity(Intent.createChooser(intent, getString(R.string.share_the_app_via)))
    }

    private fun rateTheApp() {
        reviewInfo?.let { _reviewInfo ->
            if(reviewInfo != null) {
                manager.launchReviewFlow(requireActivity(), _reviewInfo).addOnFailureListener { exception ->
                    exception?.message?.let { errorMessage ->
                        Log.e(TAG, errorMessage)
                    }
                }
            }
        }
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