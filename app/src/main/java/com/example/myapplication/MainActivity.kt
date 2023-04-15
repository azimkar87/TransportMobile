package com.example.myapplication

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.util.isNotEmpty
import com.example.myapplication.db.SQLiteHelper
import com.example.myapplication.db.TrackingModel
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.zxing.integration.android.IntentIntegrator
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE=1987
    var currentPhotoPath:String=""
    private val requestCodeCameraPermission=1001
    private lateinit var cameraSource:CameraSource
    private lateinit var detector: BarcodeDetector
    private lateinit var sqLiteHelper: SQLiteHelper
    private  lateinit var intentIntegrator: IntentIntegrator
//    private lateinit var textScanResult:TextView
//    private lateinit var cameraSurfaceView: SurfaceView
    private lateinit var imageView: ImageView
    private  lateinit var scanReadButton: Button
    private lateinit var addToListButton:Button
    private lateinit var backButton:Button
    private lateinit var editTextBarcode:  TextView
    private lateinit var textViewCreatedDate:TextView
    private lateinit var editTextWeight: EditText
    private lateinit var radioGroupCargoName:RadioGroup
    private lateinit var autoCompleteTextViewCargoCode : AutoCompleteTextView
    private  lateinit var sharedPref: SharedPreferences
    private  lateinit var prefEditor: SharedPreferences.Editor
    private var cargoCodeList:ArrayList<String>?=null
    var mAutoCompleteAdapter: ArrayAdapter<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        textScanResult=findViewById(R.id.textScanResult)
