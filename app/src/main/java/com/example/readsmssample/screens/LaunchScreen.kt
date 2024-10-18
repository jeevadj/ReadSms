package com.example.readsmssample.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.readsmssample.helpers.CustomAlertDialog
import com.example.readsmssample.helpers.NavigateTo
import com.example.readsmssample.navigation.Screens

@Composable
fun LaunchScreen( navigate : (Screens) -> Unit){
    val mContext = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission Accepted: Do something
            navigate.invoke(Screens.SmsSendersListing)
        } else {
            // Permission Denied: Do something
            CustomAlertDialog(
                mContext,
                message = "Need SMS Permission To Fetch SMS.",
                positiveButtonTitle = "Ok"
            )
        }
    }

    fun checkAndAskForSmsPermission(){
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.READ_SMS
            ) -> {
                // Some works that require permission
                navigate.invoke(Screens.SmsSendersListing)
            }
            else -> {
                // Asking for permission
                launcher.launch(Manifest.permission.READ_SMS)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = {
                checkAndAskForSmsPermission()
            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                Text("Read Sms", fontSize = 16.sp)
            }

            TextButton(onClick = {
                navigate.invoke(Screens.OneDrive)
            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                Text("OneDrive", fontSize = 16.sp)
            }
        }
    }
}