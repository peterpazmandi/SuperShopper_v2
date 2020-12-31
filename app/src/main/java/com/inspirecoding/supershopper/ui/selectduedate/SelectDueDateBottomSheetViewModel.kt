package com.inspirecoding.supershopper.ui.selectduedate

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel.Companion.ARG_KEY_DUEDATE
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SelectDueDateBottomSheetViewModel@ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    val dueDate = state.getLiveData<Long>(ARG_KEY_DUEDATE)

    private val _selectDueDateEvent = Channel<SelectDueDateEvent>()
    val selectDueDateEvent = _selectDueDateEvent.receiveAsFlow()

    var dateLong: Long = 0L



    /** Events **/
    fun onNavigateBackWithResult() {
        viewModelScope.launch {
            _selectDueDateEvent.send(SelectDueDateEvent.NavigateBackWithResult(dateLong))
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _selectDueDateEvent.send(SelectDueDateEvent.ShowErrorMessage(message))
        }
    }






    sealed class SelectDueDateEvent {
        data class NavigateBackWithResult(val dueDate: Long) : SelectDueDateEvent()
        data class ShowErrorMessage(val message: String) : SelectDueDateEvent()
    }
}