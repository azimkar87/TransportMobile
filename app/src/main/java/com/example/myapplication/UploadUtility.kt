package com.example.myapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentProviderOperation.newCall
import android.net.Uri
import android.os.AsyncTask.execute
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.db.SQLiteHelper
import com.example.myapplication.db.TrackingModel
import com.example.myapplication.ui.home.HomeFragment
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.TimeUnit


class UploadUtility(activity: Activity) {

    var activity = activity;
    private lateinit var sqLiteHelper: SQLiteHelper
    var dialog: ProgressDialog? = null
    //var serverURL: String = "https://dlc312.ru/Incomings/AddFile"
    var serverURL: String = "https://dlc312.ru/Incomings/AddFile"
    var serverUploadDirectoryPath: String = "https://dlc312.ru/Incomings/AddFile"
    val client = OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30,TimeUnit.SECONDS).build()

    fun uploadFile(itemId:Int, sourceFilePath: String, uploadedFileName: String? = null, trk:TrackingModel) {
        uploadFile(itemId, File(sourceFilePath), uploadedFileName, trk)
    }

    fun uploadFile(itemId:Int,sourceFileUri: Uri, uploadedFileName: String? = null, trk:TrackingModel) {
        try {
            val pathFromUri = URIPathHelper().getPath(activity,sourceFileUri)
            uploadFile(itemId,File(pathFromUri), uploadedFileName, trk)
        }
       catch (ex:Exception)
       {

       }
    }

    fun uploadFile(itemId:Int,sourceFile: File, uploadedFileName: String? = null, trk:TrackingModel){
        sqLiteHelper= SQLiteHelper(activity)
        Thread {
            val mimeType = getMimeType(sourceFile);
            if (mimeType == null) {
                Log.e("file error", "Not able to get mime type")
                return@Thread
            }
            val gson = Gson()
            val fileName: String = if (uploadedFileName == null)  sourceFile.name else uploadedFileName
            toggleProgressDialog(true)
            try {
                val requestBody: RequestBody =
                    MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("imageFile", fileName,sourceFile.asRequestBody(mimeType.toMediaTypeOrNull()))
                        .addFormDataPart("json", gson.toJson(trk))
                        .addFormDataPart("userId", "1")
                        .build() 

                val request: Request = Request.Builder().url(serverURL).post(requestBody).build()



                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    sqLiteHelper.setItemAsSended(trk)
                    Log.d("File upload","success, path: $serverUploadDirectoryPath$fileName")
                    showToast("File uploaded successfully at $serverUploadDirectoryPath$fileName", itemId)

                } else {
                    Log.e("File upload", "failed")
                    showToast2("File uploading failed")

                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.e("File upload", "failed")
                showToast2("File uploading failed")

            }
            toggleProgressDialog(false)
        }.start()
    }

    // url = file path or whatever suitable URL you want.
    fun getMimeType(file: File): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }


    fun showToast(message: String, itemId: Int) {
        activity.runOnUiThread {
            (activity as NavigationDrawerActivity).SetAsSended(itemId)

           // Toast.makeText( activity, message, Toast.LENGTH_LONG ).show()
        }
    }
    fun showToast2(message: String) {
        activity.runOnUiThread {

            Toast.makeText( activity, message, Toast.LENGTH_LONG ).show()
        }
    }

    fun toggleProgressDialog(show: Boolean) {
        activity.runOnUiThread {
            if (show) {
                dialog = ProgressDialog.show(activity, "", "Uploading file...", true);
            } else {
                dialog?.dismiss();
            }
        }
    }

}