package edu.uw.ee590.sensors

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_events.*
import kotlinx.android.synthetic.main.activity_settings.*
import java.lang.NumberFormatException

class SettingsActivity : AppCompatActivity() {

    var initDuration : Int? = null;
    var initWakewords : String? = null;
    var initTrigger : Int? = null;
    var initEmergencyPhone : String? = null;
    var initEnabled : Boolean? = null;
    var initVibrate : Boolean? = null;
    var initTesting : Boolean? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initSettings();

        btnSettingsFinish.setOnClickListener {
            var duration: Int = initDuration!!;
            var trigger: Int = initTrigger!!;

            try {
                duration = editDuration.text.toString().toInt();
                trigger = editTrigger.text.toString().toInt();

            } catch (nfe : NumberFormatException) {

            }

            var wakeWords:String = editSafeWords.text.toString();

            intent.apply {
                intent.putExtra("settings.vibrate", swSafeWordVibrate.isChecked);
                intent.putExtra("settings.trigger", trigger);
                intent.putExtra("settings.emergencyphone", editPhone.text.toString());
                intent.putExtra("settings.enabled", swEnable.isChecked);
                intent.putExtra("settings.recordduration", duration);
                intent.putExtra("settings.wakewords", wakeWords);
                intent.putExtra("settings.testing", swEnableTest.isChecked);
            }
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

        btnSettingsCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED, intent);
            finish();
        }

    }

    private fun initSettings() {
        var devName = intent.getStringExtra("settings.ble");
        initVibrate = intent.getBooleanExtra("settings.vibrate", true);
        initTrigger = intent.getIntExtra("settings.trigger", 120);
        initWakewords = intent.getStringExtra("settings.wakewords");
        initEmergencyPhone = intent.getStringExtra("settings.emergencyphone");
        initEnabled = intent.getBooleanExtra("settings.enabled", true);
        initDuration = intent.getIntExtra("settings.recordduration", 120);
        initTesting  = intent.getBooleanExtra("settings.testing", false);

        tvDeviceName.text = devName;
        editDuration.setText(initDuration.toString());
        editPhone.setText(initEmergencyPhone);
        editSafeWords.setText(initWakewords);
        editTrigger.setText(initTrigger.toString());
        swSafeWordVibrate.isChecked = initVibrate!!;
        swEnable.isChecked = initEnabled!!;
        swEnableTest.isChecked = initTesting!!;
    }
}
