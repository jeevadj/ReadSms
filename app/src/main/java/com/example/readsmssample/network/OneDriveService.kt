package com.example.readsmssample.network

import com.example.readsmssample.models.ApiCreateFolder
import com.example.readsmssample.models.GetOneDriveDataResponse
import com.example.readsmssample.models.OneDriveItem
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.PartMap
import retrofit2.http.Path
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OneDriveService @Inject constructor() {

    private val oneDriveBaseUrl = "https://graph.microsoft.com/v1.0/"

    fun getOneDriveApi(accessToken : String): OneDriveApi{

        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

        clientBuilder.addInterceptor(Interceptor { chain ->
            val original = chain.request()
            val builder1 = original.newBuilder()

            builder1.addHeader("Authorization","Bearer $accessToken")

            builder1.method(original.method, original.body)
            val request = builder1.build()

            val r: okhttp3.Response

            try {
                r = chain.proceed(request)
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                throw e
            }

            r
        })
        val client = clientBuilder.addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(oneDriveBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(OneDriveApi::class.java)
    }

}

object OneDriveApiConstants {
    const val ENDPOINT_USER = "me"
    const val ENDPOINT_ROOT_FOLDER = "me/drive/root/children"
    const val ENDPOINT_SUB_FOLDER = "me/drive/items/{item_id}/children"
    const val ENDPOINT_UPLOAD_FILE = "me/drive/items/{parent_id}:/{filename}:/content"
    const val PATH_PARAM_FOLDER_ID = "item_id"
    const val PATH_PARAM_PARENT_ID = "parent_id"
    const val PATH_PARAM_FILENAME = "filename"
}

interface OneDriveApi {
    @GET(OneDriveApiConstants.ENDPOINT_USER)
    suspend fun getUser() : Response<ResponseBody>

    @GET(OneDriveApiConstants.ENDPOINT_ROOT_FOLDER)
    suspend fun getDriveData() : Response<GetOneDriveDataResponse>

    @POST(OneDriveApiConstants.ENDPOINT_ROOT_FOLDER)
    suspend fun createFolderUnderRoot(@Body apiCreateFolder: ApiCreateFolder) : Response<OneDriveItem>

    @PUT(OneDriveApiConstants.ENDPOINT_UPLOAD_FILE)
    suspend fun uploadFile(@Path(OneDriveApiConstants.PATH_PARAM_PARENT_ID) parentId: String, @Path(OneDriveApiConstants.PATH_PARAM_FILENAME) fileName: String, @Body requestBody: RequestBody) : Response<OneDriveItem>

    @POST(OneDriveApiConstants.ENDPOINT_SUB_FOLDER)
    suspend fun createFolderUnderSubFolder(@Path(OneDriveApiConstants.PATH_PARAM_FOLDER_ID)folderId : String, @Body apiCreateFolder: ApiCreateFolder) : Response<OneDriveItem>

    @GET(OneDriveApiConstants.ENDPOINT_SUB_FOLDER)
    suspend fun getSubFolderData(@Path(OneDriveApiConstants.PATH_PARAM_FOLDER_ID)folderId : String) : Response<GetOneDriveDataResponse>
}

