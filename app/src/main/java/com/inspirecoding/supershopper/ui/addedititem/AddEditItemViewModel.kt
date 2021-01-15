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
import com.inspirecoding.supershopper.utils.Status
import com.inspirecoding.supershopper.utils.ValidateMethods
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*

class AddEditItemViewModel @ViewModelInject constructor(
    private val shopperRepository: ShopperRepository,
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName
    private val LISTITEM = "listItem"
    private val LOW = "LOW"

    companion object {
        const val CATEGORY = "category"
    }

    val openedShoppingList = state.getLiveData<ShoppingList>(ARG_KEY_OPENEDSHOPPINGLIST)
    val listItem = state.getLiveData<ListItem?>(LISTITEM)

    private val _addEditItemEventChannel = Channel<AddEditItemEvent>()
    val addEditItemEventChannel = _addEditItemEventChannel.receiveAsFlow()

    var item = ""
    var unit = ""
    var qunatity: Float? = null
    var comment = ""

    private val _category = MutableLiveData<Category?>()
    val category: LiveData<Category?> = _category

    fun getCategoryById(id: Int) {
        viewModelScope.launch {
            shopperRepository.getCategoryByIdWithFlow(id).collect {
                _category.postValue(it)
            }
        }
    }

    fun setCategory(category: Category) {
        _category.postValue(category)
    }

    fun areTheFieldsValid(): Boolean {
        var _errorMessage = ""

        /** Name **/
        _errorMessage += if (_errorMessage != "") {
            "\n${ValidateMethods.validateName(item)}"
        } else {
            ValidateMethods.validateName(item)
        }
//        /** Unit **/
//        _errorMessage += if (_errorMessage != "") {
//            "\n${ValidateMethods.validateUnit(item)}"
//        } else {
//            ValidateMethods.validateUnit(item)
//        }
//        /** Quantity **/
//        _errorMessage += if (_errorMessage != "") {
//            "\n${ValidateMethods.validateQuantity(qunatity)}"
//        } else {
//            ValidateMethods.validateQuantity(qunatity)
//        }
//        /** Category **/
//        _errorMessage += if (_errorMessage != "") {
//            "\n${ValidateMethods.validateCategory(_category.value)}"
//        } else {
//            ValidateMethods.validateCategory(_category.value)
//        }

        if (_errorMessage != "") onShowErrorMessage(_errorMessage)

        return _errorMessage.isEmpty()
    }

    fun updateAddShoppingListItem() {
        openedShoppingList.value?.let { shoppingList ->
            if(listItem.value != null) {

                shoppingList.listOfItems.forEach { _listItem ->
                    if(_listItem.id == (listItem.value as ListItem).id) {
                        _listItem.let {
                            it.item = item
                            it.unit = unit
                            it.qunatity = qunatity
                            it.comment = comment
                            it.priority = LOW
                        }

                        category.value?.let { _category ->
                            _listItem.categoryId = _category.id
                        }
                    }
                }

            } else {

                shoppingList.listOfItems.add(
                    createNewShoppingListItem()
                )

            }

            updateShoppingListItems()
        }
    }

    fun deleteShoppingListItem() {
        openedShoppingList.value?.let { shoppingList ->

            listItem.value?.let {
                shoppingList.listOfItems.remove(it)
            }

            updateShoppingListItems()
        }
    }

    private fun updateShoppingListItems() {
        viewModelScope.launch {
            openedShoppingList.value?.let { shoppingList ->
                shoppingListRepository.updateShoppingListItems(
                    shoppingList.shoppingListId, shoppingList.listOfItems
                ).collect { result ->
                    when(result.status)
                    {
                        Status.SUCCESS ->  {
                            onItemSavedSuccessfully()
                        }
                        Status.LOADING -> {

                        }
                        Status.ERROR ->  {
                            result.message?.let {
                                onShowErrorMessage(it)
                            }
                        }
                    }

                }
            }
        }
    }

    private fun createNewShoppingListItem(): ListItem {
        val listItem = ListItem()

        listItem.let { _listItem ->
            _listItem.id = UUID.randomUUID().toString()
            _listItem.item = item
            _listItem.unit = unit
            _listItem.qunatity = qunatity
            _listItem.comment = comment
            _listItem.priority = LOW

            _category.value?.let { category ->
                _listItem.categoryId = category.id
            }
        }

        return listItem
    }






    /** Events **/
    fun onSelectCategory() {
        viewModelScope.launch {
            _addEditItemEventChannel.send(AddEditItemEvent.NavigateToSelectCategoryFragment)
        }
    }
    private fun onItemSavedSuccessfully() {
        viewModelScope.launch {
            _addEditItemEventChannel.send(AddEditItemEvent.NavigateBack)
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _addEditItemEventChannel.send(AddEditItemEvent.ShowErrorMessage(message))
        }
    }






    sealed class AddEditItemEvent {
        object NavigateBack: AddEditItemEvent()
        object NavigateToSelectCategoryFragment: AddEditItemEvent()
        data class ShowErrorMessage(val message: String) : AddEditItemEvent()
    }

}