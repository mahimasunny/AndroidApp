package edu.uw.ee590.sensors

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.media.MediaRecorder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.NumberFormatException
import java.text.NumberFormat
import java.util.*
import kotlin.concurrent.timer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context

import android.view.View
import android.widget.AdapterView
import android.widget.SeekBar

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.collections.ArrayList
import android.location.LocationManager
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.telephony.SmsManager
import android.text.TextUtils
import android.widget.EditText
import android.widget.TextView
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.util.SortedList
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import edu.uw.ee590.sensors.BuildConfig.APPLICATION_ID
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.min
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() ,
                            BLE.Callback,
                            SafetyAudioRecord.AudioRecordFinishCallback { // }, OnMapReadyCallback {

    // private lateinit var mMap: GoogleMap //Create an instace of my map so that I can refer to it in my app

    private val TAG = "MainActivity"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private val SMS_PERMISSIONS_REQUEST_CODE = 33
    private var myLoc : Location? = null;

//    private var myLat : Double = 0.0
//    private var myLng : Double = 0.0

    private val SHOW_EVENTS_LIST: Int = 15;
    private val SHOW_SETTINGS: Int = 16;

    private var connected: Boolean = false;
    private var ble: BLE? = null;
    private var bpm: Int = 0;
    private lateinit var mSeriesBPM: LineGraphSeries<DataPoint>;
    private lateinit var hsim : HeartBeatSimulator;
    private var startTime : Long = 0;
    private var onAirRecording : Boolean = false;
    private var bpmQueue : SizeLimitedFloatQueue? = null;
    private var audioProvider : SafetyAudioRecord? = null;
    // var rvLayoutAdapter : LogDataAdapter? = null;
    var listLogs : ArrayList<LogData>? = null;
    var curItem : LogData? = null;
    var repo: LogDataRepository? = null;

    var bleString: String = "";
    val lock = ReentrantLock();
    var timerHR : Timer? = null;

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    init {
        listLogs = ArrayList<LogData>();
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
            if (!adapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

            }
        }

        ble = BLE(applicationContext, DEVICE_NAME);

        hsim = HeartBeatSimulator();

        // Check permissions
        ActivityCompat.requestPermissions(this,
            arrayOf( Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.VIBRATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1);

        val INTERNET_REQ_CODE = 5 // unique code for the permission request
        ActivityCompat.requestPermissions(this@MainActivity,
            arrayOf(Manifest.permission.INTERNET), INTERNET_REQ_CODE);

        mSeriesBPM = LineGraphSeries();
        initGraphRT(mGraphBPM, mSeriesBPM);

        startTime = System.currentTimeMillis();
        bpmQueue = SizeLimitedFloatQueue(15);
        repo = LogDataRepository(this);

        // test code:
        evaluateTestHook();
        // end test code:

        btnConnect.setOnClickListener {
            connectToArduino();
        }

        flabShowItems.setOnClickListener {
            showEvents();
        }

        imgSettings.setOnClickListener {
            showSettings();
        }

        swSimulatedData.setOnCheckedChangeListener { buttonView, isChecked ->
            testAutoGen = isChecked;
            // evaluateTestHook();
        }

        sbSimulateExt.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                               fromUser: Boolean) {
                    testSetBpm = progress;
                }
            });
    }

    private fun showSettings() {
        var intent: Intent = Intent(this, SettingsActivity::class.java);
        var wakeWords : String = WAKE_WORDS.joinToString();

        intent.putExtra("settings.wakewords", wakeWords);
        intent.putExtra("settings.vibrate", VIBRATE_FOR_WAKEWORD);
        intent.putExtra("settings.trigger", TRIGGER_VALUE.roundToInt());
        intent.putExtra("settings.emergencyphone", EMERGENCY_TEXT_PHONE);
        intent.putExtra("settings.enabled", ENABLED);
        intent.putExtra("settings.recordduration", RECORD_DURATION);
        intent.putExtra("settings.ble", DEVICE_NAME);
        intent.putExtra("settings.testing", TESTING_FEATURES);
        startActivityForResult(intent, SHOW_SETTINGS);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data);
        when(requestCode) {
            SHOW_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    VIBRATE_FOR_WAKEWORD = data?.getBooleanExtra("settings.vibrate", VIBRATE_FOR_WAKEWORD) ?:
                            VIBRATE_FOR_WAKEWORD;
                    TRIGGER_VALUE = data?.getIntExtra("settings.trigger", TRIGGER_VALUE.roundToInt())?.toFloat()
                        ?: TRIGGER_VALUE;
                    var sSafewords: String? = data?.getStringExtra("settings.wakewords");
                    if (sSafewords != null) {
                        WAKE_WORDS.clear();
                        var listSafe : List<String> = sSafewords.split(",");
                        for (str in listSafe) {
                            if (str.isNullOrBlank()) {
                                continue;
                            }
                            WAKE_WORDS.add(str.trim());
                        }
                    }

                    EMERGENCY_TEXT_PHONE = data?.getStringExtra("settings.emergencyphone")
                        ?: EMERGENCY_TEXT_PHONE;
                    ENABLED = data?.getBooleanExtra("settings.enabled", ENABLED) ?: ENABLED;
                    RECORD_DURATION = data?.getIntExtra("settings.recordduration", RECORD_DURATION)
                        ?: RECORD_DURATION;
                    TESTING_FEATURES = data?.getBooleanExtra("settings.testing", TESTING_FEATURES)
                        ?: TESTING_FEATURES;

                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); //set the map to hybrid mode