//        cameraSurfaceView=findViewById(R.id.cameraSurfaceView)
        initView()
        sharedPref=this.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE) ?: return;
        var selectedIndex=getSelectedRadioGroupIndex()
        setCheckedBySelectedIndex(selectedIndex)

        sqLiteHelper= SQLiteHelper(this)
        showQRReader();


        addToListButton.setOnClickListener {
            addTracking()
        }

        backButton.setOnClickListener{
            this.finish()
        }

        imageView.setOnClickListener()
        {
            var i=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
               dispatchTakePictureIntent()
            }
            catch (e:ActivityNotFoundException)
            {

            }
        }
        autoCompleteTextViewCargoCode.onFocusChangeListener=
            View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                getCargoCodes(autoCompleteTextViewCargoCode.text.toString())
                autoCompleteTextViewCargoCode.showDropDown()
            }
        }
        autoCompleteTextViewCargoCode.setOnKeyListener{ v, keyCode, event ->
            getCargoCodes(autoCompleteTextViewCargoCode.text.toString());
            return@setOnKeyListener false
        }

        scanReadButton.setOnClickListener{
            showQRReader()
        }

        if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)
        {
            askForCameraPermission()
        } else
        {
            setupControls()
        }
    }

    private fun setSelectedRadioGroupIndex(selectedIndex:Int)
    {
       // Toast.makeText(this,"setSelectedRadioGroupIndex.."+selectedIndex.toString(), Toast.LENGTH_SHORT).show()
        prefEditor=sharedPref.edit()
        prefEditor.putInt("RadioGroup",selectedIndex)
        prefEditor.commit()
    }

    private fun getSelectedRadioGroupIndex():Int
    {
        val selectedIndex=sharedPref.getInt("RadioGroup", 0);
       // Toast.makeText(this,"getSelectedRadioGroupIndex.."+sharedPref.getInt("RadioGroup", 0).toString(), Toast.LENGTH_SHORT).show()
        return selectedIndex
    }

    private fun showQRReader()
    {
        intentIntegrator= IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode");
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setBarcodeImageEnabled(true);
        intentIntegrator.initiateScan();
    }

    private fun getCargoNameFromRadioButtons():String
    {
        var selectedId = radioGroupCargoName.getCheckedRadioButtonId()
        var radioButton = findViewById<RadioButton>(selectedId)
       // Toast.makeText(this,"getCargoNameFromRadioButtons.."+radioButton.text.toString(), Toast.LENGTH_SHORT).show()
        return  radioButton.text.toString()
    }

    private fun setCheckedBySelectedIndex(selectedIndex: Int)
    {
      //  Toast.makeText(this,"setCheckedBySelectedIndex.."+selectedIndex.toString(), Toast.LENGTH_SHORT).show()
        radioGroupCargoName.check(radioGroupCargoName.getChildAt(selectedIndex).id)
    }

    private fun addTracking()
    {
        val sdf2=SimpleDateFormat("ddMMyyyyHHmmssZ")
        val currentDate2 = sdf2.format(Date())
        val trackingCode=editTextBarcode.text?.let { editTextBarcode.text.toString() }  ?: currentDate2.toString()
        val cargoCode=autoCompleteTextViewCargoCode.text.toString()
        val weight=editTextWeight.text.toString()
        val foreignName=getCargoNameFromRadioButtons()
        val filePath=currentPhotoPath?.let { currentPhotoPath }  ?: ""
        val sdf = SimpleDateFormat("dd.MM.yyyy")

        val currentDate = sdf.format(Date())
        val trk=TrackingModel(Id=0, TrackingCode = trackingCode, CargoCode = cargoCode, Weight = weight.toDouble(), FilePath =filePath, IsSended = false, CreatedDate = Date(), ForeignName = foreignName)
        val status=sqLiteHelper.insertTracking(trk)
        if (status>-1)
        {
            Toast.makeText(this,"Tracking added..", Toast.LENGTH_SHORT).show()
            var selectedIndex=getSelectedRadioGroupIndex()
            var newSelectedIndex=radioGroupCargoName.indexOfChild(findViewById(radioGroupCargoName.checkedRadioButtonId))
            if (selectedIndex!=newSelectedIndex)
            {
                setSelectedRadioGroupIndex(newSelectedIndex)
            }
            clearCardValues()
            showQRReader()
        }
        else if (status>-100)
        {
            Toast.makeText(this,"Record not saved..", Toast.LENGTH_SHORT).show()
        }
        else
        {
            Toast.makeText(this,"Record is dublicated, scan other track code..", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCargoCodes(searchStr:String)
    {

        cargoCodeList=sqLiteHelper.getCargoCodes(searchStr)
        mAutoCompleteAdapter = ArrayAdapter<String>(
            this@MainActivity,
            android.R.layout.simple_dropdown_item_1line, cargoCodeList!!
        )
        autoCompleteTextViewCargoCode.setAdapter(mAutoCompleteAdapter)
        var txt= autoCompleteTextViewCargoCode.text
        autoCompleteTextViewCargoCode.text=txt
        autoCompleteTextViewCargoCode.setSelection(txt.length)

    }

    private fun clearCardValues()
    {
        editTextBarcode.setText("")
        editTextWeight.setText("")
        autoCompleteTextViewCargoCode.text.clear()
        imageView.setImageDrawable(null)
    }
    private fun initView()
    {
        imageView=findViewById(R.id.barcodeViewer)
        editTextBarcode=findViewById(R.id.barcodeEditText)
        editTextWeight=findViewById(R.id.editTextWeight)
        addToListButton=findViewById(R.id.addToListButton)
        scanReadButton=findViewById(R.id.scanReadButton)
        radioGroupCargoName=findViewById(R.id.radioGroup)
        autoCompleteTextViewCargoCode=findViewById(R.id.autoCompleteTextViewCargoCode)
        backButton=findViewById(R.id.backButton)
       // barcodeImageViewer=findViewById(R.id.barcodeViewer)
    }



    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? =  getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
           // setImageBitmap()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()

                } catch (ex: IOException) {
                    // Error occurred while creating the File

                    null
                }


                // Continue only if the File was successfully created
                photoFile?.also {

                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.myapplication.fileprovider",
                        it
                    )

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }
    private fun setupControls()
    {
        detector=BarcodeDetector.Builder(this@MainActivity).build()
        cameraSource=CameraSource.Builder(this@MainActivity, detector).setAutoFocusEnabled(true).build()
       // cameraSurfaceView.holder.addCallback(surfaceCallback)
        detector.setProcessor(processor)
    }

    private fun askForCameraPermission()
    {
        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.CAMERA), requestCodeCameraPermission)
    }

    fun compressImageFile(path:String, bitmap: Bitmap)
    {
        var out =  FileOutputStream(path);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, out);
        out.close();
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

                setImageBitmap();
            }
            var intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (intentResult != null) {
                if (intentResult.contents == null) {
                    Toast.makeText(
                        applicationContext,
                        " I can not read barcode, please take a closer picture",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                    editTextBarcode.setText(intentResult.contents);
                    currentPhotoPath=intentResult.barcodeImagePath;
                    setImageBitmap();

                }
            }
        }
        catch (exception:Exception)
        {
            Toast.makeText(applicationContext, " I can not read barcode, please take a closer picture", Toast.LENGTH_SHORT).show()
        }

    }

    fun setImageBitmap()
    {
        val imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
        compressImageFile(currentPhotoPath, imageBitmap)
        imageView.setImageBitmap(imageBitmap);
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==requestCodeCameraPermission && grantResults.isNotEmpty())
        {
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                setupControls()
            } else
            {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val surfaceCallback = object : SurfaceHolder.Callback{
        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            cameraSource.stop()
        }

        override fun surfaceCreated(p0: SurfaceHolder) {
            try {
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                cameraSource.start(p0)
            }
            catch (exception:Exception)
            {
                Toast.makeText(applicationContext,"Something went wrong", Toast.LENGTH_SHORT)
            }
        }
    }

    private val processor=object : Detector.Processor<Barcode>
    {
        override fun release() {
            TODO("Not yet implemented")
        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            if (detections!=null && detections.detectedItems.isNotEmpty()){
                val qrCodes:SparseArray<Barcode> = detections.detectedItems
                val code=qrCodes.valueAt(0)
//                textScanResult.text=code.displayValue
            }
            else
            {
//                textScanResult.text=""
            }
        }
    }

}


