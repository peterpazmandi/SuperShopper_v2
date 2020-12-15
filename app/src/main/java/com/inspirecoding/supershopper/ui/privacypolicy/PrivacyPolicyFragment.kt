package com.inspirecoding.supershopper.ui.privacypolicy

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.PrivacyPolicyFragmentBinding
import com.inspirecoding.supershopper.utils.fromHtmlWithParams

class PrivacyPolicyFragment : Fragment(R.layout.privacy_policy_fragment) {

    private lateinit var binding : PrivacyPolicyFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = PrivacyPolicyFragmentBinding.bind(view)

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvPrivacyPolicy1.text = context?.fromHtmlWithParams(R.string.privacy_policy_text_1)
        binding.tvPrivacyPolicy2.text = context?.fromHtmlWithParams(R.string.privacy_policy_text_2)
    }

}