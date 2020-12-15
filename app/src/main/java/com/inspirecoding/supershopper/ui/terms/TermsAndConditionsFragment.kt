package com.inspirecoding.supershopper.ui.terms

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.TermsAndConditionsFragmentBinding
import com.inspirecoding.supershopper.utils.fromHtmlWithParams

class TermsAndConditionsFragment : Fragment(R.layout.terms_and_conditions_fragment) {

    private lateinit var binding : TermsAndConditionsFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = TermsAndConditionsFragmentBinding.bind(view)

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvTermsAndConditions1.text = context?.fromHtmlWithParams(R.string.terms_and_conditions_text_1)
        binding.tvTermsAndConditions2.text = context?.fromHtmlWithParams(R.string.terms_and_conditions_text_2)
    }

}