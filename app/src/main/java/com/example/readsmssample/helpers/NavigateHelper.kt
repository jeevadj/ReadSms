package com.example.readsmssample.helpers

sealed class NavigateTo {
    data object SmsListingScreen : NavigateTo()
}