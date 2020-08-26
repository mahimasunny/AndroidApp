package com.ece590.sendsms;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Build;
import android.widget.Button;
import android.support.v4.content.ContextCompat;
import android.widget.EditText;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.widget.Toast;
import android.widget.TextView;


public class SendSMSActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private Button sendSMS;
    private static int BPM = 72;
    private double myLat = 42.5;
    private double myLong = 124.5;
    private EditText message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                Log.e("permission", "Permission already granted.");
            } else {
                requestPermission();
            }
        }
        String sms = "Heartrate is " + BPM+ ", Latitude is " + myLat + " and Longitude is " + myLong;
        //String phoneNum = "+14253500233";
        String phoneNum = "+12698303616";
        if(!TextUtils.isEmpty(sms) && !TextUtils.isEmpty(phoneNum)) {
            message = (EditText)findViewById(R.id.message);
            if(checkPermission()) {

                //Get the default SmsManager//

                SmsManager smsManager = SmsManager.getDefault();

                //Send the SMS//

                smsManager.sendTextMessage(phoneNum, null, sms, null, null);

                message.setText(sms, TextView.BufferType.EDITABLE);

            }else {
                Toast.makeText(SendSMSActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                message.setText("No permissions to send messages", TextView.BufferType.EDITABLE);
            }
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(SendSMSActivity.this, Manifest.permission.SEND_SMS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(SendSMSActivity.this,
                            "Permission accepted", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(SendSMSActivity.this,
                            "Permission denied", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }
}