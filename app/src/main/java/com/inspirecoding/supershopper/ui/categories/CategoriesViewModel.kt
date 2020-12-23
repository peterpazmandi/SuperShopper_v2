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
import java.util.*

class CategoriesViewModel @ViewModelInject constructor(
    private val shopperRepository: ShopperRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    private val _list = mutableListOf<Category>()
    private val _listOfCategories = MutableLiveData<MutableList<Category>>()
    val listOfCategories: LiveData<MutableList<Category>> = _listOfCategories

    private val removedItems = mutableListOf<Category>()

    private val _changesMade = MutableLiveData<Boolean>()
    val changesMade: LiveData<Boolean> = _changesMade

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

        _list.remove(category)

        _list.forEachIndexed { index, category ->
            category.position = index
        }

        viewModelScope.launch {
            shopperRepository.deleteCategory(category)
            delay(200)
            shopperRepository.updateCategories(_list)
        }
    }

    fun onMoveItemUp(category: Category) {

        val currentPos = _list.indexOf(category)

        _list.remove(category)
        _list.add(currentPos - 1, category)

        _list.forEachIndexed { index, category ->
            category.position = index
        }

        viewModelScope.launch {
            shopperRepository.updateCategories(_list)
        }

        categoryListHasChanged()
    }

    fun onMoveItemDown(category: Category) {

        val currentPos = _list.indexOf(category)

        _list.remove(category)
        _list.add(currentPos + 1, category)

        _list.forEachIndexed { index, category ->
            category.position = index
        }

        viewModelScope.launch {
            shopperRepository.updateCategories(_list)
        }

        categoryListHasChanged()
    }



    private fun categoryListHasChanged() {
        _changesMade.postValue(true)
    }
    fun saveCategoryChanges() {
        viewModelScope.launch {
            shopperRepository.updateCategories(_list)
        }
    }

}