package com.example.readsmssample.screens.smslisting

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readsmssample.elements.FullScreenLoader
import com.example.readsmssample.elements.TopBar
import com.example.readsmssample.viewmodels.SharedViewModel
import com.example.readsmssample.viewmodels.smslisting.SmsLoadingStatus
import com.example.readsmssample.viewmodels.smslisting.SmsMetaData
import com.example.readsmssample.viewmodels.smslisting.SmsViewModel

@Composable
fun SmsSendersListingScreen(sharedViewModel: SharedViewModel, viewModel : SmsViewModel = hiltViewModel(), onSenderSelected : (SmsMetaData) -> Unit){
    val mContext = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopBar(title = "Text Messages")
        },
        bottomBar = {
            if(uiState.showPaging){
                Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.loadPage(false)  }, enabled = uiState.page != 0) {
                        Icon(imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft, contentDescription = "", tint = if(uiState.page != 0) Color.White else Color.LightGray.copy(alpha = 0.4f))
                    }
                    Text(uiState.page.plus(1).toString())
                    IconButton(onClick = { viewModel.loadPage(true) }) {
                        Icon(imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = "")
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(it)){
            when(uiState.loadingStatus){
                is SmsLoadingStatus.YET_TO_START -> {
                    viewModel.fetchSms(mContext.contentResolver, onFetched = {smsMap -> sharedViewModel.setSmsList(smsMap) })
                }
                is SmsLoadingStatus.LOADING -> {
                    FullScreenLoader()
                }
                is SmsLoadingStatus.LOADED -> {
                    (uiState.loadingStatus as SmsLoadingStatus.LOADED).list.let { sendersList ->
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(10.dp)) {
                            if(sendersList.isEmpty()){
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
                                items(sendersList){
                                    Column {
                                        Card(onClick = {
                                            viewModel.getSmsList(it).let { smsList ->
                                                if(smsList.isNullOrEmpty()){
                                                   Toast.makeText(mContext,"This cannot be opened", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    onSenderSelected.invoke(
                                                        SmsMetaData(sender = it, smsList)
                                                    )
                                                }
                                            }

                                        }, shape = RoundedCornerShape(10), colors = CardDefaults.cardColors(containerColor = Color.DarkGray)) {
                                            Row(modifier = Modifier.fillParentMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween){
                                                Text(modifier = Modifier, text = it, fontSize = 16.sp, color = Color.White)
                                                Text(modifier = Modifier, text = viewModel.getSmsCount(it).toString(), fontSize = 16.sp, color = Color.LightGray)
                                            }
                                        }
                                        Spacer(Modifier.height(10.dp))
                                    }
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