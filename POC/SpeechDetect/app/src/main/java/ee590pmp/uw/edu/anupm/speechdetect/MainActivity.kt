package ee590pmp.uw.edu.anupm.speechdetect

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.media.AudioFormat.CHANNEL_IN_STEREO
import android.os.Environment
import java.nio.file.Files.delete
// import sun.misc.VM.getState
import java.io.*
import java.nio.file.Files.size
import java.nio.file.Files.exists
import android.os.Environment.getExternalStorageDirectory
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.file.Files.delete
import java.nio.file.Files.exists
import android.media.AudioFormat.ENCODING_PCM_8BIT
import android.media.AudioFormat.CHANNEL_IN_MONO
import android.media.MediaRecorder.AudioSource
import android.support.v4.app.ActivityCompat
import java.nio.file.Files.exists




// import sun.font.LayoutPathImpl.getPath






class MainActivity : AppCompatActivity() {

    var arec: AudioRecord? = null;
    private val RECORDER_BPP = 16
    private val AUDIO_RECORDER_FILE_EXT_WAV = ".wav"
    private val AUDIO_RECORDER_FOLDER = "AudioRecorder"
    private val AUDIO_RECORDER_TEMP_FILE = "record_temp.raw"
    private val RECORDER_SAMPLERATE:Int = 16000;
    private val RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    private var bufferSize = 0
    private var recordingThread: Thread? = null
    private var isRecording = false

    var bufferData: IntArray? = null
    var bytesRecorded: Int = 0

    var audioData: ShortArray? = shortArrayOf(0);

    init {
        bufferSize = AudioRecord.getMinBufferSize(
            RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

        audioData = ShortArray(bufferSize) //short array that pcm data is put into.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStartRec.setOnClickListener {

            startRecording();
            Log.d("REC", "START Recording here")
        }

        btnStopRec.setOnClickListener {

            stopRecording();
            Log.d("REC", "STOP Recording here")
        }

        ActivityCompat.requestPermissions(this,
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1);
//        ActivityCompat.requestPermissions(this,
//            permissions, REQUEST_RECORD_AUDIO_PERMISSION);


    }

    fun startRecording() {
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
            RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
//        arec = AudioRecord(AudioSource.MIC, 8000, CHANNEL_IN_MONO, ENCODING_PCM_8BIT, 50 * bufferSize)
        arec = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            RECORDER_SAMPLERATE,
            RECORDER_CHANNELS,
            RECORDER_AUDIO_ENCODING,
            bufferSize);
        if (arec == null) { return; }
        var i:Int = arec!!.getState();

        if (i==1)
            arec!!.startRecording();

        isRecording = true;


        recordingThread = Thread(ThrRun(), "AudioRecorder Thread");

        // recordingThread?.start();
        recordingThread?.start();
    }

    inner class ThrRun() : Runnable {
        override fun run() {
            writeAudioDataToFile();
        }
    }

//        var fileName = "${externalCacheDir.absolutePath}/audiorecordtest.3gp"
//        var recorder: MediaRecorder = MediaRecorder().apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC) //SET THE AUDIO SOURCE
//            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)//SET THE AUDIO OUT FORMAT
//            setOutputFile(fileName)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) //SET THE AUDIO ENCODER FORMAT
//            try {
//                prepare()
//            } catch (e: IOException) {
//                Log.e("MEDIAREC", "prepare() failed")
//            }
//            start()
//            }
//        }


    private fun writeAudioDataToFile() {
        val data = ByteArray(bufferSize)
        val filename = getTempFilename()
        var os: FileOutputStream? = null

        try {
            os = FileOutputStream(filename)
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        var read = 0
        if (null != os) {
            while (isRecording) {
                read = arec!!.read(data, 0, bufferSize)
                if (read > 0) {
                }

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os!!.write(data)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }

            try {
                os!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
//
//    private fun getTempFilename(): String {
//        return "lalalala.wav";
//    }

//    private fun getTempFilename(): String {
//        val filepath = this.filesDir;
////        val file = File(filepath, AUDIO_RECORDER_FOLDER)
////
////        if (!file.exists()) {
////            file.mkdirs()
////        }
//
//        val tempFile = File(filepath, AUDIO_RECORDER_TEMP_FILE)
//
//        if (tempFile.exists())
//            tempFile.delete()
//
//        return filepath.absolutePath + "/" + AUDIO_RECORDER_TEMP_FILE
//    }

    fun getTempFilename() : String {
        var filepath = Environment.getExternalStorageDirectory().getPath();
        var file = File(filepath, AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        var tempFile = File(filepath,AUDIO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    private fun stopRecording() {
        if (null != arec) {
            isRecording = false

            val i = arec!!.getState()
            if (i == 1)
                arec!!.stop()
            arec!!.release()

            arec = null

            recordingThread = null
        }

        copyWaveFile(getTempFilename(), getFilename())
        deleteTempFile()
    }

//    private fun getFilename(): String {
//        val filepath = this.filesDir; // Environment.getDataDirectory().getPath()
////        val file = File(filepath, AUDIO_RECORDER_FOLDER)
////
////        if (!file.exists()) {
////            file.mkdirs()
////        }
//
//        return filepath.getAbsolutePath() + "/" + System.currentTimeMillis() +
//                AUDIO_RECORDER_FILE_EXT_WAV
//    }

    private fun getFilename(): String {
        val filepath = Environment.getExternalStorageDirectory().path
        val file = File(filepath, AUDIO_RECORDER_FOLDER)

        if (!file.exists()) {
            file.mkdirs()
        }

        return file.absolutePath + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV
    }

    private fun deleteTempFile() {
        val file = File(getTempFilename())
        file.delete()
    }

    private fun copyWaveFile(inFilename: String, outFilename: String) {
        var `in`: FileInputStream? = null
        var out: FileOutputStream? = null
        var totalAudioLen: Long = 0
        var totalDataLen = totalAudioLen + 36
        val longSampleRate = RECORDER_SAMPLERATE
        val channels = 1
        val byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8

        val data = ByteArray(bufferSize)

        try {
            var istream:FileInputStream = FileInputStream(inFilename)
            var ostream = FileOutputStream(outFilename)
            totalAudioLen = istream!!.getChannel().size()
            totalDataLen = totalAudioLen + 36

            Log.d("File size", "File Size $totalDataLen")

            WriteWaveFileHeader(
                ostream, totalAudioLen, totalDataLen,
                longSampleRate.toLong(), channels, byteRate
            )

            while (istream!!.read(data) !== -1) {
                ostream.write(data)
            }

            istream!!.close()
            ostream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun WriteWaveFileHeader(
        out: FileOutputStream, totalAudioLen: Long,
        totalDataLen: Long, longSampleRate: Long, channels: Int,
        byteRate: Int
    ) {
        val header = ByteArray(44)

        header[0] = 'R'.toByte()  // RIFF/WAVE header
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        header[12] = 'f'.toByte()  // 'fmt ' chunk
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1  // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (2 * 16 / 8).toByte()  // block align
        header[33] = 0
        header[34] = RECORDER_BPP.toByte()  // bits per sample
        header[35] = 0
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()

        out.write(header, 0, 44)
    }

}
