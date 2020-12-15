package com.inspirecoding.supershopper.ui.forgotpassword

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.databinding.ForgotPasswordDialogBinding
import com.inspirecoding.supershopper.utils.dismissKeyboard
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import com.inspirecoding.supershopper.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ForgotPasswordDialog : DialogFragment() {

    private val viewModel by viewModels<ForgotPasswordDialogViewModel>()
    private lateinit var binding: ForgotPasswordDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ForgotPasswordDialogBinding.inflate(
            inflater, container, false
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTextChangeListeners()
        setupFragmentEvents()

        binding.btnCancel.setOnClickListener {
            viewModel.onDismissDialog()
        }
        binding.btnReset.setOnClickListener {
            context?.dismissKeyboard(it)
            if (viewModel.areTheFieldsValid()) {
                viewModel.resetEmail()
            }
        }
    }

    private fun setupFragmentEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {

            viewModel.fragmentEvent.collect { event ->
                when(event)
                {
                    ForgotPasswordDialogViewModel.FragmentEvent.DismissDialog -> {
                        binding.dialogProgressBar.makeItInVisible()
                        dismiss()
                    }
                    is ForgotPasswordDialogViewModel.FragmentEvent.ShowErrorMessage -> {
                        binding.dialogProgressBar.makeItInVisible()
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                    ForgotPasswordDialogViewModel.FragmentEvent.RequestPending -> {
                        binding.dialogProgressBar.makeItVisible()
                    }
                    ForgotPasswordDialogViewModel.FragmentEvent.SuccessfulReset -> {
                        binding.dialogProgressBar.makeItInVisible()
                        context?.apply {
                            showToast(
                                this.getString(R.string.we_sent_you_an_email_with_the_reset_link)
                            )
                        }
                        viewModel.onDismissDialog()
                    }
                }
            }
        }
    }

    private fun setupTextChangeListeners() {
        binding.tietEmailAddress.addTextChangedListener { _emailAddress ->
            viewModel.email = _emailAddress.toString().trim()
        }
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = ForgotPasswordDialogDirections.actionForgotPasswordDialogToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }

}