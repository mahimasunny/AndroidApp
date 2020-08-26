package ee590pmp.uw.edu.anupm.safetybeat

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log

class MainActivity : AppCompatActivity(), BLE.Callback {

    private var connected: Boolean = false;
    private var ble: BLE? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("AppLifecycle", "OnCreate");
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
            if (!adapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

            }
        }

        ble = BLE(applicationContext, DEVICE_NAME);

        // Check permissions
        ActivityCompat.requestPermissions(this,
            arrayOf( Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), 1);

    }


    private fun connectToArduino() {
        if (!connected) {
            writeLine("Scanning for devices ...")
            runOnUiThread {
                // btnConnect.text = "Connecting...";
            };
            ble!!.connectFirstAvailable();
        }
    }

    override fun onResume() {
        Log.d("AppLifecycle", "OnResume");
        super.onResume()
        //updateButtons(false)
        ble!!.registerCallback(this)


    }

    override fun onStop() {
        Log.d("AppLifecycle", "OnStop");
        super.onStop()
        ble!!.unregisterCallback(this)
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
            // btnConnect.text = "Connected";
        }
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
        runOnUiThread {
            // btnConnect.text = "Connect";
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
        checkTriggered(sVal);
    }

    private fun checkTriggered(msg: String) {
        val triggered: Boolean = false;
        if (triggered) {
            // get list of actions
            val ls : List<String> = List<String>();
            for (i in ls) {

            }
        }
    }

    private fun writeLine(s: String) : Unit {
        Log.d("BLE", s);
    }


    companion object {
        private val DEVICE_NAME = "PMP590AnupM"
        private val REQUEST_ENABLE_BT = 0
    }

}
