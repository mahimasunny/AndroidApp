package edu.uw.ee590.sensors

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.arch.lifecycle.LiveData
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_events.*
import java.util.*
import kotlin.collections.ArrayList
import android.arch.lifecycle.Observer;
import android.content.Intent
import java.text.SimpleDateFormat

class EventsActivity : AppCompatActivity(), LogDataItemClickedListener {

    var rvLayoutAdapter : LogDataAdapter? = null;
    var listLogs : ArrayList<LogData>? = null;
    var repo: LogDataRepository? = null;

    init {
        listLogs = ArrayList<LogData>();
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events);

        btnEventsFinish.setOnClickListener {
            setResult(Activity.RESULT_OK, intent)
            finish();
        }

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

        imgFilterByDate.setOnClickListener {
            filterByDate();
        };


        btnClearFilter.setOnClickListener {
            populateLogs();
        };

        initRecyclerView();

        repo = LogDataRepository(this);
        populateLogs();


    }

    private fun filterByDate() {
        val cal = Calendar.getInstance()
        var y : Int = cal.get(Calendar.YEAR)
        var m : Int = cal.get(Calendar.MONTH)
        var d : Int = cal.get(Calendar.DAY_OF_MONTH)

        val dpicker = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener {datePicker, year, monthOfYear, dayOfMonth ->
                val yr = year.toString().padStart(2, '0');
                val mo = (monthOfYear + 1).toString();
                val dy = dayOfMonth.toString().padStart(2, '0');
                var sStart: String = "$mo/$dy/$yr";

                val dates = SimpleDateFormat("MM/dd/yyyy");
                var dtStart = dates.parse(sStart);

                //    var startMs : Long = dtStart.time;
                //    var endMs : Long = startMs + (24 * 3600 * 1000);

                Toast.makeText(this, sStart, Toast.LENGTH_LONG).show();

                var data : LiveData<List<LogData>>? = repo?.getByDate(dtStart);
                data?.observe(this, LogDataObserver());

            }, y, m, d);
        dpicker.show();
    }

    /*private fun showMap(){
        var intent: Intent = Intent(this, MapActivity::class.java);
        startActivityForResult(intent, PICK_GPS);
    }*/



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
        repo!!.deleteAllLogsTask();
        rvLayoutAdapter?.notifyDataSetChanged();
    }

    private fun addDataItem() {
        if (repo == null ) {
            return;
        }
        var now:Date = Date();
        var item:LogData = LogData(0, now.time, "47.715397, -122.170777",
            "blahblah.ogg",
            TriggerType.Elevated.toString(), 125);
        repo!!.insertLogDataTask(item);
        rvLayoutAdapter?.notifyDataSetChanged();
    }

    override fun OnLogDataItemClicked(position: Int) {
//        Toast.makeText(this, "Position: " + position, Toast.LENGTH_LONG).show();
        var intent: Intent = Intent(this, MapActivity::class.java);
        intent.putExtra("TIME", listLogs!!.get(position).timeStamp);
        intent.putExtra("LOCATION", listLogs!!.get(position).location);
        intent.putExtra("AUDIO", listLogs!!.get(position).audioFile);
        intent.putExtra("ALERT", listLogs!!.get(position).alertType);
        intent.putExtra("HEART_BEAT", listLogs!!.get(position).heartbeat);
        startActivityForResult(intent, PICK_GPS);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // TODO if needed
    }

    companion object {
        // these "request codes" are used to identify sub-activities that return results
        private val PICK_GPS = 1234
    }

}
