package com.example

import android.app.Activity
import android.app.ProgressDialog
import android.text.Editable
import android.widget.Toast
import com.example.myapplication.models.UserModel
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class AuthenticateUtility(activity: Activity) {
    var activity = activity;
    var dialog: ProgressDialog? = null
    var serverURL: String = "https://dlc312.ru/SignIn/Authenticate"
    val client = OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30,
        TimeUnit.SECONDS).build()




    fun test(t:Int):Int{
        return  -1;
    }

    fun toggleProgressDialog(show: Boolean) {
        activity.runOnUiThread {
            if (show) {
                dialog = ProgressDialog.show(activity, "", "Loading...", true);
            } else {
                dialog?.dismiss();
            }
        }
    }
}