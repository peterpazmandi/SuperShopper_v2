package com.inspirecoding.supershopper.ui.profile.userprofile.updateuserprofile

import android.app.Dialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.databinding.UpdateUserProfileBottomSheetFragmentBinding
import com.inspirecoding.supershopper.ui.addedititem.AddEditItemViewModel
import com.inspirecoding.supershopper.ui.profile.currentuserprofile.CurrentUserProfileViewModel
import com.inspirecoding.supershopper.ui.selectcategory.SelectCategoryBottomSheetFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class UpdateUserProfileBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: UpdateUserProfileBottomSheetViewModel by viewModels()
    private lateinit var binding: UpdateUserProfileBottomSheetFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UpdateUserProfileBottomSheetFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFieldToChangeObserver()
        setupEvents()

        binding.tietNewValue.doAfterTextChanged {
            viewModel.newValue = it.toString()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnChange.setOnClickListener {
            viewModel.onChangeSelected()
        }
    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fragmentEvent.collect { event ->
                when(event)
                {
                    is UpdateUserProfileBottomSheetViewModel.FragmentEvent.UpdateUserName -> {
                        navigateBackWithUserName(event.fieldToChange)
                    }
                    is UpdateUserProfileBottomSheetViewModel.FragmentEvent.UpdateEmailAddress -> {
                        navigateBackWithEmail(event.fieldToChange)
                    }
                    is UpdateUserProfileBottomSheetViewModel.FragmentEvent.UpdatePassword -> {
                        navigateBackWithUserPassword(event.fieldToChange)
                    }
                    is UpdateUserProfileBottomSheetViewModel.FragmentEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }

    private fun setupFieldToChangeObserver() {
        viewModel.fieldToChange.observe(viewLifecycleOwner, { fieldToChange ->
            when(fieldToChange)
            {
                CurrentUserProfileViewModel.USERNAME -> {
                    setUiToChangeUserName()
                }
                CurrentUserProfileViewModel.EMAIL -> {
                    setUiToChangeEmail()
                }
                CurrentUserProfileViewModel.PASSWORD -> {
                    setUiToChangePassword()
                }
            }
        })
    }

    private fun setUiToChangeUserName() {
        binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_blue, 0, 0, 0)
        binding.tvTitle.text = getString(R.string.change_name)

        binding.tietNewValue.inputType = InputType.TYPE_CLASS_TEXT
    }

    private fun setUiToChangeEmail() {
        binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email_blue, 0, 0, 0)
        binding.tvTitle.text = getString(R.string.change_email)

        binding.tietNewValue.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    }

    private fun setUiToChangePassword() {
        binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_blue, 0, 0, 0)
        binding.tvTitle.text = getString(R.string.change_password)

        binding.tietNewValue.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.tilNewValue.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
    }





    private fun navigateBackWithUserName(username: String) {
        setFragmentResult(
            CurrentUserProfileViewModel.USERNAME,
            bundleOf(CurrentUserProfileViewModel.USERNAME to username)
        )
        findNavController().popBackStack()
    }
    private fun navigateBackWithEmail(email: String) {
        setFragmentResult(
            CurrentUserProfileViewModel.EMAIL,
            bundleOf(CurrentUserProfileViewModel.EMAIL to email)
        )
        findNavController().popBackStack()
    }
    private fun navigateBackWithUserPassword(password: String) {
        setFragmentResult(
            CurrentUserProfileViewModel.PASSWORD,
            bundleOf(CurrentUserProfileViewModel.PASSWORD to password)
        )
        findNavController().popBackStack()
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = UpdateUserProfileBottomSheetFragmentDirections.actionCurrentUserProfileFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }
}