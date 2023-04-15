package com.example.myapplication


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.db.SQLiteHelper
import com.example.myapplication.db.TrackingModel
import java.text.SimpleDateFormat


class TrackingAdapter: RecyclerView.Adapter<TrackingAdapter.TrackingHolder>()  {

    private var trkList:ArrayList<TrackingModel> = ArrayList()
    private lateinit var trackingCodeEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var foreignNameEditText:EditText
    private lateinit var cargoCodeEditText:EditText


    fun addItems(items:ArrayList<TrackingModel>)
    {
        this.trkList=items
        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackingHolder {
        val v=LayoutInflater.from(parent.context).inflate(R.layout.tracking_item, parent, false)


        return TrackingHolder(v);
    }


    override fun getItemCount(): Int {
        if (trkList.size==0)
            return 0
       return trkList.size
    }



    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TrackingHolder, position: Int) {
        if (trkList.size>0)
        {
            var trk=trkList[position]

                holder.bindView(trk)
            holder.itemView.setTag(trk.Id)
        }

    }

    fun setItemAsSended(itemId:Int)
    {
        if (itemId>=0)
        {
            var trk=trkList.filter { it.Id==itemId}.single()
            trk.IsSended=true
            notifyDataSetChanged()
        }
    }

    fun getItemByPosition(itemIndex: Int):TrackingModel
    {
        return trkList[itemIndex]
    }

    inner class TrackingHolder(item: View):RecyclerView.ViewHolder(item)
    {
         var itemTrackingCode:TextView
         var itemCreatedDate:TextView
         var itemWeight:TextView
         var itemForeignName:TextView
         var itemIsSended:ImageView
         var itemCargoCode:TextView
         var itemPopupMenu:ImageButton
         var mContext:Context

         private lateinit var trkItem:TrackingModel

        init {


            itemTrackingCode=item.findViewById(R.id.textViewTrackingCode)
            itemCreatedDate=item.findViewById(R.id.textViewCreatedDate)
            itemWeight=item.findViewById(R.id.textViewWeight)
            itemForeignName=item.findViewById(R.id.textViewForeignName)
            itemIsSended=item.findViewById(R.id.imageViewIsSended)
            itemCargoCode=item.findViewById(R.id.textViewCargoCode)
            itemPopupMenu=item.findViewById(R.id.popupMenuDelivery)
            itemPopupMenu.setOnClickListener{showPopupMenu(it) }
            mContext=item.context
        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun bindView(trk: TrackingModel){

            itemTrackingCode.text=trk.TrackingCode
            var createdDate = SimpleDateFormat("dd.MM.yyyy").format(trk.CreatedDate)

            itemCreatedDate.text=createdDate.toString()
            itemWeight.text=trk.Weight.toString()
            itemForeignName.text=trk.ForeignName
            itemCargoCode.text=trk.CargoCode
            trkItem=trk
            if (trk.IsSended)
            {
                itemIsSended.setImageResource(R.drawable.accept_green)

            } else  itemIsSended.setImageResource(R.drawable.accept_grey)
        }

        fun deleteItemSQL(trackingCode: String)
        {
            var sqLiteHelper=SQLiteHelper(mContext)
            sqLiteHelper.deleteTracking(trackingCode)
        }

        private fun deleteItemFromList(position: Int)
        {
            trkList.removeAt(position)
            notifyDataSetChanged()
        }

        private fun updateItem(position: TrackingModel)
        {
            position.TrackingCode = trackingCodeEditText.text.toString()
            position.Weight = weightEditText.text.toString().toDoubleOrNull()!!
            position.ForeignName = foreignNameEditText.text.toString()
            position.CargoCode = cargoCodeEditText.text.toString()
            notifyDataSetChanged()
        }

        private fun initItems(v:View)
        {
            trackingCodeEditText = v.findViewById<EditText>(R.id.trackingCodeEditText)
            weightEditText = v.findViewById<EditText>(R.id.weightEditText)
            foreignNameEditText = v.findViewById<EditText>(R.id.foreignNameEditText)
            cargoCodeEditText = v.findViewById<EditText>(R.id.cargoCodeEditText)
        }

        private fun getItem(position: TrackingModel)
        {
            trackingCodeEditText.setText(position.TrackingCode)
            weightEditText.setText(position.Weight.toString())
            foreignNameEditText.setText(position.ForeignName)
            cargoCodeEditText.setText(position.CargoCode)
        }

        private fun updateItemSQL(trk:TrackingModel)
        {
            var sqLiteHelper=SQLiteHelper(mContext)
            sqLiteHelper.updateTracking(trk)
        }

        private fun showPopupMenu(view:View)
        {
            val position=trkList[adapterPosition]
            if (! position.IsSended) {
                val popupMenu: PopupMenu = PopupMenu(mContext, view)
                popupMenu.inflate(R.menu.recyclerview_popup_menu)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_popup_edit -> {
                            val v = LayoutInflater.from(mContext).inflate(R.layout.edit_item, null)
                            initItems(v)
                            getItem(position)
                            AlertDialog.Builder(mContext)
                                .setView(v)
                                .setPositiveButton("OK") { dialog, _ ->
                                    updateItem(position)
                                    updateItemSQL(position)

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
                        R.id.action_popup_delete -> {
                            deleteItemSQL(trkItem.TrackingCode)
                            deleteItemFromList(adapterPosition)
                            Toast.makeText(
                                mContext,
                                "Delete menu clicked" + trkItem.TrackingCode,
                                Toast.LENGTH_SHORT
                            ).show()
                            true
                        }
                        else -> true
                    }
                }
                popupMenu.show()
            }
        }

    }


}