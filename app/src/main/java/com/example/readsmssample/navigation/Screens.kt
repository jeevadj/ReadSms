package com.example.readsmssample.navigation

import kotlinx.serialization.Serializable

sealed class Screens{
    @Serializable
    object LaunchScreen : Screens()
    @Serializable
    object SmsSendersListing : Screens()
    @Serializable
    data class SmsListing(val sender: String) : Screens()
    @Serializable
    object OneDrive : Screens()

}

//@Serializable
//data class SmsListing(val smsMetaData: SmsMetaData){
//    companion object{
//        val typeMap = mapOf(typeOf<SmsMetaData>() to parcelableType<SmsMetaData>())
//
//        fun from(savedStateHandle: SavedStateHandle) =
//            savedStateHandle.toRoute<SmsMetaData>(typeMap)
//    }
//}

