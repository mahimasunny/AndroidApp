package edu.uw.anupm.pmp590

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface LogDataDao {

@Insert
fun insert(log: LogData) : Long;

@Query("SELECT * FROM logdata_table")
fun getAllData() : LiveData<List<LogData>>;

@Query("SELECT * FROM logdata_table WHERE timestamp >= :startTime AND timestamp <= :endTime")
fun getDataByDate(startTime : Long, endTime: Long) : LiveData<List<LogData>>;

@Query("DELETE FROM logdata_table")
fun deleteAllData();

@Delete
fun delete(log : LogData) : Unit;

// @Delete


}