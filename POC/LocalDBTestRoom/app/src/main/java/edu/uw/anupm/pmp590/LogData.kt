package edu.uw.anupm.pmp590

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull
import android.support.annotation.Nullable
import java.util.*

@Entity(tableName = "logdata_table")
data class LogData (@PrimaryKey(autoGenerate = true) val id: Int, @ColumnInfo(name = "timestamp") val timeStamp : Long) {

    @NonNull
    @ColumnInfo(name = "audiofile")
    var audioFile : String = "";

    @NonNull
    @ColumnInfo(name = "location")
    var location : String = "";

    @Nullable
    @ColumnInfo(name = "heartbeat")
    var heartbeat : String = "VeryHigh";

    init {
        // timeStampMs = System.currentTimeMillis();
        // timeStampMs = timeStamp; //.time;
        audioFile = "Test.mp3";
        location = "40.322112, 123.21123";
    }

//    constructor(@PrimaryKey(autoGenerate = true) id: Int, time: Date, s:String) : this(id) {
//        this.timeStamp = time.time;
//    }

}

//LogData (@PrimaryKey(autoGenerate = true) @ColumnInfo(name="id") val id : Int,
//              @ColumnInfo(name = "timestamp") val time: Date) {
//
//    init {
//        // this.id = id;
//    }
//
//    // @PrimaryKey(autoGenerate = true)
//    // val id : Int;
//
//    constructor (time : Date) : this (){
//
//    }
//
//}