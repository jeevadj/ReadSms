package com.example.readsmssample.viewmodels.smslisting

import android.content.ContentResolver
import android.os.Parcelable
import android.provider.Telephony
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Parcelize
@Serializable
data class Sms(val sender : String, val message : String, val date : Long) : Parcelable

data class SmsListingUiState(
    val page : Int = 0,
    val showPaging : Boolean = false,
    val loadingStatus : SmsLoadingStatus = SmsLoadingStatus.YET_TO_START,
)

@Parcelize
@Serializable
data class SmsMetaData2(val sender : String, val smsList: Sms) : Parcelable

@Parcelize
@Serializable
data class SmsMetaData(val sender : String, val smsList: List<Sms> ) : Parcelable

sealed class SmsLoadingStatus {
    data object YET_TO_START : SmsLoadingStatus()
    data object LOADING : SmsLoadingStatus()
    data class LOADED(val list : List<String>) : SmsLoadingStatus()
    data class LOAD_MSGS(val list : List<Sms>) : SmsLoadingStatus()
}

@HiltViewModel
class SmsViewModel @Inject constructor(): ViewModel() {
    private val _uiState = MutableStateFlow(SmsListingUiState())
    val uiState = _uiState.asStateFlow()
    private var smsList : Map<String, List<Sms>> = hashMapOf()

    private val limit = 20

    fun updateLoadingStatus(status: SmsLoadingStatus, showPaging: Boolean = false, page : Int = 0){
        _uiState.update { it.copy(loadingStatus =  status, showPaging =  showPaging, page =  page) }
    }

    fun fetchSms(contentResolver: ContentResolver, onFetched: (Map<String, List<Sms>>) -> Unit){
        updateLoadingStatus(SmsLoadingStatus.LOADING)
        readSms(contentResolver,onFetched )
    }

    fun getSmsCount(sender : String) = smsList[sender]?.size ?: 0

    fun getSmsList(sender: String) =  smsList[sender]

    private fun readSms(contentResolver: ContentResolver, onFetched : (Map<String, List<Sms>>) -> Unit) {

        val smsList = arrayListOf<Sms>()
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val date = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
                smsList.add(Sms(address,body,date))
            } while (cursor.moveToNext())
        }
        updateSmsList(smsList)
        onFetched.invoke(smsList.groupBy {sms -> sms.sender })
        cursor?.close()
    }

    fun loadPage( increment : Boolean){
        updateLoadingStatus(SmsLoadingStatus.LOADING)
        val startIndex =
            if(increment) {
                _uiState.value.page.plus(1)
            } else {
                if(_uiState.value.page <= 0) 0 else (_uiState.value.page).minus(1)
            }
        val endIndex = startIndex.plus(1)
        if(endIndex.times(limit) <= smsList.keys.size){
            viewModelScope.launch {
//            delay(5000)
                updateLoadingStatus(
                    SmsLoadingStatus.LOADED(
                        smsList.keys.toList()
                            .subList(startIndex.times(limit), endIndex.times(limit))
                    ), showPaging = smsList.keys.size > limit, page = startIndex)
            }
        }
    }

    private fun updateSmsList(list : ArrayList<Sms>){
        smsList  = list.groupBy {sms -> sms.sender }
        val startIndex = _uiState.value.page
        val endIndex = startIndex.plus(1)
        viewModelScope.launch {
//            delay(5000)
            updateLoadingStatus(
                SmsLoadingStatus.LOADED(
                    smsList.keys.toList().subList(startIndex.times(limit), endIndex.times(limit))
                ), showPaging = smsList.keys.size > limit, page = startIndex)
        }
    }
}