package edu.uw.anupm.pmp590

import android.arch.lifecycle.Observer;
import android.app.AlertDialog
import android.arch.lifecycle.LiveData
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.text.method.TextKeyListener.clear
import java.nio.file.Files.size
import android.provider.ContactsContract.CommonDataKinds.Note



class MainActivity : AppCompatActivity() , LogDataItemClickedListener {

    var rvLayoutAdapter : LogDataAdapter? = null;
    var listLogs : ArrayList<LogData>? = null;

    var repo: LogDataRepository? = null;

    init {
        listLogs = ArrayList<LogData>();
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

//        btnAdd.setOnClickListener {
//            Toast.makeText(this, "test", Toast.LENGTH_LONG).show();
//        }

        // CreateDummyLogs();

        btnClear.setOnClickListener {
            val dialog = AlertDialog.Builder(this);
            dialog.setTitle("Confirm Data Delete");
            dialog.setMessage("Are you sure you want to delete all data?");
            dialog.setPositiveButton("Confirm", {_,_ -> clearData()});
            dialog.setNegativeButton("Cancel", {_,_ -> });
            dialog.show();

        }

        btnAdd.setOnClickListener {
            addDataItem();
        }

        initRecyclerView();

        repo = LogDataRepository(this);
        populateLogs();

    }

    private fun initRecyclerView() {
        rvLayoutAdapter = LogDataAdapter(this, this);
        if (rvLayoutAdapter == null || listLogs == null) {
            return;
        }
        rvLayoutAdapter?.setLogs(listLogs!!);
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = rvLayoutAdapter!!;

    }

    inner class LogDataObserver() : Observer <List<LogData>> {
        override fun onChanged(logs: List<LogData>?) {
            if (listLogs == null) { return; }
            if (listLogs!!.size > 0) {
                listLogs!!.clear()
            }
            if (logs != null) {
                listLogs!!.addAll(logs);
            }
            else {

            }
            rvLayoutAdapter?.notifyDataSetChanged();
        }

    }

    private fun populateLogs() {
        // var obs:Observer = Observer<List<LogData>>();
        var data : LiveData<List<LogData>>? = repo?.allLogs;
        if (data == null) {
            return;
        }
        data.observe(this, LogDataObserver());
        // rvLayoutAdapter = LogDataAdapter(this, this);

    }

    private fun clearData() {

    }

    private fun addDataItem() {
        if (repo == null ) {
            return;
        }
        var now:Date = Date();
        var item:LogData = LogData(0, now.time);
        repo!!.insertLogDataTask(item);
        rvLayoutAdapter?.notifyDataSetChanged();
    }

    private fun CreateDummyLogs() {
        rvLayoutAdapter = LogDataAdapter(this, this);
        var listLogs:ArrayList<LogData> = ArrayList<LogData>();
        for (i in 1..100) {
            // listLogs
            var dt = Date();
            listLogs.add(LogData(i, dt.time));
        }

        rvItems.layoutManager = LinearLayoutManager(this)
        if (rvLayoutAdapter == null) {
            return;
        }

        rvItems.adapter = rvLayoutAdapter!!;
        rvLayoutAdapter?.setLogs(listLogs);
        rvLayoutAdapter?.notifyDataSetChanged();

    }

    override fun OnLogDataItemClicked(position: Int) {
        Toast.makeText(this, "Position: " + position, Toast.LENGTH_LONG).show();
    }

}
