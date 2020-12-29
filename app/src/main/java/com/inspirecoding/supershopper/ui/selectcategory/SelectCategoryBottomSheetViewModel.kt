package com.inspirecoding.supershopper.ui.selectcategory

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.repository.local.ShopperRepository
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SelectCategoryBottomSheetViewModel @ViewModelInject constructor(
    private val shopperRepository: ShopperRepository,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName


    private val _list = mutableListOf<Category>()
    private val _listOfCategories = MutableLiveData<MutableList<Category>>()
    val listOfCategories: LiveData<MutableList<Category>> = _listOfCategories

    private val _selectCategoryEvent = Channel<SelectCategoryEvent>()
    val selectCategoryEvent = _selectCategoryEvent.receiveAsFlow()

    fun getListOfCategories() {
        viewModelScope.launch {
            shopperRepository.getCategories().collect { list ->
                _list.clear()
                _list.addAll(list)
                _listOfCategories.postValue(_list)
            }
        }
    }





    /** Events **/
    fun onNavigateBackWithResult(category: Category) {
        viewModelScope.launch {
            _selectCategoryEvent.send(SelectCategoryEvent.NavigateBackWithResult(category))
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _selectCategoryEvent.send(SelectCategoryEvent.ShowErrorMessage(message))
        }
    }






    sealed class SelectCategoryEvent {
        data class NavigateBackWithResult(val category: Category) : SelectCategoryEvent()
        data class ShowErrorMessage(val message: String) : SelectCategoryEvent()
    }
}