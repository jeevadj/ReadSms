package com.example.readsmssample.helpers

import android.app.AlertDialog
import android.content.Context

fun CustomAlertDialog(
    mContext: Context,
    message: String,
    title: String? = null,
    negativeButtonTitle: String? = null,
    positiveButtonTitle: String? = null,
    cancelable : Boolean = false,
    onNegativeButtonClicked : (() -> Unit)? = null,
    onPositiveButtonClicked : (() -> Unit)? = null
) {

    AlertDialog.Builder(mContext).apply {
        setCancelable(cancelable)
        setMessage(message)
        setTitle(title)
        setNegativeButton(negativeButtonTitle) { _, _ ->
            onNegativeButtonClicked?.invoke()
        }
        setPositiveButton(positiveButtonTitle) { _, _ ->
            onPositiveButtonClicked?.invoke()
        }
    }.create().show()
}
