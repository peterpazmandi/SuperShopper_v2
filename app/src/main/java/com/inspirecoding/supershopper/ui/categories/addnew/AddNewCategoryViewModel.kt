package com.inspirecoding.supershopper.ui.categories.addnew

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.local.ShopperRepository
import com.inspirecoding.supershopper.ui.register.RegisterViewModel
import com.inspirecoding.supershopper.utils.ValidateMethods
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddNewCategoryViewModel@ViewModelInject constructor(
    private val shopperRepository: ShopperRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    private val _addNewCategoryEventChannel = Channel<AddNewCategoryEvent>()
    val addNewCategoryEventChannel = _addNewCategoryEventChannel.receiveAsFlow()

    var name: String? = null
    var icon: Int? = null


    fun areTheFieldsValid(): Boolean {
        var _errorMessage = ""

        _errorMessage += if (_errorMessage != "") {
            "\n${ValidateMethods.validateCategoryName(name)}"
        } else {
            ValidateMethods.validateCategoryName(name)
        }

        if (icon == null) {
            _errorMessage += if (_errorMessage != "") {
                "\nSelect an icon!"
            } else {
                "Select an icon!"
            }
        }

        if (_errorMessage != "") onShowErrorMessage(_errorMessage)

        return _errorMessage.isEmpty()
    }

    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _addNewCategoryEventChannel.send(AddNewCategoryEvent.ShowErrorMessage(message))
        }
    }

    fun insertCategory() {
        viewModelScope.launch {
            val categoriesCount = shopperRepository.getCategoriesSuspend().size

            val newCategory = Category(
                customName = name,
                iconDrawableResId = icon!!,
                position = categoriesCount + 1
            )

            shopperRepository.insertCategory(newCategory)
        }
    }

    sealed class AddNewCategoryEvent {
        data class ShowErrorMessage(val message: String) : AddNewCategoryEvent()
    }

}