package com.example.myapplication.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SQLiteHelper(context:Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
    {
        companion object {
            private const val DATABASE_VERSION=1
            private const val DATABASE_NAME="transport.db"
            private const val TBL_TRACKING="trackings"
            private const val ID="Id"
            private const val NAME="TrackingCode"
            private const val CARGO_CODE="CargoCode"
            private const val FILE_PATH="FilePath"
            private const val CREATED_DATE="CreatedDate"
            private const val IS_SENDED="IsSended"
            private const val WEIGHT="Weight"
            private const val FOREIGN_NAME="ForeignName"

//            private const val TBL_CARGO_CODES="cargo_codes"
//            private const val CC_ID="Id"
//            private const val CC_NAME="Name"
//            private const val

        }

        override fun onCreate(db: SQLiteDatabase?){
            val createdTblTracking=("CREATE TABLE  IF NOT EXISTS "+ TBL_TRACKING+"("+ID+" INTEGER PRIMARY KEY , "+ NAME+" TEXT NOT NULL UNIQUE, "+ CARGO_CODE+" TEXT, "+ FILE_PATH+" TEXT, "+ CREATED_DATE+ " NUMERIC, "+ WEIGHT+" REAL, "+ FOREIGN_NAME+" TEXT, " + IS_SENDED+ " NUMERIC"+")")
            db!!.execSQL(createdTblTracking)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("DROP TABLE IF EXISTS $TBL_TRACKING")
            onCreate(db)
        }

        fun refreshDB() {
            val db=this.writableDatabase
            db!!.execSQL("DROP TABLE IF EXISTS $TBL_TRACKING")
            onCreate(db)
        }

        fun setItemAsSended(trk: TrackingModel):Long
        {
            val db=this.writableDatabase
            val contentValues=ContentValues()
            contentValues.put(IS_SENDED, 1)
            val _success = db.update(TBL_TRACKING, contentValues, ID + "=?", arrayOf(trk.Id.toString())).toLong()
            db.close()
            return _success
        }

        @SuppressLint("Range")
        fun insertTracking(trk:TrackingModel):Long {
            var success:Long=0
            val db=this.writableDatabase
            val trackingCode=trk.TrackingCode
            val selectQuery="SELECT*FROM $TBL_TRACKING WHERE $NAME=\"$trackingCode\""
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {

                success=-100
            }
            else
            {
                val contentValues=ContentValues()
                //contentValues.put(ID, trk.Id)
                contentValues.put(NAME, trk.TrackingCode)
                val sdf = SimpleDateFormat("dd.MM.yyyy")
                val currentDate = sdf.format(Date())
                contentValues.put(CREATED_DATE, currentDate.toString())
                contentValues.put(FILE_PATH, trk.FilePath)
                contentValues.put(WEIGHT, trk.Weight)
                contentValues.put(IS_SENDED, 0)
                contentValues.put(FOREIGN_NAME, trk.ForeignName)
                contentValues.put(CARGO_CODE, trk.CargoCode)
                success=db.insert(TBL_TRACKING,null,contentValues)
                db.close()

            }
            cursor.close()
            return success
        }

        fun updateTracking(trk: TrackingModel)
        {
            val db = this.writableDatabase
            val contentValues=ContentValues()

            contentValues.put(NAME, trk.TrackingCode)
            contentValues.put(FILE_PATH, trk.FilePath)
            contentValues.put(WEIGHT, trk.Weight)
            contentValues.put(IS_SENDED, 0)
            contentValues.put(FOREIGN_NAME, trk.ForeignName)
            contentValues.put(CARGO_CODE, trk.CargoCode)
            db.update(TBL_TRACKING, contentValues, "TrackingCode=?", arrayOf(trk.TrackingCode))
            db.close()
        }

        fun deleteTracking(trackingCode: String) {

            // on below line we are creating
            // a variable to write our database.
            val db = this.writableDatabase

            // on below line we are calling a method to delete our
            // course and we are comparing it with our course name.
            db.delete(TBL_TRACKING, "$NAME=?", arrayOf(trackingCode))
            db.close()
        }


        @SuppressLint("Range")
        fun getNotSendedTrackings():ArrayList<TrackingModel>{
            val trkList:ArrayList<TrackingModel> = ArrayList()
            val selectQuery="SELECT*FROM $TBL_TRACKING WHERE $IS_SENDED=0"
            val  db=this.readableDatabase
            val cursor:Cursor?
            try {
                cursor=db.rawQuery(selectQuery,null)

            }catch (e:Exception)
            {
                e.printStackTrace()
                db.execSQL(selectQuery)
                return ArrayList()
            }
            var Id:Int
            var TrackingCode:String
            var FilePath:String?
            var CreatedDate:Date?
            var IsSended:Boolean
            var Weight:Double
            var ForeignName:String?
            var CargoCode:String?
            if (cursor.moveToFirst())
            {
                do {

                    try {
                        Id=cursor.getInt(cursor.getColumnIndex("Id"))
                        if (cursor.getColumnIndex("TrackingCode")!=null)
                        {
                            TrackingCode=cursor.getString(cursor.getColumnIndex("TrackingCode"))
                        } else TrackingCode="123"

                        if (cursor.getColumnIndex("FilePath")!=null)
                        {
                            FilePath=cursor.getString(cursor.getColumnIndex("FilePath"))
                        } else FilePath="123"
                        if (cursor.getColumnIndex("CreatedDate")!=null)
                        {
                            val dateString=cursor.getString(cursor.getColumnIndex("CreatedDate"))
                            val dateFormat:DateFormat=SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
                            CreatedDate= dateFormat.parse(dateString)
                        } else CreatedDate=Date()
                        IsSended=cursor.getInt(cursor.getColumnIndex("IsSended"))>0
                        Weight=cursor.getDouble(cursor.getColumnIndex("Weight"))
                        ForeignName=cursor.getString(cursor.getColumnIndex("ForeignName"))
                        CargoCode=cursor.getString(cursor.getColumnIndex("CargoCode"))
                        val trk=TrackingModel(Id=Id, TrackingCode=TrackingCode, CargoCode=CargoCode, FilePath=FilePath!!, CreatedDate=CreatedDate!!, Weight = Weight, IsSended=IsSended, ForeignName = ForeignName)
                        trkList.add(trk)
                    } catch (e:Exception)
                    {
                        e.printStackTrace()
                    }


                } while (cursor.moveToNext())
            }
            return trkList
        }

        private fun AddDayToDate(date: Date):Date
        {
            var dt = Date()
            val c = Calendar.getInstance()
            c.time = dt
            c.add(Calendar.DATE, 1)
            dt = c.time
            return dt
        }

        fun atStartOfDay(date: Date?): Date? {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            return calendar.time
        }

        fun atEndOfDay(date: Date?): Date? {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar[Calendar.HOUR_OF_DAY] = 23
            calendar[Calendar.MINUTE] = 59
            calendar[Calendar.SECOND] = 59
            calendar[Calendar.MILLISECOND] = 999
            return calendar.time
        }

        @SuppressLint("Range")
        fun getCargoCodes(searchStr:String):ArrayList<String>
        {
            val cargoCodeList:ArrayList<String> =ArrayList()
            val selectQuery="SELECT CargoCode FROM $TBL_TRACKING WHERE $CARGO_CODE LIKE \"%$searchStr%\" GROUP BY $CARGO_CODE ORDER BY COUNT(Id) DESC LIMIT 10"
            val  db=this.readableDatabase
            val cursor:Cursor?
            try {
                cursor=db.rawQuery(selectQuery,null)
            }catch (e:Exception)
            {
                e.printStackTrace()
                db.execSQL(selectQuery)
                return ArrayList()
            }
            var CargoCode:String
            if (cursor.moveToFirst())
            {
                do {
                    try {
                        if (cursor.getColumnIndex("CargoCode")!=null)
                        {
                            CargoCode=cursor.getString(cursor.getColumnIndex("CargoCode"))
                        } else CargoCode=""
                        cargoCodeList.add(CargoCode)

                    } catch (e:Exception)
                    {
                        e.printStackTrace()
                    }
                }while (cursor.moveToNext())
            }
            return  cargoCodeList
        }

        @SuppressLint("Range")
        fun getAllTrackings(dt:Date):ArrayList<TrackingModel>{
            val trkList:ArrayList<TrackingModel> = ArrayList()

            val dateFormat:DateFormat=SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
            val dtBegin= dateFormat.format(atStartOfDay(dt))
            val dtEnd=dateFormat.format(atEndOfDay(dt))

            val selectQuery="SELECT*FROM $TBL_TRACKING WHERE $CREATED_DATE BETWEEN  \"$dtBegin\" AND \"$dtEnd\""
            val  db=this.readableDatabase
            val cursor:Cursor?
            try {
                cursor=db.rawQuery(selectQuery,null)

            }catch (e:Exception)
            {
                e.printStackTrace()
                db.execSQL(selectQuery)
                return ArrayList()
            }
            var Id:Int
            var TrackingCode:String
            var FilePath:String?
            var CreatedDate:Date?
            var IsSended:Boolean
            var Weight:Double
            var ForeignName:String?
            var CargoCode:String?
            if (cursor.moveToFirst())
            {
                do {

                   try {
                       Id=cursor.getInt(cursor.getColumnIndex("Id"))
                       if (cursor.getColumnIndex("TrackingCode")!=null)
                       {
                           TrackingCode=cursor.getString(cursor.getColumnIndex("TrackingCode"))
                       } else TrackingCode="123"

                       if (cursor.getColumnIndex("FilePath")!=null)
                       {
                           FilePath=cursor.getString(cursor.getColumnIndex("FilePath"))
                       } else FilePath="123"
                       if (cursor.getColumnIndex("CreatedDate")!=null)
                       {
                           val dateString=cursor.getString(cursor.getColumnIndex("CreatedDate"))
                           val dateFormat:DateFormat=SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
                           CreatedDate= dateFormat.parse(dateString)
                       } else CreatedDate=Date()
                       IsSended=cursor.getInt(cursor.getColumnIndex("IsSended"))>0
                       Weight=cursor.getDouble(cursor.getColumnIndex("Weight"))
                       ForeignName=cursor.getString(cursor.getColumnIndex("ForeignName"))
                       CargoCode=cursor.getString(cursor.getColumnIndex("CargoCode"))
                       val trk=TrackingModel(Id=Id, TrackingCode=TrackingCode, CargoCode =CargoCode, FilePath=FilePath!!, CreatedDate=CreatedDate!!, Weight = Weight, IsSended=IsSended, ForeignName = ForeignName)
                       trkList.add(trk)
                   } catch (e:Exception)
                   {
                       e.printStackTrace()
                   }


                } while (cursor.moveToNext())
            }
            return trkList
        }
}