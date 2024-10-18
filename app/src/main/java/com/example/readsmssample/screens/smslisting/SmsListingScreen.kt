package com.example.readsmssample.screens.smslisting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readsmssample.elements.FullScreenLoader
import com.example.readsmssample.elements.TopBar
import com.example.readsmssample.viewmodels.smslisting.SmsListingViewModel
import com.example.readsmssample.viewmodels.smslisting.SmsLoadingStatus
import com.example.readsmssample.viewmodels.smslisting.SmsMetaData
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SmsListingScreen(smsMetaData: SmsMetaData, viewModel: SmsListingViewModel = hiltViewModel()){

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            uiState.sender?.let { TopBar(title = it) }
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(it)){
            when(uiState.loadingStatus){
                is SmsLoadingStatus.YET_TO_START -> {
                    viewModel.loadSmsMetaData(smsMetaData)
                }
                is SmsLoadingStatus.LOADING -> {
                    FullScreenLoader()
                }
                is SmsLoadingStatus.LOAD_MSGS -> {
                    (uiState.loadingStatus as SmsLoadingStatus.LOAD_MSGS).list.let { smsList ->
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(10.dp)) {
                            if(smsList.isEmpty()){
                                item{
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("No messages")
                                    }
                                }
                            } else {
                                items(smsList){
                                    Card {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(it.message , maxLines = 3, overflow = TextOverflow.Ellipsis, color = Color.White)
                                            Calendar.getInstance().apply {
                                                timeInMillis = it.date
                                                val formatter: DateFormat =
                                                    SimpleDateFormat("HH:mm:ss", Locale.US)
                                                Text(formatter.format(Date(it.date)), fontSize = 12.sp, color = Color.LightGray)
                                            }


                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}
