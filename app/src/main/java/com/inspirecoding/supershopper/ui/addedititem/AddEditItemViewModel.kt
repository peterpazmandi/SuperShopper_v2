package com.inspirecoding.supershopper.ui.addedititem

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.data.ListItem
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.repository.local.ShopperRepository
import com.inspirecoding.supershopper.repository.shoppinglist.ShoppingListRepository
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel.Companion.ARG_KEY_OPENEDSHOPPINGLIST
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditItemViewModel @ViewModelInject constructor(
    private val shopperRepository: ShopperRepository,
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName
    private val LISTITEM = "listItem"

    companion object {
        const val CATEGORY = "category"
    }

    val openedShoppingList = state.getLiveData<ShoppingList>(ARG_KEY_OPENEDSHOPPINGLIST)
    val listItem = state.getLiveData<ListItem?>(LISTITEM)

    private val _addEditItemEventChannel = Channel<AddEditItemEvent>()
    val addEditItemEventChannel = _addEditItemEventChannel.receiveAsFlow()

    var item = ""
    var unit = ""
    var qunatity = 0f

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> = _category

    fun getCategoryById(id: Int) {
        viewModelScope.launch {
            shopperRepository.getCategoryById(id).collect {
                _category.postValue(it)
            }
        }
    }

    fun setCategory(category: Category) {
        _category.postValue(category)
    }





    /** Events **/
    fun onSelectCategory() {
        viewModelScope.launch {
            _addEditItemEventChannel.send(AddEditItemEvent.NavigateToSelectCategoryFragment)
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _addEditItemEventChannel.send(AddEditItemEvent.ShowErrorMessage(message))
        }
    }






    sealed class AddEditItemEvent {
        object NavigateToSelectCategoryFragment: AddEditItemEvent()
        data class ShowErrorMessage(val message: String) : AddEditItemEvent()
    }
}