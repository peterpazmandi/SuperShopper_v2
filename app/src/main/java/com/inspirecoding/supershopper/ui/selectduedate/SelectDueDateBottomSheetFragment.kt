package com.inspirecoding.supershopper.ui.selectduedate

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.SelectDueDateBottomSheetFragmentBinding
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel.Companion.ARG_KEY_DUEDATE
import com.inspirecoding.supershopper.ui.selectcategory.SelectCategoryBottomSheetFragmentDirections
import com.inspirecoding.supershopper.utils.convertSelectedDateToLong
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.select_due_date_bottom_sheet_fragment.view.*
import kotlinx.coroutines.flow.collect
import java.util.*

@AndroidEntryPoint
class SelectDueDateBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: SelectDueDateBottomSheetViewModel by viewModels()
    private lateinit var binding: SelectDueDateBottomSheetFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SelectDueDateBottomSheetFragmentBinding.inflate(inflater)

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnOk.setOnClickListener {
            viewModel.dateLong = binding.datePicker.convertSelectedDateToLong()

            viewModel.onNavigateBackWithResult()
        }

        return binding.root
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            isFitToContents = false
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        setupDueDateObserver()
        setupEvents()
    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.selectDueDateEvent.collect { event ->
                when(event)
                {
                    is SelectDueDateBottomSheetViewModel.SelectDueDateEvent.NavigateBackWithResult -> {
                        navigateBackWithResult(event.dueDate)
                    }
                    is SelectDueDateBottomSheetViewModel.SelectDueDateEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)}
                }
            }
        }
    }

    private fun setupDueDateObserver() {
        viewModel.dueDate.observe(viewLifecycleOwner, {
            viewModel.dateLong = it
            setInitialDate()
        })
    }

    private fun setInitialDate() {
        val calender = Calendar.getInstance()
        calender.time = Date(viewModel.dateLong)
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val dayOfMonth = calender.get(Calendar.DAY_OF_MONTH)
        binding.datePicker.updateDate(year, month, dayOfMonth)
    }












    /** Navigation methods **/
    private fun navigateBackWithResult(dueDate: Long) {
        setFragmentResult(
            ARG_KEY_DUEDATE,
            bundleOf(ARG_KEY_DUEDATE to dueDate)
        )
        findNavController().popBackStack()
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = SelectCategoryBottomSheetFragmentDirections.actionSelectCategoryBottomSheetFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }

}