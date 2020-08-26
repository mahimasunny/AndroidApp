package edu.uw.ee590.sensors

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull
import android.support.annotation.Nullable
import java.util.*

@Entity(tableName = "logdata_table")
data class LogData (@PrimaryKey(autoGenerate = true) val id: Int,
                    @ColumnInfo(name = "timestamp") val timeStamp : Long,
                    @ColumnInfo(name = "location") var location: String,
                    @ColumnInfo(name = "audiofile") var audioFile : String,
                    @ColumnInfo(name = "alertType") var alertType : String,
                    @ColumnInfo(name = "heartbeat") var heartbeat : Int
) {

    init {
        // timeStampMs = System.currentTimeMillis();
        // timeStampMs = timeStamp; //.time;
        //        audioFile = "Test.mp3";
        //        location = "40.322112, 123.21123";
    }
}