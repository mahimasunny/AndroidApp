package ee590pmp.uw.edu.anupm.arduinocommsvc

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.util.Log


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
            if (!adapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

            }
        }

        // Check permissions
        ActivityCompat.requestPermissions(this,
            arrayOf( Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), 1);

        btnStart.setOnClickListener {
            startSvc();
        }

        btnStop.setOnClickListener {
            stopSvc();
        }

    }

    private fun stopSvc() {
        var intent: Intent  = Intent(this, BLESafetyListenerService::class.java);
        stopService(intent);
    }

    private fun startSvc() {
        var intent: Intent  = Intent(this, BLESafetyListenerService::class.java);
        startService(intent);
    }

    override fun onStop() {
        super.onStop()
        Log.d("BLE-Svc", "Stopping main app");
    }

    companion object {
        private val REQUEST_ENABLE_BT = 0
    }

}
