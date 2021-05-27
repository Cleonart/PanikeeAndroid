package com.example.panikee

import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.panikee.audioProcessing.TensorflowLite
import com.jlibrosa.audio.JLibrosa
import com.example.panikee.audioProcessing.Recognition
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


class MainRecord : AppCompatActivity(){

    private lateinit var btnPlay:Button
    private lateinit var btnRecord:Button
    private lateinit var btnStop:Button

    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var audioRecorder: AudioRecord
    private var state:Boolean = false

    private var filename:String = "recorder.mp3"
    private lateinit var output:String

    private lateinit var tfliteV2 : TensorflowLite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        //output = Environment.getExternalStorageDirectory().absolutePath + "/" + filename
        initInterferenceV2()
        jlibrosaTest()

        /*
        btnRecord = findViewById(R.id.btnRecord)
        btnRecord.setOnClickListener {
            startRecording()
        }

        btnStop = findViewById(R.id.btnStop)
        btnStop.setOnClickListener {
            stopRecording()
        }
         */
    }

    private fun initInterferenceV2(){
        tfliteV2 = TensorflowLite()
        tfliteV2.init(this)
    }

    private fun jlibrosaTest(){
        val audioFilePath = Environment.getExternalStorageDirectory().absolutePath + "/audioData/children.wav"
        val defaultSampleRate = -1    //-1 value implies the method to use default sample rate
        val defaultAudioDuration = -1 //-1 value implies the method to process complete audio duration
        val jLibrosa = JLibrosa()

        /* To read the magnitude values of audio files - equivalent to librosa.load('../audioFiles/1995-1826-0003.wav', sr=None) function */
        val audioFeaturesValues = jLibrosa.loadAndRead(audioFilePath, defaultSampleRate, defaultAudioDuration)

        /* To read the no of frames present in audio file*/
        /* To read sample rate of audio file */
        /* To read number of channels in audio file */
        val nNoOfFrames = jLibrosa.noOfFrames
        val sampleRate = jLibrosa.sampleRate
        val noOfChannels = jLibrosa.noOfChannels
        val buffer = Array(noOfChannels) { DoubleArray(nNoOfFrames) }

        val mfccValues = jLibrosa.generateMFCCFeatures(audioFeaturesValues, sampleRate, 40)
        val meanMFCCValues = jLibrosa.generateMeanMFCCFeatures(mfccValues, mfccValues.size, mfccValues[0].size)

        Log.d("JLIBROSATEST",".......")
        Log.d("JLIBROSATEST","Size of MFCC Feature Values: (" + mfccValues.size + " , " + mfccValues[0].size + " )")

        tfliteV2.predict(meanMFCCValues)
        val output = tfliteV2.getOutputAsLabel()
        Log.d("JLIBROSATEST", output.toString())
    }

    private fun startRecording(){
        if (audioPermission()) {
            try {
                mediaRecorder.prepare()
                mediaRecorder.start()
                state = true
                Toast.makeText(this, "Recording Start", Toast.LENGTH_SHORT).show()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private fun stopRecording(){
        if(state){
            mediaRecorder.stop()
            mediaRecorder.release()
            state = false
            Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun audioPermission(): Boolean {
        if (checkPermissionFromDevice()){
            //audioRecorder = AudioRecord(0)
            mediaRecorder = MediaRecorder()
            mediaRecorder.reset()
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setOutputFile(output)
            return true
        }
        val permissions = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO)
            ActivityCompat.requestPermissions(this, permissions, 0)
        return false
    }
    private fun checkPermissionFromDevice(): Boolean {
        val writeExternalStorageResult:Int = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val recordAudioResult = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
        return  writeExternalStorageResult == PackageManager.PERMISSION_GRANTED &&
                recordAudioResult == PackageManager.PERMISSION_GRANTED
    }

    fun getModelPath(): String {
        return "model.tflite"
    }

}