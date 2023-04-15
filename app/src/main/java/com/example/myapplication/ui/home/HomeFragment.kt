package com.example.myapplication.ui.home

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Communicator
import com.example.myapplication.NavigationDrawerActivity
import com.example.myapplication.R
import com.example.myapplication.TrackingAdapter
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.db.SQLiteHelper
import com.example.myapplication.db.TrackingModel
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {
    private var mContext: Context? = null
    private lateinit var sqLiteHelper: SQLiteHelper
    private var layoutManager:RecyclerView.LayoutManager?=null
    private lateinit var recyclerView: RecyclerView
    private lateinit var dateFilterEditText:EditText
    private lateinit var buttonShowCalendar: Button
    private lateinit var textViewQuantity:TextView
    private lateinit var textViewSendedQuantity:TextView
    private var adapter:TrackingAdapter?=null
    private var _binding: FragmentHomeBinding? = null
    private var trackingList:ArrayList<TrackingModel>?=null
    private val homeViewModel:HomeViewModel by activityViewModels()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object{
        @JvmStatic
        fun newInstance()=HomeFragment()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext=context
    }

    override fun onResume() {
        var dt:Date
        if (dateFilterEditText.text.toString()=="##.##.####")
        {
            dt=Date()
        }
        else dt = SimpleDateFormat("dd.MM.yyyy").parse(dateFilterEditText.text.toString())
        getTrackings(dt)
        super.onResume()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        sqLiteHelper= SQLiteHelper(mContext!!)
        recyclerView=root.findViewById<RecyclerView>(R.id.recyclerView)
        dateFilterEditText=root.findViewById(R.id.editTextDateFilter)
        buttonShowCalendar=root.findViewById(R.id.buttonShowCalendar)
        textViewQuantity=root.findViewById(R.id.textViewQuantity)
        textViewSendedQuantity=root.findViewById(R.id.textViewSendedQuantity)
        layoutManager= LinearLayoutManager(mContext)
        recyclerView.layoutManager=layoutManager
        adapter=TrackingAdapter()
        textViewQuantity.setText(adapter!!.itemCount.toString())
        recyclerView.adapter=adapter

        homeViewModel.itemIndex.observe(viewLifecycleOwner, {
            adapter?.setItemAsSended(it)
            textViewSendedQuantity.text= (textViewSendedQuantity.text.toString().toInt()+1).toString()
        })



        var cal = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd.MM.yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            dateFilterEditText.setText(sdf.format(cal.time))

        }
        buttonShowCalendar.setOnClickListener {
            if (dateFilterEditText.text.toString()!="##.##.####") {
                try {
                    val dt = SimpleDateFormat("dd.MM.yyyy").parse(dateFilterEditText.text.toString())
                    getTrackings(dt)
                    homeViewModel.setSelectedDate(dt)
                }catch (e: Exception) {
                    Toast.makeText(mContext,"Please select correct date!", Toast.LENGTH_SHORT).show()
                }

            }
        }
        dateFilterEditText.setOnClickListener{
            DatePickerDialog(
                mContext!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        return root
    }

    fun GetTrackingList():ArrayList<TrackingModel>
    {
        return trackingList!!
    }



    public fun getTrackings(dt:Date)
    {
        trackingList=sqLiteHelper.getAllTrackings(dt)
        adapter?.addItems(trackingList!!)
        textViewQuantity.setText(adapter!!.itemCount.toString())
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}