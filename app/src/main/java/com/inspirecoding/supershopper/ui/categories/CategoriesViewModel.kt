package com.inspirecoding.supershopper.ui.categories

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.repository.local.ShopperRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CategoriesViewModel @ViewModelInject constructor(
    private val shopperRepository: ShopperRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    private val _settingsEvents = Channel<CategoryEvent>()
    val settingsEvents = _settingsEvents.receiveAsFlow()

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

    fun onRemoveItem(from: Int) {

        viewModelScope.launch {
            val fromCat = _list[from]
            _list.remove(fromCat)

            for (i in 0 until _list.size) {
                _list[i].position = i
            }
            shopperRepository.updateCategories(_list)
            shopperRepository.deleteCategory(fromCat)

            getListOfCategories()
        }

    }
    fun onRemoveItem(category: Category) {

        viewModelScope.launch {
            _list.remove(category)

            for (i in 0 until _list.size) {
                _list[i].position = i
            }
            shopperRepository.updateCategories(_list)
            shopperRepository.deleteCategory(category)

            getListOfCategories()
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


    fun moveItem(from: Int, to: Int) {

        val fromCat = _list[from]
        _list.removeAt(from)

        _list.add(to, fromCat)

    }

    fun updateItems() {

        viewModelScope.launch {

            for (i in 0 until _list.size) {
                _list[i].position = i
            }

            shopperRepository.updateCategories(_list)

        }

    }

    fun printLog() {
        println("position - ${_list.map { it.position }}")
        println("id - ${_list.map { it.id }}")
    }

    /** Events **/
    fun onAddCategorySelected() {
        viewModelScope.launch {
            _settingsEvents.send(CategoryEvent.NavigateToAddCategoryFragment)
        }
    }
    fun onEditCategorySelected(category: Category) {
        viewModelScope.launch {
            _settingsEvents.send(CategoryEvent.NavigateToAddEditCategoryFragment(category))
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _settingsEvents.send(CategoryEvent.ShowErrorMessage(message))
        }
    }


    sealed class CategoryEvent {
        object NavigateToAddCategoryFragment: CategoryEvent()
        data class NavigateToAddEditCategoryFragment(val category: Category): CategoryEvent()
        data class ShowErrorMessage(val message: String) : CategoryEvent()
    }


}