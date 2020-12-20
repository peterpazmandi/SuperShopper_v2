package com.inspirecoding.supershopper.ui.openedshoppinglist.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.inspirecoding.supershopper.MainActivity

class OpenedShoppingListAdapter (activity: MainActivity, private var itemCount: Int): FragmentStateAdapter(activity) {

    private var fragmentList: MutableList<Fragment> = ArrayList()
    fun addFragment(fragment: Fragment) = fragmentList.add(fragment)

    override fun getItemCount() = itemCount

    override fun createFragment(position: Int): Fragment {
        return when (position)
        {
            0 -> fragmentList[0]
            1 -> fragmentList[1]
            else -> fragmentList[0]
        }
    }

}