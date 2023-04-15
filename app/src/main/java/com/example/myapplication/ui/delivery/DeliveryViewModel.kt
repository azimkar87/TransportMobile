package com.example.myapplication.ui.delivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class DeliveryViewModel : ViewModel() {

    val itemIndex=MutableLiveData<Int>()

    fun setData(newData:Int)
    {
        itemIndex.value=newData
    }
}