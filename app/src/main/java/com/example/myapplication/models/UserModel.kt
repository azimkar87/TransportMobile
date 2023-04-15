package com.example.myapplication.models

import java.util.Date
import java.io.Serializable

class UserModel(): Serializable {

    var id:Int=0
    var Login:String=""
    var Password:String=""
    var Fullname:String=""
    var Mail:String=""
    var Deleted:Boolean=false
    var UserTypeId:Int?=null
    var ChatId:Long? = null
}

