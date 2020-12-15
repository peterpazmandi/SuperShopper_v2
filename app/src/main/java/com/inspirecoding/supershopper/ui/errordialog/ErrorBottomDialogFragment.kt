package com.inspirecoding.supershopper.ui.errordialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.ErrorBottomDialogFragmentBinding

class ErrorBottomDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding : ErrorBottomDialogFragmentBinding
    private val safeArgs : ErrorBottomDialogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ErrorBottomDialogFragmentBinding.inflate(
            layoutInflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val errorMessage = safeArgs.errorMessage
        binding.tvErrorMessage.text = errorMessage
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

}