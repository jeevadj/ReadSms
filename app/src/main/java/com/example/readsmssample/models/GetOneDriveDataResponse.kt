package com.example.readsmssample.models

import com.google.gson.annotations.SerializedName

data class GetOneDriveDataResponse(
    @SerializedName("@odata.context")
    val context: String,
    @SerializedName("@odata.count")
    val count: Int,
    @SerializedName("@microsoft.graph.tips")
    val tips: String,
    val value: List<OneDriveItem>
)

data class OneDriveItem(
    @SerializedName("@microsoft.graph.downloadUrl")
    val downloadUrl: String?,
    val cTag: String?,
    val createdBy: ModifiedMetaData?,
    val createdDateTime: String?,
    val eTag: String?,
    val file: File?,
    val fileSystemInfo: FileSystemInfo?,
    val folder: Folder?,
    val id: String,
    val lastModifiedBy: ModifiedMetaData?,
    val lastModifiedDateTime: String?,
    val name: String,
    val parentReference: ParentReference?,
    val reactions: Reactions?,
    val size: Int?,
    val specialFolder: SpecialFolder?,
    val webUrl: String?
)

data class ModifiedMetaData(
    val application: Application,
    val device: Device,
    val oneDriveSync: OneDriveSync,
    val user: OneDriveUser
)

data class Device(
    val id: String
)

data class Application(
    val displayName: String,
    val id: String
)

data class File(
    val hashes: Hashes,
    val mimeType: String
)

data class FileSystemInfo(
    val createdDateTime: String,
    val lastModifiedDateTime: String
)

data class Folder(
    val childCount: Int? = null,
    val view: View?= null,
)

data class Hashes(
    val quickXorHash: String,
    val sha1Hash: String,
    val sha256Hash: String
)


data class OneDriveSync(
    @SerializedName("@odata.type")
    val type: String,
    val id: String
)

data class ParentReference(
    val driveId: String,
    val driveType: String,
    val id: String,
    val path: String
)

data class Reactions(
    val commentCount: Int
)

data class SpecialFolder(
    val name: String
)

data class View(
    val sortBy: String,
    val sortOrder: String,
    val viewType: String
)