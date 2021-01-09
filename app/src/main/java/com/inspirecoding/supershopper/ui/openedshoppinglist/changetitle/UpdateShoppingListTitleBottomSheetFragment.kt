package com.inspirecoding.supershopper.ui.openedshoppinglist.changetitle

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
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
import com.inspirecoding.supershopper.databinding.UpdateShoppingListTitleBottomSheetFragmentBinding
import com.inspirecoding.supershopper.databinding.UpdateUserProfileBottomSheetFragmentBinding
import com.inspirecoding.supershopper.ui.profile.currentuserprofile.CurrentUserProfileViewModel
import com.inspirecoding.supershopper.ui.profile.userprofile.updateuserprofile.UpdateUserProfileBottomSheetFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class UpdateShoppingListTitleBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: UpdateShoppingListTitleBottomSheetViewModel by viewModels()
    private lateinit var binding: UpdateShoppingListTitleBottomSheetFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UpdateShoppingListTitleBottomSheetFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTitleObserver()
        setupEvents()

        binding.tietNewValue.doAfterTextChanged {
            viewModel.newTitle = it.toString()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnChange.setOnClickListener {
            viewModel.onChangeSelected()
        }
    }

    private fun setupTitleObserver() {
        viewModel.previousTitle.observe(viewLifecycleOwner, { title ->
            binding.tietNewValue.setText(title)
        })
    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fragmentEvent.collect { event ->
                when(event)
                {
                    is UpdateShoppingListTitleBottomSheetViewModel.FragmentEvent.UpdateTitle -> {
                        navigateBackWithTitle(event.title)
                    }
                    is UpdateShoppingListTitleBottomSheetViewModel.FragmentEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }






    private fun navigateBackWithTitle(title: String) {
        setFragmentResult(
            UpdateShoppingListTitleBottomSheetViewModel.TITLE,
            bundleOf(UpdateShoppingListTitleBottomSheetViewModel.TITLE to title)
        )
        findNavController().popBackStack()
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = UpdateUserProfileBottomSheetFragmentDirections.actionCurrentUserProfileFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }

}