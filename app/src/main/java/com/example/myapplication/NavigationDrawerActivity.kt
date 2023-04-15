package com.example.myapplication


import android.R.attr
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.databinding.ActivityNavigationDrawerBinding
import com.example.myapplication.db.SQLiteHelper
import com.example.myapplication.db.TrackingModel
import com.example.myapplication.models.DeliveryImageModel
import com.example.myapplication.models.UserModel
import com.example.myapplication.ui.delivery.DeliveryModel
import com.example.myapplication.ui.delivery.DeliveryViewModel
import com.example.myapplication.ui.home.HomeViewModel
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


class NavigationDrawerActivity : AppCompatActivity(), Communicator {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavigationDrawerBinding
    private lateinit var trackingList:ArrayList<TrackingModel>
    private lateinit var sqLiteHelper: SQLiteHelper
    private lateinit var userModel:UserModel
    var serverURL:String= "https://dlc312.ru/"+"Deliveries/AddFile"
    private val homeViewModel: HomeViewModel by viewModels()
    private val deliveryViewModel:DeliveryViewModel by viewModels()
    private lateinit var deliveryImageModel: DeliveryImageModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gson = Gson();
        var userString=intent.extras?.getString("USER_OBJECT") ?: ""

        userModel=gson.fromJson(userString, UserModel:: class.java);

        binding = ActivityNavigationDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sqLiteHelper=SQLiteHelper(applicationContext)
        binding.apply {
            val headerView: View = navView.getHeaderView(0);
            val navFullName : TextView = headerView.findViewById(R.id.txtFullName);
            val navLogin : TextView = headerView.findViewById(R.id.txtLogin);
            if (userModel!=null)
            {
                navFullName.text=userModel.Fullname;
                navLogin.text=userModel.Login;
            }


            navView.setNavigationItemSelectedListener {

                when(it.itemId)
                {
                    R.id.nav_upgrade->
                    {
                        sqLiteHelper.refreshDB();
                    }

                }

                true
            }

        }
        setSupportActionBar(binding.appBarNavigationDrawer.toolbar)

        binding.appBarNavigationDrawer.fab.setOnClickListener { view ->
            startActivity(Intent(this@NavigationDrawerActivity,MainActivity::class.java));
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_navigation_drawer)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_report, R.id.nav_upgrade, R.id.nav_delivery
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
      navView.setupWithNavController(navController)
    }
    public fun SetDeliveryImage(imagePath: String, deliveryId:Int)
    {

        if (imagePath!=null)
        {
            try
            {
                deliveryImageModel=DeliveryImageModel()
                deliveryImageModel.ImagePath=imagePath
                deliveryImageModel.DeliveryId=deliveryId
            }
            catch (e:Exception)
            {
                var error=e.message
            }

        }

    }
    fun compressImageFile(path:String, bitmap: Bitmap)
    {
        var out =  FileOutputStream(path);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, out);
        out.close();
    }

    private  fun LoadFragment(fragment:Fragment, title:String)
    {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val fragmentManager=supportFragmentManager
        val fragmentTransaction=fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment_content_navigation_drawer, fragment)
        fragmentTransaction.commit()
        drawerLayout.closeDrawers()
        setTitle(title)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.navigation_drawer, menu)
        return true
    }

    override fun passTrackingList(list: ArrayList<TrackingModel>) {
        trackingList=list
    }

    private fun sendToServer( trackingModel: TrackingModel)
    {
        UploadUtility(this).uploadFile(trackingModel.Id,trackingModel.FilePath, null,trackingModel)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        var index=0
        return if (id == R.id.action_settings) {
            trackingList=sqLiteHelper.getNotSendedTrackings()
            for (tracking in trackingList)
            {
                sendToServer(tracking)
                index++
            }
            true
        }
        else if (id==R.id.action_resend)
        {
            var dt:Date

            dt=Date()
            trackingList=sqLiteHelper.getAllTrackings(dt)
            for (tracking in trackingList)
            {
                sendToServer(tracking)
                index++
            }
            true
        }
        else super.onOptionsItemSelected(item)

    }

    override fun onBackPressed() {
        super.onBackPressed()


    }

    public fun SetAsSended(itemId: Int)
    {
        homeViewModel.setData(itemId)
    }

    public fun SetAsDelivered(itemId: Int)
    {
        deliveryViewModel.setData(itemId)
    }

    public fun GetActiveUser():UserModel
    {
        return userModel
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageBitmap = BitmapFactory.decodeFile(deliveryImageModel.ImagePath);
        compressImageFile(deliveryImageModel.ImagePath, imageBitmap)
        addImageToDelivery(deliveryImageModel)
    }

    fun addImageToDelivery(deliveryImageModel: DeliveryImageModel)
    {
        val client = OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30,
            TimeUnit.SECONDS).build()
        var file=File(deliveryImageModel.ImagePath);
        val mimeType = getMimeType(file);
        if (mimeType == null) {
            Log.e("file error", "Not able to get mime type")

        }
        val requestBody: RequestBody =
            MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("imageFile", file.name ,file.asRequestBody(mimeType?.toMediaTypeOrNull()))
                .addFormDataPart("delivery_id", deliveryImageModel.DeliveryId.toString())
                .addFormDataPart("userId", userModel.id.toString())
                .build()

        val request: Request = Request.Builder().url(serverURL).post(requestBody).build()

        client.newCall(request).enqueue(object: Callback
        {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                try
                {
                    if (response.isSuccessful)
                    {

                        Toast.makeText(this@NavigationDrawerActivity,"Успешно добавлено!", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e:Exception)
                {
                    var error=e.message
                }

            }

        })
    }

    fun getMimeType(file: File): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_navigation_drawer)

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}