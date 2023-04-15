package com.example.myapplication.ui.delivery

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.OnIntentReceived
import com.example.myapplication.NavigationDrawerActivity
import com.example.myapplication.R
import com.example.myapplication.models.DeliveryImageModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


class DeliveryAdapter: RecyclerView.Adapter<DeliveryAdapter.DeliveryHolder>(), OnIntentReceived {
    var currentPhotoPath:String=""
    private var deliveryList:ArrayList<DeliveryModel> = ArrayList()
    private  lateinit var DeliveryManNoteEditText:EditText
    private lateinit var DeliveryManReceivedSumEditText:EditText
    private var mContext: Context? = null
    val REQUEST_IMAGE_CAPTURE=1001

    fun addItems(items:ArrayList<DeliveryModel>)
    {
        this.deliveryList=items
        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryHolder {
        val v= LayoutInflater.from(parent.context).inflate(R.layout.delivery_item, parent, false)
        mContext=v.context
        return DeliveryHolder(v);
    }


    override fun getItemCount(): Int {
        if (deliveryList.size==0)
            return 0
        return deliveryList.size
    }



    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DeliveryHolder, position: Int) {
        val activity1=holder.mContext as Activity
        if (deliveryList.size>0)
        {
            var delivery=deliveryList[position]
            holder.bindView(delivery)
            holder.itemView.setTag(delivery.id)
        }
    }



    fun getItemByPosition(itemIndex: Int): DeliveryModel
    {
        return deliveryList[itemIndex]
    }

    inner class DeliveryHolder(item: View): RecyclerView.ViewHolder(item)
    {
        var AddressTextView:TextView
        var PhoneTextView:TextView
        var CargoCodeTextView:TextView
        var PlaceTextView:TextView
        var CreatedDateTextView:TextView
        var IsMainSumPaidCheckBox:CheckBox
        var IsServiceSumPaidCheckBox:CheckBox
        var RegistratorNoteTextView:TextView
        var DeliveryManNoteTextView:TextView
        var DeliveryManReceivedSumTextView:TextView
        var MainSumTextView:TextView
        var ServiceSumTextView:TextView
        var ReceiverTextView:TextView
        var ItemIsDelivered:ImageView
        var itemPopupMenu: ImageButton

        var mContext: Context

        private lateinit var deliveryItem: DeliveryModel

        init {
            AddressTextView=item.findViewById(R.id.textViewAddress)
            PhoneTextView=item.findViewById(R.id.textViewPhone)
            CargoCodeTextView=item.findViewById(R.id.textViewCargo)
            PlaceTextView=item.findViewById(R.id.textViewPlace)
            CreatedDateTextView=item.findViewById(R.id.textViewCreatedDate)
            IsMainSumPaidCheckBox=item.findViewById(R.id.checkBoxIsMainSumPaid)
            IsServiceSumPaidCheckBox=item.findViewById(R.id.checkBoxIsServiceSumPaid)
            RegistratorNoteTextView=item.findViewById(R.id.textViewRegistratorNote)
            DeliveryManNoteTextView=item.findViewById(R.id.textViewDeliveryManNote)
            MainSumTextView=item.findViewById(R.id.textViewMainSum)
            ServiceSumTextView=item.findViewById(R.id.textViewServiceSum)
            ReceiverTextView=item.findViewById(R.id.textViewReceiver)
            ItemIsDelivered=item.findViewById(R.id.imageViewIsDelivered)
            DeliveryManReceivedSumTextView=item.findViewById(R.id.textViewDeliveryManReceivedSum)
            itemPopupMenu=item.findViewById(R.id.popupMenuDelivery)
            itemPopupMenu.setOnClickListener{showPopupMenu(it) }
            mContext=item.context
        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun bindView(delivery: DeliveryModel){

            AddressTextView.text=delivery.address
            PhoneTextView.text=delivery.telephone
            CargoCodeTextView.text=delivery.cargo_code
            PlaceTextView.text=delivery.place.toString()
            var formatter=DateTimeFormatter.ofPattern("dd.MM.yyyy")
            var createdDate=LocalDate.parse(delivery.created_date.take(10))
            CreatedDateTextView.text=createdDate.format(formatter).toString()
            IsMainSumPaidCheckBox.isChecked=delivery.is_main_sum_paid
            IsServiceSumPaidCheckBox.isChecked=delivery.is_service_sum_paid
            RegistratorNoteTextView.text=delivery.registrator_note
            DeliveryManNoteTextView.text=delivery.delivery_man_note
            MainSumTextView.text=delivery.main_sum.toString()
            ServiceSumTextView.text=delivery.service_sum.toString()
            ReceiverTextView.text=delivery.name
            DeliveryManReceivedSumTextView.text=delivery.delivery_man_received_sum.toString()
            delivery.IsDelivered=!delivery.delivered_date.isNullOrEmpty()
            deliveryItem=delivery
            if (delivery.IsDelivered)
            {
                ItemIsDelivered.setImageResource(R.drawable.accept_green)

            } else  ItemIsDelivered.setImageResource(R.drawable.accept_grey)
        }





        private fun deleteItemFromList(position: Int)
        {
            deliveryList.removeAt(position)
            notifyDataSetChanged()
        }

        private fun updateItem(delivery: DeliveryModel)
        {
            delivery.delivery_man_note=DeliveryManNoteEditText.text.toString()
            notifyDataSetChanged()
        }

        private  fun updateDeliveredStatus(delivery:DeliveryModel)
        {
            delivery.IsDelivered=!delivery.delivered_date.isNullOrEmpty()
            if (delivery.IsDelivered)
            {
                ItemIsDelivered.setImageResource(R.drawable.accept_green)

            } else  ItemIsDelivered.setImageResource(R.drawable.accept_grey)
            notifyDataSetChanged()
        }

        private fun initItems(v: View)
        {
            DeliveryManNoteEditText=v.findViewById(R.id.DeliveryManNoteEditText)
            DeliveryManReceivedSumEditText=v.findViewById(R.id.DeliveryManReceivedSumEditText)
        }

        private fun getItem(delivery: DeliveryModel)
        {
            DeliveryManNoteEditText.setText(delivery.delivery_man_note)
            DeliveryManReceivedSumEditText.setText(delivery.delivery_man_received_sum.toString())
        }


        private fun setItem(delivery: DeliveryModel)
        {
            delivery.delivery_man_note=DeliveryManNoteEditText.text.toString()
            delivery.delivery_man_received_sum= DeliveryManReceivedSumEditText.text.toString().toDouble()
            notifyDataSetChanged()
        }

        private fun setItemAsDelivered(delivery: DeliveryModel)
        {
            delivery.delivered_date=Date().toString()
            delivery.IsDelivered=true
            notifyDataSetChanged()
        }



        fun getActivity(context: Context?): Activity? {
            if (context == null) {
                return null
            } else if (context is ContextWrapper) {
                return if (context is Activity) {
                    context
                } else {
                    getActivity(context.baseContext)
                }
            }
            return null
        }

        private fun showPopupMenu(view: View)
        {
            val delivery=deliveryList[adapterPosition]

                val popupMenu: PopupMenu = PopupMenu(mContext, view)
                popupMenu.inflate(R.menu.delivery_popup_menu)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delivery_popup_edit -> {
                            val v = LayoutInflater.from(mContext).inflate(R.layout.delivery_edit_item, null)
                            initItems(v)
                            getItem(delivery)
                            AlertDialog.Builder(mContext)
                                .setView(v)
                                .setPositiveButton("OK") { dialog, _ ->
                                    setItem(delivery)
                                    getActivity(mContext)?.let { it1 ->
                                        SetNote(delivery, it1)

                                    }
                                    Toast.makeText(mContext, "Item is saved", Toast.LENGTH_SHORT)
                                        .show()
                                    dialog.dismiss()
                                }
                                .setNegativeButton("Cancel")
                                { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()

                            true
                        }
                        R.id.delivery_popup_set_delivery -> {
                            getActivity(mContext)?.let { it2 ->
                                SetDelivered(delivery, it2)
                            }
                            true
                        }
                        R.id.delivery_popup_add_image -> {
                            dispatchTakePictureIntent(mContext, delivery.id)
                            true
                        }
                        else -> true
                    }
                }
                popupMenu.show()

        }
        @Throws(IOException::class)
        private fun createImageFile(mContext: Context): File {

            // Create an image file name
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir: File? =mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            ).apply {
                // Save a file: path for use with ACTION_VIEW intents
                currentPhotoPath = absolutePath
            }
        }

