package com.mrprogrammer.attendance.Respositoy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object CommonRepo {
    private val totalLeave: MutableLiveData<HashMap<String,String>> = MutableLiveData()

    fun updateLeave(newValue: HashMap<String,String>) {
        totalLeave.postValue(newValue)
    }

    fun getLeave():LiveData<HashMap<String,String>> {
        return totalLeave
    }


}