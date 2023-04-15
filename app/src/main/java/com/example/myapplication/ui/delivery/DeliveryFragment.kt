package com.example.myapplication.ui.delivery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.OnIntentReceived
import com.example.myapplication.NavigationDrawerActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentDeliveryBinding
import com.example.myapplication.models.UserModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class DeliveryFragment : Fragment() {
    val REQUEST_IMAGE_CAPTURE=1987
    private var mContext: Context? = null
    private var layoutManager: RecyclerView.LayoutManager?=null
    private var _binding: FragmentDeliveryBinding? = null
    private lateinit var recyclerView: RecyclerView
    private var adapter:DeliveryAdapter?=null
    private  lateinit var userModel: UserModel
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val deliveryViewModel: DeliveryViewModel by activityViewModels()
    private var mIntentListener: OnIntentReceived? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    private val binding get() = _binding!!


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeliveryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        recyclerView=root.findViewById<RecyclerView>(R.id.deliveryRecyclerView)
        swipeRefreshLayout = root.findViewById<SwipeRefreshLayout>(R.id.swipe)
        layoutManager= LinearLayoutManager(mContext)
        recyclerView.layoutManager=layoutManager
        adapter= DeliveryAdapter()
        recyclerView.adapter=adapter
        mIntentListener = adapter;
        deliveryViewModel.itemIndex.observe(viewLifecycleOwner, {
           // adapter?.setItemAsDelivered(it)
        })
        swipeRefreshLayout.setOnRefreshListener {
            var userModel=(mContext as NavigationDrawerActivity).GetActiveUser()
            LoadDeliveries(Date(), userModel.id)
            swipeRefreshLayout.setRefreshing(false)
        }
        return root
    }

    override fun onResume() {
        var userModel=(mContext as NavigationDrawerActivity).GetActiveUser()

        if (userModel!=null)  LoadDeliveries(Date(), userModel.id)
        super.onResume()
    }

    companion object {
        @JvmStatic
        fun newInstance()= DeliveryFragment()
    }

    public fun LoadDeliveries(dt: Date, user_id:Int)
    {
        GetDeliveries(dt, user_id, mContext as Activity)
    }

    fun AddDeliveryItems(deliveryList:ArrayList<DeliveryModel>)
    {
        adapter?.addItems(deliveryList!!)
    }

    fun GetDeliveries(_dt: Date, user_id: Int,_activity: Activity)
    {
        val sdf = SimpleDateFormat("dd.MM.yyyy")

        val dt = sdf.format(_dt)
        var serverURL:String=getString(R.string.main_site)+"Deliveries/GetDeliveriesForMobile"
        var activity = _activity;
        val client = OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30,
            TimeUnit.SECONDS).build()

        val httpBuilder= serverURL.toHttpUrlOrNull()!!.newBuilder()
        var url=httpBuilder
            .addQueryParameter("dt", dt.toString())
            .addQueryParameter("user_id", user_id.toString())
            .build().toString()

        val request: Request = Request.Builder().url(url).build()
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

                    getActivity()?.runOnUiThread {
                        var deliveryModel=gson.fromJson<List<DeliveryModel>> (response.body!!.string(), typeToken);
                        AddDeliveryItems(ArrayList(deliveryModel))
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



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mIntentListener?.onIntent(data, resultCode)
        Toast.makeText(mContext,"Image was added in Fragment", Toast.LENGTH_SHORT).show()
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {

        }
    }
}