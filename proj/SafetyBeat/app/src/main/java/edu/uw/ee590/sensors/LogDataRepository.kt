package edu.uw.ee590.sensors

import android.app.AlertDialog
import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.AsyncTask
import android.support.annotation.WorkerThread
import java.util.*
import android.os.AsyncTask.execute
import android.provider.ContactsContract.CommonDataKinds.Note
import android.util.Log
import android.widget.Toast


class LogDataRepository(val ctx: Context) {
    private val logDatabase : LogDatabase;

    init {
        logDatabase = LogDatabase.getDatabase(ctx);
    }


    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @WorkerThread
    suspend fun insert(dataObj: LogData) {
        logDatabase.logdataDao().insert(dataObj);
    }

    // The above works with coroutines and the workerthread tag, but without CoRoutines or MVVM, you need the following
    // helper method.
    fun insertLogDataTask(logData: LogData) {
        InsertLogAsyncTask(logDatabase.logdataDao()).execute(logData);
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allLogs : LiveData<List<LogData>> = logDatabase.logdataDao().getAllData();

    fun getByDate(date: Date) : LiveData<List<LogData>> {
        val startDate:Long = date.time;
        var endDate:Long = startDate + (3600*24*1000);
        // val dateEnd:Date = Date()
        return logDatabase.logdataDao().getDataByDate(startDate, endDate);
    }

    @WorkerThread
    suspend fun delete(dataObj: LogData) {
        logDatabase.logdataDao().delete(dataObj);
    }

    @WorkerThread
    suspend fun deleteAllData() {
        logDatabase.logdataDao().deleteAllData();
    }

    // The above works with coroutines and the workerthread tag, but without CoRoutines or MVVM, you need the following
    // helper method.
    fun deleteAllLogsTask() {
        DeleteAllLogsAsyncTask(logDatabase.logdataDao()).execute();
    }

}

class InsertLogAsyncTask(val logDataDao: LogDataDao) : AsyncTask<LogData, Void, Void>() {

    override fun doInBackground(vararg params: LogData?): Void? {
        val item: LogData = params!!.get(0)!!;
        if (item == null) {
            Log.d("LogDataRepository", "Null Item to add");
            // Toast.makeText(this, "null item", Toast.LENGTH_LONG).show();
            return null;
        }
        logDataDao.insert(item);
        return null;
    }

}

class DeleteAllLogsAsyncTask(val logDataDao: LogDataDao) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        logDataDao.deleteAllData();
        return null;
    }

}