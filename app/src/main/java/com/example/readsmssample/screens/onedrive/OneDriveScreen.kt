package com.example.readsmssample.screens.onedrive

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readsmssample.R
import com.example.readsmssample.elements.FullScreenLoader
import com.example.readsmssample.elements.TopBar
import com.example.readsmssample.helpers.CustomAlertDialog
import com.example.readsmssample.navigation.Screens
import com.example.readsmssample.onedriveservice.OneDriveInitStatus
import com.example.readsmssample.viewmodels.onedrive.OneDriveDataStatus
import com.example.readsmssample.viewmodels.onedrive.OneDriveUiState
import com.example.readsmssample.viewmodels.onedrive.OneDriveViewModel
import com.microsoft.identity.client.IAccount
import reactor.core.publisher.Sinks.One

@Composable
fun OneDriveScreen(
    oneDriveViewModel: OneDriveViewModel = hiltViewModel()
){
    val mContext = LocalContext.current
    var activeAccount by remember { mutableStateOf<IAccount?>(null) }
    val uiState by oneDriveViewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(uiState.isBackPressedEnabled) {
        oneDriveViewModel.popStack()
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = oneDriveViewModel.writeUriToFile(it)
            Log.d("File","file : ${file.absolutePath}")
            oneDriveViewModel.uploadFile(file)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map: Map<String, @JvmSuppressWildcards Boolean> ->

        val areGranted = map.values.reduce { acc, next -> acc && next }
        if (areGranted) {
            // Permission Accepted: Do something
            galleryLauncher.launch("image/*")
        } else {
            // Permission Denied: Do something
            CustomAlertDialog(
                mContext,
                message = "Need Photos and Media Permission To import Pic/Doc.",
                positiveButtonTitle = "Ok"
            )
        }
    }



    fun checkForStoragePermission(){
        val permissions = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE ) else arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES)
        if (
            permissions.all {
                ContextCompat.checkSelfPermission(
                    mContext,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            galleryLauncher.launch("image/*")
        } else {
            launcher.launch(permissions)
        }
    }

    Scaffold(
        topBar = {
            TopBar("OneDrive", subtitle = activeAccount?.username, content = {
                if(activeAccount != null){
                    Row {
                        IconButton(onClick = { oneDriveViewModel.toggleCreateFolderAlert() }) { Icon(Icons.Outlined.Add, contentDescription = "") }
                        IconButton(onClick = { checkForStoragePermission() }) { Icon(painterResource(R.drawable.baseline_upload_file_24), contentDescription = "", tint = Color.White) }
                        IconButton(onClick = { oneDriveViewModel.logoutOneDrive() }) { Icon(Icons.Outlined.ExitToApp, contentDescription = "") }
                    }
                }
            })
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            when(uiState.oneDriveInitStatus){
                is OneDriveInitStatus.Success -> {
                    (uiState.oneDriveInitStatus as OneDriveInitStatus.Success).let { status ->
                        activeAccount = status.activeAccount
                        Column(modifier = Modifier.align(Alignment.Center)) {
                            if(activeAccount == null){
                                TextButton(
                                    onClick = {
                                        oneDriveViewModel.loginOneDrive(mContext as Activity)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("Sign In To Continue")
                                }
                            } else {
                                SignInView(uiState, oneDriveViewModel)
                            }
                        }
                    }

                }
                is OneDriveInitStatus.Failed -> {
                    (uiState.oneDriveInitStatus as OneDriveInitStatus.Failed).apply {
                        exception?.printStackTrace()
                        Text(exception.toString(), modifier = Modifier.align(Alignment.Center))
                    }

                }
                is OneDriveInitStatus.None -> {
                    Text("Initiated", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }

    if(uiState.oneDriveData == OneDriveDataStatus.Loading){
        FullScreenLoader()
    }

    if(uiState.showCreateFolderAlert){
        ShowCreateFolderAlert{
            if(it != null){
                oneDriveViewModel.createFolder(it)
            }
            oneDriveViewModel.toggleCreateFolderAlert()
        }
    }
}

@Composable
fun SignInView(uiState: OneDriveUiState, viewModel: OneDriveViewModel){

    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    ){

        when(uiState.oneDriveData){
            is OneDriveDataStatus.None -> {
                viewModel.loadOneDriveData()
            }
            is OneDriveDataStatus.Loading -> {
            }
            is OneDriveDataStatus.LoadFailed -> {
                val message = (uiState.oneDriveData as OneDriveDataStatus.LoadFailed).message
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = message,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            is OneDriveDataStatus.LoadedData -> {
                (uiState.oneDriveData as OneDriveDataStatus.LoadedData).list.let { oneDriveItems ->
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(oneDriveItems){
                            Column(modifier = Modifier
                                .fillParentMaxWidth()
                                .heightIn(min = 56.dp)
                                .clickable {
                                    if(it.folder != null){
                                        viewModel.addToStack(it)
                                    }
                                },
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {

                                Box(modifier = Modifier.fillParentMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                    Text(it.name)

                                    it.folder?.let { folder ->
                                        Row(modifier = Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
                                            Text(folder.childCount.toString())
                                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "", tint = Color.White)
                                        }
                                    }

                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

    }

}

@Composable
fun ShowCreateFolderAlert( dismissAlert : (String?) -> Unit){
    var folderName by remember { mutableStateOf("") }
    Dialog(onDismissRequest = { dismissAlert.invoke(null) }, properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)) {
        Card {
            Column{
                Box(Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.Center){
                    Text("Create Folder", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.1f), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    TextField(modifier = Modifier.padding(0.dp), value = folderName, onValueChange = {
                        folderName = it
                    }, placeholder = {
                        Text("Enter Folder Name",color = Color.LightGray)
                    })
                }
                Row(modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { dismissAlert.invoke(null) }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = { dismissAlert.invoke(folderName) }) {
                        Text("Ok")
                    }
                }
            }
        }
    }
}