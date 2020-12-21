package com.inspirecoding.supershopper.ui.categories

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.repository.local.ShopperRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CategoriesViewModel @ViewModelInject constructor(
    private val shopperRepository: ShopperRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    private val categories = mutableListOf<Category>()
    private val _listOfCategories = MutableLiveData<MutableList<Category>>()
    val listOfCategories: LiveData<MutableList<Category>> = _listOfCategories

    private val removedItems = mutableListOf<Pair<Int, Category>>()
    private val _removedCategories = MutableLiveData<MutableList<Pair<Int, Category>>>()
    val removedCategories: LiveData<MutableList<Pair<Int, Category>>> = _removedCategories

    fun getListOfCategories() {
        viewModelScope.launch {
            shopperRepository.getCategories().collect { list ->
                categories.addAll(list)
                _listOfCategories.postValue(categories)
            }
        }
    }

}