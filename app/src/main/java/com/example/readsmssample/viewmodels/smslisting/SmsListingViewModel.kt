package com.example.readsmssample.viewmodels.smslisting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SmsListUiState(
    val sender : String? = null,
    val loadingStatus: SmsLoadingStatus = SmsLoadingStatus.YET_TO_START
)

@HiltViewModel
class SmsListingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _uiState = MutableStateFlow(SmsListUiState())
    val uiState = _uiState.asStateFlow()

//    val smsMetaData = SmsListing.from(savedStateHandle)
    lateinit var smsMetaData : SmsMetaData

    fun loadSmsMetaData(smsMetaData : SmsMetaData){
        updateStatus(SmsLoadingStatus.LOADING)
        this.smsMetaData = smsMetaData
        updateStatus(SmsLoadingStatus.LOAD_MSGS(this.smsMetaData.smsList), sender = smsMetaData.sender)
    }

    private fun updateStatus(status: SmsLoadingStatus, sender: String? = null){
        _uiState.update { it.copy(loadingStatus = status, sender = it.sender ?: sender) }
    }
}