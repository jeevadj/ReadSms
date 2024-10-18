package com.example.readsmssample.viewmodels.onedrive

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.readsmssample.models.OneDriveItem
import com.example.readsmssample.onedriveservice.OneDriveInitStatus
import com.example.readsmssample.repositories.OneDriveRepository
import com.example.readsmssample.utils.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.net.URI
import java.util.Stack
import javax.inject.Inject

data class OneDriveUiState(
    var isBackPressedEnabled : Boolean = false,
    var showCreateFolderAlert : Boolean = false,
    var userName : String? = null,
    var oneDriveInitStatus : OneDriveInitStatus = OneDriveInitStatus.None,
    var oneDriveData : OneDriveDataStatus = OneDriveDataStatus.None,
)

sealed class OneDriveDataStatus {
    data object None : OneDriveDataStatus()
    data object Loading : OneDriveDataStatus()
    data class LoadFailed(val message : String) : OneDriveDataStatus()
    data class LoadedData(val list : List<OneDriveItem>) : OneDriveDataStatus()
}

@HiltViewModel
class OneDriveViewModel @Inject constructor(
    private val oneDriveRepository: OneDriveRepository,
    private val imageUtils: ImageUtils,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OneDriveUiState())
    val uiState = _uiState.asStateFlow()

    private val hierarchy : Stack<OneDriveItem> = Stack()

    init {
        oneDriveRepository.init { oneDriveInitStatus -> updateOneDriveInitStatus(oneDriveInitStatus) }
    }

    private fun updateOneDriveInitStatus(status: OneDriveInitStatus){
        _uiState.update { it.copy(oneDriveInitStatus = status) }
    }

    fun loginOneDrive(activity: Activity) {
        oneDriveRepository.loginOneDrive(activity){ oneDriveInitStatus ->
            updateOneDriveInitStatus(status = oneDriveInitStatus )
        }
    }

    fun logoutOneDrive(){
        oneDriveRepository.logoutOneDrive { oneDriveInitStatus ->
            updateOneDriveInitStatus(oneDriveInitStatus)
        }
    }

    private fun updateOneDriveDataStatus(status : OneDriveDataStatus){
        _uiState.update {
            it.copy(oneDriveData = status, isBackPressedEnabled = hierarchy.isNotEmpty())
        }
    }

    fun toggleCreateFolderAlert(){
        _uiState.update { it.copy(showCreateFolderAlert = it.showCreateFolderAlert.not()) }
    }

    fun loadOneDriveData(){
        oneDriveRepository.loadOneDriveData(folderId = if(hierarchy.isEmpty()) null else hierarchy.peek().id){ oneDriveDataStatus ->
            updateOneDriveDataStatus(oneDriveDataStatus)
        }
    }

    fun addToStack(oneDriveItem: OneDriveItem) {
        hierarchy.push(oneDriveItem)
        loadOneDriveData()
    }

    fun popStack(){
        hierarchy.pop()
        loadOneDriveData()
    }

    fun createFolder(folderName : String){
        oneDriveRepository.createFolder(folderId = if(hierarchy.isEmpty()) null else hierarchy.peek().id,folderName = folderName){ oneDriveDataStatus ->
            updateOneDriveDataStatus(oneDriveDataStatus)
        }
    }

    fun uploadFile(uploadingFile : File){
        val parentId = if(hierarchy.isEmpty()) null else hierarchy.peek().id
        oneDriveRepository.uploadFile(parentId, uploadingFile.name, uploadingFile){ oneDriveDataStatus ->
            updateOneDriveDataStatus(oneDriveDataStatus)
        }
    }

    fun writeUriToFile(uri: Uri): File {
        return imageUtils.imageUriToFile(uri)
    }
}