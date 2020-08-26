package ee590pmp.uw.edu.anupm.arduinocommsvc

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.support.annotation.Nullable
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.startActivityForResult
// import org.junit.runner.Runner
import android.support.v4.os.HandlerCompat.postDelayed
import android.util.Log
import android.widget.Toast
import java.util.Timer
import kotlin.concurrent.withLock
import kotlin.concurrent.timer



class BLESafetyListenerService : Service(), BLE.Callback {

    //Declaring the handler
    private var handler: Handler? = null
    //Declaring your implementation of Runnable
    private var runner: Runner? = null
    private var connected: Boolean = false;
    private var ble: BLE? = null
    private var msgCt: Int = 0;
    private var tm : Timer? = null;

//    inner class myh : Handler() {
//        override fun handleMessage(msg: Message?) {
//            super.handleMessage(msg);
//        }
//    }

    init {
        handler = Handler();
        runner = Runner();
        tm = timer(initialDelay = 1000, period = 1000, action = {
            writeLine("Timer");
        });
    }

    inner class Runner : Runnable {
        override fun run() {
            Log.d("AndroidClarified", "Running")
            handler?.postDelayed(this, 1000 * 5);
            // handler?.postDelayed
        }

    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null;
    }

    override fun onCreate() {
        super.onCreate()

        ble = BLE(applicationContext, DEVICE_NAME);

        connectToArduino();

    }

    override fun onDestroy() {
        Log.d("SvcLifeCycle", "Destroy Service");
        // super.onDestroy()
        // handler?.removeCallbacks(runner);
        ble!!.unregisterCallback(this)
        // Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SvcLifeCycle", "Start Command");
        handler?.post(runner);
        return START_STICKY;
        // return super.onStartCommand(intent, flags, startId)
    }

    private fun connectToArduino() {
        if (!connected) {
            writeLine("Scanning for devices ...")
            ble!!.connectFirstAvailable();
        }
        ble!!.registerCallback(this)
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
        // queryTemperature();
    }

    /**
     * Called when some error occurred which prevented UART connection from completing
     * @param ble: the BLE UART object
     */
    override fun onConnectFailed(ble: BLE) {
        writeLine("Error connecting to device!")
    }

    /**
     * Called when the UART device disconnected
     * @param ble: the BLE UART object
     */
    override fun onDisconnected(ble: BLE) {
        writeLine("Disconnected!")
        connected = false;
    }

    /**
     * Called when data is received by the UART
     * @param ble: the BLE UART object
     * @param rx: the received characteristic
     */
    override fun onReceive(ble: BLE, rx: BluetoothGattCharacteristic) {
        // if ()
        var sVal: String = rx.getStringValue(0)
        writeLine("Received value number $msgCt : $sVal");
        checkTriggered(sVal);
        ++msgCt;
        if (msgCt > 120) {
            writeLine("Stopping Service!")
            this.stopSelf();
        }
    }

    private fun checkTriggered(msg: String) {
        // Toast.makeText(this, "Service BLE message: $msg", Toast.LENGTH_LONG).show();
        val triggered: Boolean = false;
        if (triggered) {
            // get list of actions
            val ls : List<String> = emptyList<String>();
            for (i in ls) {

            }
        }
    }

    private fun writeLine(s: String) : Unit {
        Log.d("BLE-Svc", s);
    }


    companion object {
        private val DEVICE_NAME = "PMP590SafetyBeat"
        private val REQUEST_ENABLE_BT = 0
    }

}
