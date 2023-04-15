package com.example.myapplication

import com.example.myapplication.db.TrackingModel

interface Communicator {
    fun passTrackingList(list:ArrayList<TrackingModel>)
}