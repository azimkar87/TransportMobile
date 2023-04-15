package com.example.myapplication.ui.delivery

import java.util.*

class DeliveryModel (
    var id:Int,
    var address:String,
    var telephone:String,
    var name:String,
    var cargo_code:String,
    var place:Int,
    var created_date:String,
    var is_main_sum_paid:Boolean,
    var is_service_sum_paid:Boolean,
    var main_sum:Double,
    var service_sum:Double,
    var registrator_note:String,
    var delivery_man_note:String,
    var IsDelivered:Boolean,
    var delivery_man_id:Int,
    var delivered_date:String,
    var delivery_man_received_sum:Double
) {

}