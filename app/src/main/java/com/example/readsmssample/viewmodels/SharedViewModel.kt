package com.example.readsmssample.viewmodels

import androidx.lifecycle.ViewModel
import com.example.readsmssample.viewmodels.smslisting.Sms
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {
    private var smsList : Map<String, List<Sms>> = hashMapOf()

    fun setSmsList(smsList : Map<String, List<Sms>>){
        this.smsList = smsList
    }

    fun getSmsList(sender : String) = smsList[sender]
}