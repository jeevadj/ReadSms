package com.example.readsmssample.models

import com.google.gson.annotations.SerializedName

data class ApiCreateFolder(
    @SerializedName("name")
    val name : String? = null,
    @SerializedName("@microsoft.graph.conflictBehavior")
    val conflictBehavior : String? = "rename",
    @SerializedName("folder")
    val folder: Folder,

)