package com.inspirecoding.supershopper.ui.friends

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FriendsFragmentBinding
import com.inspirecoding.supershopper.utils.Status
import com.inspirecoding.supershopper.utils.Status.*
import com.inspirecoding.supershopper.utils.baseclasses.BaseListAdapter
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsFragment : Fragment(R.layout.friends_fragment) {

    private val viewModel: FriendsViewModel by viewModels()
    private lateinit var binding: FriendsFragmentBinding
    private lateinit var adapter: BaseListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FriendsFragmentBinding.bind(view)

        initRecyclerView()
        setupFriendsListObserver()
        viewModel.getFriendsAlphabeticalList()

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initRecyclerView() {
        adapter = BaseListAdapter { view, selectedItem ->

        }
        binding.rvFriends.setHasFixedSize(true)
        binding.rvFriends.setItemViewCacheSize(50)

        binding.rvFriends.adapter = adapter
    }

    private fun setupFriendsListObserver() {
        viewModel.listOfFriends.observe(viewLifecycleOwner, { result ->
            when (result.status) {
                LOADING -> {
                    binding.progressBar.makeItVisible()
                }
                SUCCESS -> {
                    binding.progressBar.makeItInVisible()
                    result.data?.let { listOfFriends ->
                        val listOfUserObjects = viewModel.createListOfFriendsItem(listOfFriends)
                        adapter.submitList(listOfUserObjects)
                    }
                }
                ERROR -> {
                    binding.progressBar.makeItInVisible()
                }
            }
        })
    }


}