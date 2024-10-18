package com.example.readsmssample.onedriveservice

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.readsmssample.R
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication.ISingleAccountApplicationCreatedListener
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication.CurrentAccountCallback
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


// https://entra.microsoft.com/#view/Microsoft_AAD_RegisteredApps/ApplicationMenuBlade/~/Overview/quickStartType~/null/sourceType/Microsoft_AAD_IAM/appId/ac41bc06-276d-4a3c-a7ec-c98643b553ea/objectId/48e47dcc-9945-42f6-b2f1-18a3c1b10abd/isMSAApp~/false/defaultBlade/Overview/appSignInAudience/AzureADMyOrg/servicePrincipalCreated~/true
sealed class OneDriveInitStatus {
    data object None : OneDriveInitStatus()
    data class Success(val activeAccount : IAccount?) : OneDriveInitStatus()
    data class Failed(val exception: MsalException?) : OneDriveInitStatus()
}


sealed class GetOneDriveAccessTokenCallBack {
    data class onTokenFetchSuccess(val accessToken : String?) : GetOneDriveAccessTokenCallBack()
    data class onTokenFetchFailed(val exception: MsalException?) : GetOneDriveAccessTokenCallBack()
}

class OneDriveHandler @Inject constructor(
    @ApplicationContext val mContext: Context
) {

    val TAG = "OneDriveHandler"
    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private var mAccount: IAccount? = null

    fun initOneDrive(status : (OneDriveInitStatus) -> Unit){
        PublicClientApplication.createSingleAccountPublicClientApplication(
            mContext,
            R.raw.auth_config_single_account,
            object : ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    /*
                         * This test app assumes that the app is only going to support one account.
                         * This requires "account_mode" : "SINGLE" in the config json file.
                         */
                    mSingleAccountApp = application
                    loadAccount(status)
                }

                override fun onError(exception: MsalException) {
                    status.invoke(OneDriveInitStatus.Failed(exception))
                }
            })
    }

    val scopes = arrayOf("user.read", "user.readwrite","Files.Read","Files.ReadWrite")

    fun loginOneDrive(activity : Activity, status: (OneDriveInitStatus) -> Unit){
        val signInParameters: SignInParameters = SignInParameters.builder()
            .withActivity(activity)
            .withLoginHint(null)
            .withScopes(listOf(*scopes))
            .withCallback(object : AuthenticationCallback {
                override fun onSuccess(authenticationResult: IAuthenticationResult?) {
                    Log.d(TAG, "Successfully authenticated")
                    Log.d(TAG, "ID Token: " + authenticationResult?.account?.claims!!["id_token"])

                    mAccount = authenticationResult.account
                    status.invoke(OneDriveInitStatus.Success(mAccount))
                }

                override fun onError(exception: MsalException?) {
                    status.invoke(OneDriveInitStatus.Failed(exception))
                }

                override fun onCancel() {
                    status.invoke(OneDriveInitStatus.Success(null))
                }

            })
            .build()
        mSingleAccountApp!!.signIn(signInParameters)
    }

    fun signOut(status: (OneDriveInitStatus) -> Unit){
        mSingleAccountApp!!.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback{
            override fun onSignOut() {
                mAccount = null
                status.invoke(OneDriveInitStatus.Success(null))
            }

            override fun onError(exception: MsalException) {
                status.invoke(OneDriveInitStatus.Failed(exception))
            }

        })
    }

    private fun loadAccount(status : (OneDriveInitStatus) -> Unit) {
        if (mSingleAccountApp == null) {
            return
        }
        mSingleAccountApp!!.getCurrentAccountAsync(object : CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) {
                // You can use the account data to update your UI or your app database.
                mAccount = activeAccount
                status.invoke(OneDriveInitStatus.Success(activeAccount))
            }

            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {
                if (currentAccount == null) {
                    // Perform a cleanup task as the signed-in account changed.
                }
            }

            override fun onError(exception: MsalException) {
                status.invoke(OneDriveInitStatus.Failed(exception))
            }
        })
    }

    fun getSilentAuthToken( callBack : (GetOneDriveAccessTokenCallBack) -> Unit ){

        val silentCallback = object : SilentAuthenticationCallback{
            override fun onSuccess(authenticationResult: IAuthenticationResult?) {
                callBack.invoke(GetOneDriveAccessTokenCallBack.onTokenFetchSuccess(authenticationResult?.accessToken))
            }

            override fun onError(exception: MsalException?) {
                callBack.invoke(GetOneDriveAccessTokenCallBack.onTokenFetchFailed(exception))
            }
        }

        val parameters = AcquireTokenSilentParameters.Builder()
            .fromAuthority(mAccount?.authority)
            .forAccount(mAccount)
            .withScopes(listOf(*scopes))
            .withCallback(silentCallback)
            .build()

        mSingleAccountApp!!.acquireTokenSilentAsync(parameters)
    }


}