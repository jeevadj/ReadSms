package com.example.readsmssample.repositories

import android.app.Activity
import android.util.Log
import com.example.readsmssample.models.ApiCreateFolder
import com.example.readsmssample.models.Folder
import com.example.readsmssample.models.OneDriveUser
import com.example.readsmssample.network.OneDriveService
import com.example.readsmssample.onedriveservice.GetOneDriveAccessTokenCallBack
import com.example.readsmssample.onedriveservice.OneDriveHandler
import com.example.readsmssample.onedriveservice.OneDriveInitStatus
import com.example.readsmssample.viewmodels.onedrive.OneDriveDataStatus
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class OneDriveRepository @Inject
constructor(
    private val oneDriveHandler: OneDriveHandler,
    private val oneDriveService : OneDriveService
) {

    fun init(callBack: (OneDriveInitStatus) -> Unit){
        oneDriveHandler.initOneDrive{ status ->
            callBack.invoke(status)
        }
    }

    fun loginOneDrive(activity: Activity, callBack: (OneDriveInitStatus) -> Unit){
        callBack.invoke(OneDriveInitStatus.None)
        oneDriveHandler.loginOneDrive(activity){ status ->
            callBack.invoke(status)
        }
    }

    fun logoutOneDrive(callBack: (OneDriveInitStatus) -> Unit){
        callBack.invoke(OneDriveInitStatus.None)
        oneDriveHandler.signOut { status ->
            callBack.invoke(status)
        }
    }

    fun uploadFile(parentId : String?, fileName : String,uploadingFile : File,  callBack: (OneDriveDataStatus) -> Unit) {
        callBack.invoke(OneDriveDataStatus.Loading)
        getToken { accessToken ->
            CoroutineScope(Dispatchers.IO).launch {
                val requestBody = uploadingFile.asRequestBody("image/jpg".toMediaTypeOrNull())

                val changedParentId = if(parentId.isNullOrEmpty()) "root" else parentId
                oneDriveService.getOneDriveApi(accessToken).uploadFile(changedParentId,fileName,requestBody).let { response ->
                    if(response.isSuccessful){
                        response.body()?.let { responseBody ->
                            Log.d("uploadFileSuccess","uploadFile success  : ${responseBody.name} ${responseBody.id}" )
                            loadOneDriveData(folderId = parentId,callBack)
                        }
                    } else {
                        val failedMsg = response.errorBody()?.string().toString()
                        Log.d("uploadFileFailed", failedMsg)
                        loadOneDriveData(folderId = parentId,callBack)
                    }
                }
            }
        }

    }


    fun createFolder(folderId: String? = null, folderName : String, callBack: (OneDriveDataStatus) -> Unit){
        callBack.invoke(OneDriveDataStatus.Loading)
        getToken { accessToken ->
            CoroutineScope(Dispatchers.IO).launch {
                val apiCreateFolder = ApiCreateFolder(name = folderName, folder = Folder())
                val request = if(folderId.isNullOrEmpty()) oneDriveService.getOneDriveApi(accessToken).createFolderUnderRoot(apiCreateFolder) else oneDriveService.getOneDriveApi(accessToken).createFolderUnderSubFolder(apiCreateFolder = apiCreateFolder, folderId = folderId)

                request.let { response ->
                    if(response.isSuccessful){
                        response.body()?.let { responseBody ->
                            Log.d("createFolderSuccess","FolderCreated : ${responseBody.name} ${responseBody.id}" )
                            loadOneDriveData(folderId,callBack)
                        }

                    } else {
                        val failedMsg = response.errorBody()?.string().toString()
                        Log.d("createFolderError", failedMsg)
                        loadOneDriveData(folderId,callBack)
                    }
                }
            }
        }
    }


    fun loadOneDriveData(folderId : String? = null, callBack: (OneDriveDataStatus) -> Unit ){
        callBack.invoke(OneDriveDataStatus.Loading)
        getToken { accessToken ->
            CoroutineScope(Dispatchers.IO).launch {
                val request = if(folderId.isNullOrEmpty()) oneDriveService.getOneDriveApi(accessToken).getDriveData() else oneDriveService.getOneDriveApi(accessToken).getSubFolderData(folderId)

                request.let { response ->
                    if(response.isSuccessful){
                        response.body()?.let { responseBody ->
                            callBack.invoke(OneDriveDataStatus.LoadedData(responseBody.value))
                        } ?: callBack.invoke(OneDriveDataStatus.LoadFailed("Response Empty"))
                    }else {
                        val failedMsg = response.errorBody()?.string().toString()
                        Log.d("getUserError", failedMsg)
                        callBack.invoke(OneDriveDataStatus.LoadFailed(failedMsg))
                    }
                }
            }
        }
    }

    private fun getToken( onTokenFetched : (String) -> Unit){
        oneDriveHandler.getSilentAuthToken { callback ->
            when(callback){
                is GetOneDriveAccessTokenCallBack.onTokenFetchSuccess -> {
                    if(callback.accessToken != null){
                        onTokenFetched.invoke(callback.accessToken)
                    } else {
                        Log.d("getUserError","Access TOken null")
                    }
                }
                is GetOneDriveAccessTokenCallBack.onTokenFetchFailed -> {
                    Log.d("GetOneDriveAccessTokenCallBack.onTokenFetchFailed", callback.exception?.toString().toString())
                }
            }
        }
    }

    fun getUser( onFetched : (String) -> Unit ){
        getToken{ accessToken ->
            CoroutineScope(Dispatchers.IO).launch {
                oneDriveService.getOneDriveApi(accessToken).getUser().let {
                    if(it.isSuccessful){
                        val user = Gson().fromJson(it.body()?.string() , OneDriveUser::class.java)
                        onFetched.invoke(user.displayName)
                    }else {
                        Log.d("getUserError",it.errorBody()?.string().toString())
                    }
                }
            }
        }
    }
}