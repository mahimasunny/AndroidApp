package samples.speech.cognitiveservices.microsoft.com

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.Manifest.permission.INTERNET
import android.Manifest.permission.RECORD_AUDIO
import android.support.v4.app.ActivityCompat
import com.microsoft.cognitiveservices.speech.internal.ResultReason.RecognizedSpeech
import android.util.Log
import android.view.View
import android.widget.TextView
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.Future
import kotlin.concurrent.timer


class MainActivity : AppCompatActivity() {

    // Replace below with your own subscription key
    private val speechSubscriptionKey = "019639dd17ab4b6f8643da0164521907"
    // Replace below with your own service region (e.g., "westus").
    private val serviceRegion = "westus"

    private var tmr: Timer? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val INTERNET_REQ_CODE = 5 // unique code for the permission request
        ActivityCompat.requestPermissions(this@MainActivity,
            arrayOf(Manifest.permission.RECORD_AUDIO),1);
        ActivityCompat.requestPermissions(this@MainActivity,
            arrayOf(Manifest.permission.INTERNET), INTERNET_REQ_CODE);

        btnTry.setOnClickListener {
            triggerRecognition();
        };

        triggerRecognition();

    }

    private fun triggerRecognition() {
        tvView.text = "Wait...";
        // ok Albert, start that timer!!!
        tmr = timer(initialDelay = 5000, period = 3600000, action = {
            doRecognition();
        });

    }

    private fun doRecognition() : Unit {
        runOnUiThread {
            tvView.text = "Speaketh ye!!!";
        }
        tmr?.cancel();
        tmr = null;

        tmr = timer(initialDelay = 2000, period = 3600000, action = {
            ShowRecognition();
        });
    }

    private fun ShowRecognition() {
        tmr?.cancel();
        tmr = null;

        var task: Future<SpeechRecognitionResult>? = null;
        var result: SpeechRecognitionResult? = null;

        try {
            val config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion)!!

            val reco = SpeechRecognizer(config)!!

            task = reco.recognizeOnceAsync()!!

            // Note: this will block the UI thread, so eventually, you want to
            //        register for the event (see full samples)
            result = task!!.get()!!


            reco.close();
        } catch (ex: Exception) {
            Log.e("SpeechSDKDemo", "unexpected " + ex.message)
        }

        runOnUiThread {
            if (result!!.getReason() === ResultReason.RecognizedSpeech) {
                tvView.setText(result!!.toString())
            } else {
                tvView.text =
                    "Error recognizing. Did you update the subscription info? ${result!!.toString()}";
            }
        }


    }
}
