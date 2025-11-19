package ai.p2ach.p2achandroidlibrary.utils


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

object AppDialog {

    fun showAlert(
        context: Context,
        title: String? = null,
        message: String,
        positiveText: String = "확인",
        cancelable: Boolean = true,
        onPositive: (() -> Unit)? = null
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialog, _ ->
                dialog.dismiss()
                onPositive?.invoke()
            }
            .setCancelable(cancelable)

        if (!title.isNullOrEmpty()) {
            builder.setTitle(title)
        }

        val dialog = builder.create()
        dialog.show()
        return dialog
    }

    fun showConfirm(
        context: Context,
        title: String? = null,
        message: String,
        positiveText: String = "확인",
        negativeText: String = "취소",
        cancelable: Boolean = true,
        onPositive: (() -> Unit)? = null,
        onNegative: (() -> Unit)? = null
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialog, _ ->
                dialog.dismiss()
                onPositive?.invoke()
            }
            .setNegativeButton(negativeText) { dialog, _ ->
                dialog.dismiss()
                onNegative?.invoke()
            }
            .setCancelable(cancelable)

        if (!title.isNullOrEmpty()) {
            builder.setTitle(title)
        }

        val dialog = builder.create()
        dialog.show()
        return dialog
    }
}

fun Fragment.showAlertDialog(
    title: String? = null,
    message: String,
    positiveText: String = "확인",
    cancelable: Boolean = true,
    onPositive: (() -> Unit)? = null
): AlertDialog {
    return AppDialog.showAlert(
        requireContext(),
        title,
        message,
        positiveText,
        cancelable,
        onPositive
    )
}

fun Fragment.showConfirmDialog(
    title: String? = null,
    message: String,
    positiveText: String = "확인",
    negativeText: String = "취소",
    cancelable: Boolean = true,
    onPositive: (() -> Unit)? = null,
    onNegative: (() -> Unit)? = null
): AlertDialog {
    return AppDialog.showConfirm(
        requireContext(),
        title,
        message,
        positiveText,
        negativeText,
        cancelable,
        onPositive,
        onNegative
    )
}


fun Fragment.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
    startActivity(intent)
}