package com.example

import android.content.Intent


interface OnIntentReceived {
    fun onIntent(i: Intent?, resultCode: Int)
}