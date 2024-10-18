package com.example.readsmssample.utils

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class ImageUtils @Inject constructor(@ApplicationContext private val mContext: Context){

    fun imageUriToFile(imageUri: Uri): File {
        val fileDir = mContext.getExternalFilesDir(null)
        val file = File(fileDir,"${UUID.randomUUID()}.jpg")
        val inputStream = mContext.contentResolver.openInputStream(imageUri)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)

        return file
    }
}