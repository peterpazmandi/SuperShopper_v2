package com.inspirecoding.supershopper.ui.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.CategoriesFragmentBinding

class CategoriesFragment : Fragment(R.layout.categories_fragment) {

    private val viewModel by viewModels<CategoriesViewModel>()
    private lateinit var binding: CategoriesFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CategoriesFragmentBinding.bind(view)

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}