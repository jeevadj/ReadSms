package com.example.readsmssample.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.readsmssample.screens.LaunchScreen
import com.example.readsmssample.screens.onedrive.OneDriveScreen
import com.example.readsmssample.screens.smslisting.SmsListingScreen
import com.example.readsmssample.screens.smslisting.SmsSendersListingScreen
import com.example.readsmssample.viewmodels.SharedViewModel
import com.example.readsmssample.viewmodels.smslisting.SmsMetaData


@Composable
fun Navigator(sharedViewModel: SharedViewModel){

    Scaffold( modifier = Modifier.systemBarsPadding())
    { paddingValues ->
        val navController = rememberNavController()
        Box(modifier = Modifier.padding(paddingValues)){
            NavHost(navController, startDestination = Screens.LaunchScreen){
                composable<Screens.LaunchScreen>{
                    LaunchScreen{
                        navController.navigate(it)
                    }
                }
                composable<Screens.SmsSendersListing>{
                    SmsSendersListingScreen(sharedViewModel,onSenderSelected = { smsMetaData ->
                        navController.navigate(Screens.SmsListing(smsMetaData.sender))
                    })
                }

                composable<Screens.SmsListing>{ backStackEntry ->
                    val smsListing = backStackEntry.toRoute<Screens.SmsListing>()
                    SmsListingScreen(SmsMetaData(smsListing.sender, sharedViewModel.getSmsList(smsListing.sender) ?: arrayListOf()))
                }

                composable<Screens.OneDrive> {
                    OneDriveScreen()
                }
            }
        }
    }
}