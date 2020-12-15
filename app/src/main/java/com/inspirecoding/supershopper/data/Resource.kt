package com.inspirecoding.supershopper.data

import com.inspirecoding.supershopper.utils.Status

sealed class Resource <out T> (val status : Status, val _data : T?, val message: String?) {

    data class Success<out R>(val data : R?) : Resource<R>(
        status = Status.SUCCESS,
        _data = data,
        message = null
    )
    data class Loading(val isLoading : Boolean) : Resource<Nothing>(
        status = Status.LOADING,
        _data = null,
        message = null
    )
    data class Error(val exception: String) : Resource<Nothing>(
        status = Status.ERROR,
        _data = null,
        message = exception
    )

}

