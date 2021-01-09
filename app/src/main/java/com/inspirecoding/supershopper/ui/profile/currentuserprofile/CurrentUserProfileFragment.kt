package com.inspirecoding.supershopper.ui.profile.currentuserprofile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.CurrentUserProfileFragmentBinding
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class CurrentUserProfileFragment : Fragment(R.layout.current_user_profile_fragment) {

    private val PROFILE_IMAGE_REQUEST_CODE = 0
    private val PASSWORD = "password"
    private val EMAIL = "email"

    private val viewModel: CurrentUserProfileViewModel by viewModels()
    private lateinit var binding: CurrentUserProfileFragmentBinding

    private val openImageCropperActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val cropResult: CropImage.ActivityResult = CropImage.getActivityResult(result.data)
                cropResult.uri.path?.let { _path ->
                    viewModel.onChangeProfileImage(_path)
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CurrentUserProfileFragmentBinding.bind(view)

        setupCurrentUserObserver()
        setupEvents()

        binding.ivBackButton.setOnClickListener {
            viewModel.onNavigateBackWithCurrentUser()
        }

        binding.ivUpdatePhoto.setOnClickListener {
            startImageCropper()
        }

        binding.ivUpdateUserName.setOnClickListener {
            viewModel.onNavigateToUpdateUserName()
        }
        binding.ivEditEmailAddress.setOnClickListener {
            viewModel.onNavigateToUpdateEmailAddress()
        }
        binding.tvChangePassword.setOnClickListener {
            viewModel.onNavigateToUpdatePassword()
        }

        setFragmentResultListener(CurrentUserProfileViewModel.USERNAME) { _, bundle ->
            bundle.getString(CurrentUserProfileViewModel.USERNAME).let { newUserName ->
                newUserName?.let {
                    viewModel.onChangeUserName(it)
                }
            }
        }
        setFragmentResultListener(CurrentUserProfileViewModel.EMAIL) { _, bundle ->
            bundle.getString(CurrentUserProfileViewModel.EMAIL).let { newEmail ->
                newEmail?.let {
                    viewModel.onChangeEmailAddress(it)
                }
            }
        }
        setFragmentResultListener(CurrentUserProfileViewModel.PASSWORD) { _, bundle ->
            bundle.getString(CurrentUserProfileViewModel.PASSWORD).let { newPassword ->
                newPassword?.let {
                    viewModel.onChangePassword(it)
                }
            }
        }

        binding.tvLogOut.setOnClickListener {
            viewModel.onLogOut()
        }
    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fragmentEvent.collect { event ->
                when(event)
                {
                    CurrentUserProfileViewModel.FragmentEvent.ShowLoading -> {
                        binding.progressBar.makeItVisible()
                    }
                    is CurrentUserProfileViewModel.FragmentEvent.ShowResult -> {
                        binding.progressBar.makeItInVisible()
                    }
                    is CurrentUserProfileViewModel.FragmentEvent.NavigateToUpdateUserName -> {
                        navigateToUpdateUserName(event.fieldToChange)
                    }
                    is CurrentUserProfileViewModel.FragmentEvent.NavigateToUpdateEmailAddress -> {
                        navigateToUpdateEmailAddress(event.fieldToChange)
                    }
                    is CurrentUserProfileViewModel.FragmentEvent.NavigateToUpdatePassword -> {
                        navigateToUpdatePassword(event.fieldToChange)
                    }
                    CurrentUserProfileViewModel.FragmentEvent.LogOut -> {
                        navigateToSplashFragment()
                    }
                    is CurrentUserProfileViewModel.FragmentEvent.NavigateBackWithCurrentUser -> {
                        navigateBackWithCurrentUser(event.currentUser)
                    }
                    is CurrentUserProfileViewModel.FragmentEvent.ShowErrorMessage -> {
                        binding.progressBar.makeItInVisible()
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }

    private fun setupCurrentUserObserver() {
        viewModel.currentUser.observe(viewLifecycleOwner, { currentUser ->
            setupUi(currentUser)
        })
    }

    private fun setupUi(currentUser: User) {

        if(currentUser.profilePicture.isNotEmpty()) {
            Picasso
                .get()
                .load(currentUser.profilePicture)
                .placeholder(R.drawable.ic_default_profile_picture)
                .centerCrop()
                .fit()
                .into(binding.ivProfilePhoto)
        }

        binding.tvUserName.text = currentUser.name

        binding.tvEmailAddress.text = currentUser.emailAddress
    }










    /** Navigation methods **/
    private fun startImageCropper() {
        context?.let { _context ->
            openImageCropperActivity.launch(
                CropImage.activity()
                    .setActivityTitle(getString(R.string.profile_photo))
                    .setAspectRatio(1, 1)
                    .getIntent(_context)
            )
        }
    }

    private fun navigateToUpdateUserName(fieldToChange: String) {
        val action = CurrentUserProfileFragmentDirections.actionCurrentUserProfileFragmentToUpdateUserProfileBottomSheetFragment(fieldToChange)
        findNavController().navigate(action)
    }

    private fun navigateToUpdateEmailAddress(fieldToChange: String) {
        val action = CurrentUserProfileFragmentDirections.actionCurrentUserProfileFragmentToUpdateUserProfileBottomSheetFragment(fieldToChange)
        findNavController().navigate(action)
    }

    private fun navigateToUpdatePassword(fieldToChange: String) {
        val action = CurrentUserProfileFragmentDirections.actionCurrentUserProfileFragmentToUpdateUserProfileBottomSheetFragment(fieldToChange)
        findNavController().navigate(action)
    }

    private fun navigateToSplashFragment() {
        findNavController().popBackStack(R.id.shoppingListsFragment, true)
        findNavController().navigate(R.id.splashFragment)
    }

    private fun navigateBackWithCurrentUser(currentUser: User) {
        setFragmentResult(
            ShoppingListsViewModel.ARG_KEY_USER,
            bundleOf(ShoppingListsViewModel.ARG_KEY_USER to currentUser)
        )
        findNavController().popBackStack()
    }

    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = CurrentUserProfileFragmentDirections.actionCurrentUserProfileFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }

}