//        getMyLocationWithLocationAPI()
//    }
    /*
       * Uses Google PS LOcation API to get the Latitude and Longitude
       */
    fun getMyLocationWithLocationAPI(){
        //OPTION 2: USE GOOGLE PLAY SERVICES LOCATION API

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
        }
    }



    /**
     * Return the current state of the permissions needed.
     */

    private fun checkSMSPermissions() :Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
        if (result == PackageManager.PERMISSION_GRANTED) {
             return true
        } else {
            return false
        }
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
    }

    private fun requestPermissions() {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                Log.i(TAG, "Requesting permission")
                startLocationPermissionRequest()
            }

    }

    private fun requestSMSPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSIONS_REQUEST_CODE)
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,grantResults: IntArray   ) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                // Permission granted.
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> getLastLocation()
                // Permission denied.
                else -> {
                    View.OnClickListener {
                        // Build intent that displays the App settings screen.
                        val intent = Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                }
            }
        }
        else if (requestCode == SMS_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(
                    this@MainActivity,
                    "Permission accepted", Toast.LENGTH_LONG
                ).show()

            } else {
                Toast.makeText(
                    this,
                    "Permission denied", Toast.LENGTH_LONG
                ).show()

            }
        }
    }




    private fun showEvents() {
        var intent: Intent = Intent(this, EventsActivity::class.java);
        startActivityForResult(intent, SHOW_EVENTS_LIST);
    }



    //  Provides a simple way of getting a device's location

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {

                    myLoc = task.getResult();
                    if (myLoc != null) {
//                        Toast.makeText(
//                            this,
//                            "Latitude ${myLoc!!.latitude}, Longitude ${myLoc!!.longitude}",
//                            Toast.LENGTH_LONG
//                        ).show()
                    }
                } else {
                    Log.w(TAG, "getLastLocation:exception", task.exception)
                }
            }
    }



    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED


    private fun connectToArduino() {
        if (!connected) {
            writeLine("Scanning for devices ...")
            runOnUiThread {
                msgConnect.text = "Connecting...";
            };
            ble!!.connectFirstAvailable();
        }
    }

    private fun initGraphRT(mGraph: GraphView, series : LineGraphSeries<DataPoint>){

        mGraph.getViewport().setXAxisBoundsManual(true)
        mGraph.getViewport().setYAxisBoundsManual(true);
        mGraph.getViewport().setMinY(30.0);
        mGraph.getViewport().setMaxY(200.0);

        mGraph.getGridLabelRenderer().setLabelVerticalWidth(100)

        series.setDrawDataPoints(true)
        series.setDrawBackground(false)
        mGraph.addSeries(series)
        setLabelsFormat(mGraph,3,1)

    }

    /* Formatting the plot*/
    fun setLabelsFormat(mGraph:GraphView,maxInt:Int,maxFraction:Int){
        val nf = NumberFormat.getInstance()
        nf.setMaximumFractionDigits(maxFraction)
        nf.setMaximumIntegerDigits(maxInt)

        mGraph.getGridLabelRenderer().setVerticalAxisTitle("BPM")
        mGraph.getGridLabelRenderer().setHorizontalAxisTitle("Time (s)")

        mGraph.getGridLabelRenderer().setLabelFormatter(object : DefaultLabelFormatter(nf,nf) {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    super.formatLabel(value, isValueX)+ "s"
                } else {
                    super.formatLabel(value, isValueX)
                }
            }
        })

    }

    fun updateBPMGraph (bpm: Int) {
        runOnUiThread {
            var timeInSecs:Long = System.currentTimeMillis() - startTime;
            val xval = timeInSecs/1000.toDouble();
            mSeriesBPM!!.appendData(DataPoint(xval, bpm.toDouble()), true, 50)
        }
    }

    override fun onResume() {
        Log.d("AppLifecycle", "OnResume");
        super.onResume()
        //updateButtons(false)
        ble!!.registerCallback(this)

        evaluateTestHook();
    }

    override fun onStop() {
        Log.d("AppLifecycle", "OnStop");
        super.onStop()
        ble!!.unregisterCallback(this);
        if (timerHR != null) {
            timerHR?.cancel();
            timerHR = null;
        }
        // ble!!.disconnect()
    }

    /**
     * Called when a UART device is discovered (after calling startScan)
     * @param device: the BLE device
     */
    override fun onDeviceFound(device: BluetoothDevice) {
        writeLine("Found device : " + device.name)
        writeLine("Waiting for a connection ...")
    }

    /**
     * Prints the devices information
     */
    override fun onDeviceInfoAvailable() {
        writeLine(ble!!.deviceInfo)
    }

    /**
     * Called when UART device is connected and ready to send/receive data
     * @param ble: the BLE UART object
     */
    override fun onConnected(ble: BLE) {
        writeLine("Connected!")
        connected = true;
        runOnUiThread {
            msgConnect.text = "Connected";
        }
    }

    /**
     * Called when some error occurred which prevented UART connection from completing
     * @param ble: the BLE UART object
     */
    override fun onConnectFailed(ble: BLE) {
        writeLine("Error connecting to device!")
        runOnUiThread {
            msgConnect.text = "Connect";
        }
    }

    /**
     * Called when the UART device disconnected
     * @param ble: the BLE UART object
     */
    override fun onDisconnected(ble: BLE) {
        writeLine("Disconnected!")
        connected = false;
        runOnUiThread {
            msgConnect.text = "Connect";
        }
    }

    /**
     * Called when data is received by the UART
     * @param ble: the BLE UART object
     * @param rx: the received characteristic
     */
    override fun onReceive(ble: BLE, rx: BluetoothGattCharacteristic) {
        // if ()
        var sVal: String = rx.getStringValue(0)
        writeLine("Received value: " + sVal);

        lock.withLock {
            bleString += sVal;
        }
        runOnUiThread {
            evaluate();
        }
    }

    private fun evaluate() {
        if (bleString.isNullOrBlank()) {
            return;
        }
        if (bleString.length < 5) {
            // not enough characters populated yet
            return;
        }
        if (bleString.length > 400) {
            // too many messages piling up, lop off the earliest ones
            bleString = bleString.substring(300);
        }

        var start:Int = 0;
        var end:Int = -1;
        var msg:String = "";
        lock.withLock {
            // string will be something like this
            // "bpm:125critical:140bpm:140bpm:142"
            // or
            // "bpm:120"

            var end1:Int = bleString.indexOf("bpm:", start + 1);
            var end2:Int = bleString.indexOf("critical:", start + 1);
            var end3:Int = bleString.indexOf("interest:", start + 1);

            if (end1 < 0 && end2 < 0 && end3 < 0) {
                // no end found, we can take the whole string
                // by setting end = -1;
                // or wait for next reading to come in to gurantee the
                // message is not only a part of a full message as in bpm:12 and then m:0bpm:122
                // let's go with latter since otherwise heartrate was measured as 12
                return;
            } else {
                var idx : ArrayList<Int> = ArrayList<Int>();
                idx.add(end1);
                idx.add(end2);
                idx.add(end3);
                idx.sort();

                for (i in 0 until idx.size) {
                    if (idx[i] >= 0) {
                        end = idx[i];
                        break; // get the mimimum non-negative value and break;
                    }
                }
//                // both bpm: and critical: exist, take minimum
//                end = min(end1, end2);
            }
            if (end == -1) {
                msg = bleString.substring(0);
                bleString = "";
            } else {
                msg = bleString.substring(start, end);
                bleString = bleString.substring(end);
            }
        }
        if (ENABLED == false) {
            return; // do nothing since disabled
        }
        var trig: Trigger? = checkTriggers(msg);
        if (trig != null) {
            performActions(trig);
        }

    }

    private var testAutoGen : Boolean = false;
    private var testSetBpm : Int = 90;

    private fun evaluateTestHook() {
        var lbpm: Int = 0;
        if (!TESTING_FEATURES) {
            llTestTools.visibility = 0x00000008; // GONE;
            testAutoGen = false;
        } else {
            llTestTools.visibility = 0x00000000; //VISIBLE;
            // ok let's start the test hook
            timerHR = timer(initialDelay = 1000, period = 1000, action = {
                if (testAutoGen) {
                    lbpm = hsim.getCurrentBPM();

                } else {
                    lbpm = testSetBpm;
                }
                lock.withLock {
                    bleString += "bpm:" + lbpm;
                }
                runOnUiThread {
                    evaluate();
                }
            });

        }
    }

    private fun checkTriggers(msg: String) : Trigger? {

        var trigger: Trigger? = null;
        if (msg.isNullOrBlank()) {
            return null;
        }
        if (msg.length < 5 || msg.endsWith(":")) { // e.g. msg is just "bpm:"
            return null;
        }

        // trigger should be of type:
        // BPM:140
        // interest:140
        // or
        // critical:140

        var bpm: Int = -1;
        try {
            bpm = msg.substring(msg.lastIndexOf(":") + 1).toInt();
        } catch (nfe : NumberFormatException) {
            Log.e("checkTriggers", "Bad number format from BLE: $msg");
            return null;
        }
        if (msg.startsWith("critical:")) {
            trigger = Trigger(TriggerType.Critical, bpm);
        } else if (msg.startsWith("interest:")) {
            trigger = Trigger(TriggerType.Interested, bpm);
        } else if (msg.startsWith("bpm:")) {
            if (bpm > TRIGGER_VALUE) {
                trigger = Trigger(TriggerType.Elevated, bpm);
            } else {
                trigger = Trigger(TriggerType.Normal, bpm);
            }
        } else {
            return null;
        }

        return trigger;
    }

    private fun performActions(trigger: Trigger) {
        // sanitize the data, HR is not realistic
        if (trigger.bpm > 300 || trigger.bpm < 0) {
            return;
        }
        updateBPMGraph(trigger.bpm);
        bpmQueue?.add(trigger.bpm.toFloat());

        // if critical, start recording right away
        // otherwise
        // take an average over 10 data points (about 5 seconds worth of data). then if triggered, go into a "recording"
        // mode

        if (onAirRecording) { // already performing actions
            // TODO, can be promoted by Panic button on CP as well within first 20 seconds
            return;
        }

        if (trigger.triggerType == TriggerType.Critical || trigger.triggerType == TriggerType.Interested) {
            // actionsForType)
            onAirRecording = true;
        } else {
            // if bpm is elevated,
            val avgVal: Float? = bpmQueue?.averageOverLast(10);
            if (avgVal == null) {
                return;
            }
            if (avgVal!! >= TRIGGER_VALUE) {
                onAirRecording = true;
            } else {
                // does not meet trigger requirement
                return;
            }
        }

        // detected event for recording
        // start recording
        // runOnUiThread {
        //   Toast.makeText(this, "Starting Logging of data!", Toast.LENGTH_LONG).show();
        // }

        initiateLocationSearch();
        val timestamp : Long = Date().time;
        val recordingFile:String = "recording." + timestamp.toString() + ".mp3";

        curItem = LogData(0, timestamp, "0,0,0", recordingFile, trigger.triggerType.toString(), trigger.bpm);

        StartRecording(recordingFile, timestamp);

        // there is the special case where a safe word could promote a trigger from elevated to
        // critical. We will deal with that separately in the RecordFinish event.

    }

    private fun StartRecording(recording: String, timestamp: Long) {
        Log.d("MainActivity", "Starting Recording");
        // take 1 minute of recording
        // in 1 minute timer, call stop recording
        // in the meantime, chunk the data and speech recog
        // if safe word, elevate it, we will have the timestamp from
        audioProvider = SafetyAudioRecord(recording, timestamp, WAKE_WORDS, this);
        if (audioProvider == null) {
            LogErr("Unable to start recording");
        }

        showWakeWordRecorNotification();

        audioProvider?.performTimedRecord(RECORD_DURATION);

    }

    private fun showWakeWordRecorNotification() {
        Toast.makeText(this, "Starting Wake Word detection", Toast.LENGTH_LONG).show();

        if (VIBRATE_FOR_WAKEWORD) {
            // we will also vibrate the phone to signal to the user that safe word detection is active
            // in case the user does not have the phone in hand
            // vibration for 1.5 seconds
            val vib: Vibrator? = getSystemService(VIBRATOR_SERVICE) as Vibrator;
            if (Build.VERSION.SDK_INT >= 26) {
                vib?.vibrate(VibrationEffect.createOneShot(1500, 10));
            } else {
                vib?.vibrate(1500);
            }
        }
    }

    override fun OnRecordFinish() {
        onAirRecording = false;
        Log.d("REC1", "Finishing all logging and returning to clean state.");
        audioProvider = null;
    }

    override fun OnSafewordDetection(promotedToCritical: Boolean) {
        if (promotedToCritical) {
            Log.d("MainActivityP", "Promoted")
        } else {
            Log.d("MainActivityP", "Not Promoted")
        }

        if (curItem == null) { return; }

        if (promotedToCritical) {
            curItem!!.alertType = TriggerType.Critical.toString();
        }

        // at end of safe word detection timeout, we should have received location/GPS data also
        if (myLoc == null) {
            curItem!!.location = "0,0,0";
        } else {
            var locc: String = "${myLoc!!.latitude},${myLoc!!.longitude},";
            if (myLoc!!.hasAltitude()) {
                locc += myLoc!!.altitude.toString();
            } else {
                locc += "0";
            }
            curItem!!.location = locc;
        }

        addDataItem(curItem!!);

    }

    private fun addDataItem(item: LogData) {
        if (curItem?.alertType?.toLowerCase() == "critical") {
            sendSMS(curItem!!);
        }

        if (repo == null ) {
            return;
        }
        repo!!.insertLogDataTask(item);
    }

    private fun sendSMS(dataObj: LogData) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSMSPermissions()) {
                Log.e("permission", "Permission already granted.")
            } else {
                requestSMSPermissions()
            }
        }
        val sms = "Please help! I am in trouble. My location is: Latitude ${myLoc!!.latitude} Longitude: " +
                                                    "${myLoc!!.longitude} HeartRate: ${dataObj.heartbeat}"
        //String phoneNum = "+12698303616";
        val phoneNum = EMERGENCY_TEXT_PHONE;
        if (!TextUtils.isEmpty(sms) && !TextUtils.isEmpty(phoneNum)) {
//            message = findViewById<View>(R.id.message) as EditText
            if (checkSMSPermissions()) {

                //Get the default SmsManager//

                val smsManager = SmsManager.getDefault()

                //Send the SMS//

                smsManager.sendTextMessage(phoneNum, null, sms, null, null)

//                message.setText(sms, TextView.BufferType.EDITABLE)

            } else {
                Log.d("PERM", "Permission denied")
//                message.setText("No permissions to send messages", TextView.BufferType.EDITABLE)
            }
        }

    }



    private fun initiateLocationSearch() {
        getMyLocationWithLocationAPI();
    }

    private fun writeLine(s: String) : Unit {
        Log.d("BLE", s);
    }

    private fun LogErr(s: String) {
        Toast.makeText(this, "ERROR: $s", Toast.LENGTH_LONG).show();
        Log.e("Err", s);
    }


    companion object {
        // private val DEVICE_NAME = "Chari"

        private val DEVICE_NAME = "PMP590SafetyBeat"

        private val REQUEST_ENABLE_BT = 0

        private var WAKE_WORDS : MutableList<String> =
                    mutableListOf("lord save me", "i hit you because i love you", "bunny");

        private var VIBRATE_FOR_WAKEWORD = true;

        private var TRIGGER_VALUE: Float = 120f;

        private var EMERGENCY_TEXT_PHONE: String = "+12698303616";

        private var ENABLED : Boolean = true;

        private var RECORD_DURATION : Int = 120;

        private var TESTING_FEATURES : Boolean = false;

    }


}
