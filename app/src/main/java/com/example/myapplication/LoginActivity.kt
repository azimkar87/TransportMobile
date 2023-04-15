package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.AuthenticateUtility
import com.example.myapplication.db.SQLiteHelper
import com.example.myapplication.models.UserModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var buttonLogin:Button
    private lateinit var loginEditText:EditText
    private lateinit var passwordEditText: EditText
    private lateinit var sqLiteHelper: SQLiteHelper
    private lateinit var userString:String
    private  lateinit var sharedPref:SharedPreferences
    private  lateinit var prefEditor:SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        buttonLogin=findViewById(R.id.buttonLogin)
        loginEditText=findViewById(R.id.editTextLogin)
        passwordEditText=findViewById(R.id.editTextPassword)
        sharedPref=this.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE) ?: return;
        val loginPref=sharedPref.getString("Login", "");
        val passwordPref=sharedPref.getString("Password", "");

        Authenticate(loginPref.toString(), passwordPref.toString());
        buttonLogin.setOnClickListener {
            Authenticate(loginEditText.text.toString(), passwordEditText.text.toString())
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        saveSharedPref()
        finish()
    }

    fun saveSharedPref()
    {
        if (this@LoginActivity.loginEditText.text.toString()!="" && this@LoginActivity.passwordEditText.text.toString()!="")
        {
            prefEditor=sharedPref.edit()
            prefEditor.putString("Login",this@LoginActivity.loginEditText.text.toString())
            prefEditor.putString("Password",this@LoginActivity.passwordEditText.text.toString())
            prefEditor.commit()
        }
    }

    fun Authenticate(login: String, password: String)
    {
        var serverURL: String = "https://dlc312.ru/SignIn/Authenticate"
        var bodyText="";

        val client = OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30,
            TimeUnit.SECONDS).build()
        val requestBody: RequestBody =
            FormBody.Builder()
                .add("login", login)
                .add("password", password)
                .build()
        val request: Request = Request.Builder().url(serverURL).post(requestBody).build()
        client.newCall(request).enqueue(object: Callback
        {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful)
                {
                    bodyText=response.body?.string().toString();
                    this@LoginActivity.runOnUiThread(java.lang.Runnable {
                        userString=bodyText;
                        if ( userString!="0")
                        {
                            saveSharedPref()
                            var intent =Intent(this@LoginActivity,NavigationDrawerActivity::class.java);
                            intent.putExtra("USER_OBJECT", userString);
                            startActivity(intent);
                        }
                        else Toast.makeText(this@LoginActivity,"User is not authorized",Toast.LENGTH_SHORT).show()
                    })
                }
            }

        })
    }
}