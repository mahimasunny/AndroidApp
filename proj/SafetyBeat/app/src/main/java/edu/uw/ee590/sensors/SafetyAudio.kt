package edu.uw.ee590.sensors

import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import java.io.File
import java.io.IOException
import java.lang.Integer.min
import java.util.*
import java.util.concurrent.Future
import kotlin.concurrent.timer


class SafetyAudioRecord(val fileName:String, val timeStamp:Long,
                        val safeWords : List<String>,
                        val handler: SafetyAudioRecord.AudioRecordFinishCallback) {

    private val speechSubscriptionKey = "019639dd17ab4b6f8643da0164521907";
    private val serviceRegion = "westus";
    private val recordingFolder = "SafetyBeat";
    private var recognition : String? = null;
    private var fullRecorder : MediaRecorder? = null;

    interface AudioRecordFinishCallback {
        fun OnRecordFinish();
        fun OnSafewordDetection(promotedToCritical:Boolean);
    }

    var tm: Timer? = null;
    var recordingInitialSample: Boolean = true;
    var elapsedRecording: Int = 0;
    var totalRecordingLength: Int = 0;

    init {

    }

    // overview: Each recording consists of a 20 sec clip to detect
    // safe words, and a longer full recording
    fun performTimedRecord(numSecondsToRecord: Int) {
        recordingInitialSample = true;
        elapsedRecording = 0;
        recognition = null;

        totalRecordingLength = numSecondsToRecord;
        if (totalRecordingLength > 300) {
            totalRecordingLength = 300; // cap it to 5 minutes
        }

        // through safe word.
        // start timer for stopping the recordings
        tm =
            // initial 20 seconds is for safe word detection (initialDelay)
            // rest will be numSecondsToRecord length clip
            timer(initialDelay = 500.toLong(),
                period = 30000L) {
                if (recordingInitialSample) {
                    startInitialRecording();
                    // first 15 second sample has been recorded
                    val promoted: Boolean = detectPromotion();
                    recordingInitialSample = false;
                    handler.OnSafewordDetection(promoted);
                    startRecord(fileName); // now start the full recording
                    elapsedRecording = 10;
                } else {
                    // not initial sample anymore
                    elapsedRecording += 30;
                    Log.d("REC1","Recorded $elapsedRecording seconds of audio.");
                    if (elapsedRecording >= totalRecordingLength) {
                        stopRecord(fileName);
                    }
                }
        };
    }

    private fun startInitialRecording() {
        Log.d("REC1", "Starting Initial Recording");
        var task: Future<SpeechRecognitionResult>? = null;
        var result: SpeechRecognitionResult? = null;

        try {
            val config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion)!!

            val reco = SpeechRecognizer(config)!!

            task = reco.recognizeOnceAsync()!!

            // Note: this will block the UI thread, so eventually, you want to
            //        register for the event (see full samples)
            result = task!!.get()!!
            recognition = result.text;

            reco.close();
        } catch (ex: Exception) {
            Log.e("SpeechSDKDemo", "unexpected " + ex.message)
        }

    }

    private fun detectPromotion(): Boolean {
        Log.d("REC1", "Detecting Safe Words!");
        // stop recording the original stream.
        // once we stop the record, chunk up the data to see if we can do
        // speech detection. If that succeeds, we promote event to Critical
        // from Elevated

        // TODO
        if (recognition != null ) {
            Log.d("REC1", "Detected Speech: $recognition");
        }
        else {
            Log.d("REC1", "Detected Speech: null");
            return false;
        }

        for (str in safeWords) {
            val curPhrase = str.toLowerCase();
            if (recognition!!.toLowerCase().contains(curPhrase)) {
                return true;
            }
        }

        return false;
    }


    fun startRecord(fileName: String) {
        Log.d("REC1", "starting recording: $fileName in folder $recordingFolder");
        var filePath = resolveFileName();
        fullRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC);
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            setOutputFile(filePath);
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                prepare()
            } catch (e: IOException) {
                Log.e("REC2", "prepare() failed") }
            start();
        }

    }

    fun stopRecord(fileName: String) {
        Log.d("REC1", "stopping recording: $fileName");
        try {
            fullRecorder?.stop();
        } finally {
            tm?.cancel();
            tm = null;
        }

        handler.OnRecordFinish();
    }

    fun resolveFileName() : String {
        var filepath = Environment.getExternalStorageDirectory().getPath();
        var dir = File(filepath, recordingFolder);

        if(!dir.exists()){
            dir.mkdirs();
        }

        return dir.absolutePath + "/" + fileName;
    }
}