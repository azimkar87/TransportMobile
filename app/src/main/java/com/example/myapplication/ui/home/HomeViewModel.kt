package com.example.myapplication.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Date

class HomeViewModel : ViewModel() {


val itemIndex=MutableLiveData<Int>()
val selectedDate=MutableLiveData<Date>()

fun setData(newData:Int)
{
       itemIndex.value=newData
}

fun setSelectedDate(_selectedDate:Date)
{
       selectedDate.value=_selectedDate
}
fun   getSelectedDate():LiveData<Date>
{
       return selectedDate
}

}