        private fun dispatchTakePictureIntent(mContext: Context, deliveryId:Int) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent.resolveActivity(mContext.packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile(mContext)

                    } catch (ex: IOException) {
                        // Error occurred while creating the File

                        null
                    }


                    // Continue only if the File was successfully created
                    photoFile?.also {
                        (mContext as NavigationDrawerActivity).SetDeliveryImage(it.absolutePath, deliveryId)
                        val photoURI: Uri = FileProvider.getUriForFile(
                            mContext,
                            "com.example.myapplication.fileprovider",
                            it
                        )

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        getActivity(mContext)?.let { it3 ->
                            it3.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)

                        }


                    }
                }
            }
        }



        private fun SetNote(delivery: DeliveryModel, activity: Activity)
        {

            var serverURL:String= mContext.getString(R.string.main_site)+"Deliveries/SetDeliveryManNote"
            val client = OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30,
                TimeUnit.SECONDS).build()
            val requestBody: RequestBody =
                FormBody.Builder()
                    .add("note", delivery.delivery_man_note)
                    .add("delivery_id", delivery.id.toString())
                    .add("user_id", delivery.delivery_man_id.toString())
                    .add("received_sum", delivery.delivery_man_received_sum.toString())
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
                        val gson =  Gson()

                        val typeToken = object : TypeToken<List<DeliveryModel>>() {}.type
                        if (response.isSuccessful)
                        {

                            activity.runOnUiThread {
                              //  var deliveryModel=gson.fromJson<List<DeliveryModel>> (response.body!!.string(), typeToken);
                               // AddDeliveryItems(java.util.ArrayList(deliveryModel))
                            }
                        }
                    }
                    catch (e:Exception)
                    {
                        var error=e.message
                    }

                }
            })
        }

        private fun SetDelivered(delivery: DeliveryModel,  activity: Activity)
        {
            var serverURL:String= mContext.getString(R.string.main_site)+"Deliveries/SetDeliveredFromMobile"
            val client = OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30,
                TimeUnit.SECONDS).build()
            val requestBody: RequestBody =
                FormBody.Builder()
                    .add("delivery_id", delivery.id.toString())
                    .add("user_id", delivery.delivery_man_id .toString())
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
                            var bodyText=response.body?.string().toString();
                            if (bodyText=="1")
                            {
                                activity.runOnUiThread {
                                    setItemAsDelivered(delivery);
                                }
                            }

                        }
                    }
                    catch (e:Exception)
                    {
                        var error=e.message
                    }

                }
            })
        }
    }

    override fun onIntent(i: Intent?, resultCode: Int) {
        Toast.makeText(mContext,"Image was added in Adapter", Toast.LENGTH_SHORT).show()
    }


}