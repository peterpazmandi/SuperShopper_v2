package com.inspirecoding.supershopper.ui.categories

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.repository.local.ShopperRepository
import com.inspirecoding.supershopper.ui.categories.listitems.CategoryItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CategoriesViewModel @ViewModelInject constructor(
    private val shopperRepository: ShopperRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    private val _list = mutableListOf<Category>()
    private val _listOfCategories = MutableLiveData<MutableList<Category>>()
    val listOfCategories: LiveData<MutableList<Category>> = _listOfCategories

    fun getListOfCategories() {
        viewModelScope.launch {
            shopperRepository.getCategories().collect { list ->
                _list.clear()
                _list.addAll(list)
                _listOfCategories.postValue(_list)
            }
        }
    }

    fun onRemoveItem(category: Category) {

        viewModelScope.launch {
            shopperRepository.deleteCategory(category)

            getListOfCategories()

            for (i in 0 until _list.size-1) {
                _list[i].position = i
                shopperRepository.updateCategory(_list[i])
            }
        }

    }

    fun onMoveItemUp(category: Category) {

        val currentPos = _list.indexOfFirst {
            it.id == category.id
        }

        val currentItem = _list[currentPos]
        currentItem.position = currentItem.position - 1

        val prevItem = _list[currentPos - 1]
        prevItem.position = prevItem.position + 1

        viewModelScope.launch {
            shopperRepository.updateCategory(currentItem)
            shopperRepository.updateCategory(prevItem)
        }
    }

    fun onMoveItemDown(category: Category) {

        val currentPos = _list.indexOfFirst {
            it.id == category.id
        }

        val currentItem = _list[currentPos]
        currentItem.position = currentItem.position + 1

        val followerItem = _list[currentPos + 1]
        followerItem.position = followerItem.position - 1

        viewModelScope.launch {
            shopperRepository.updateCategory(currentItem)
            shopperRepository.updateCategory(followerItem)
        }
    }

}