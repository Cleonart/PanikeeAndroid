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
import com.jlibrosa.audio.JLibrosa
import com.ml.quaterion.noiseClassification.Recognition
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        //output = Environment.getExternalStorageDirectory().absolutePath + "/" + filename

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

    private fun initializingInterference(meanMFCCValues : FloatArray) : String?{

        var predictedResult: String? = "unknown"

        /**
         * Step [1] Load the TFLite Model in MappedByteBuffer
         * Load options also with threads of 2
         * **/
        val tfliteModel: MappedByteBuffer = FileUtil.loadMappedFile(this, getModelPath())
        val tflite: Interpreter
        val tfliteOptions = Interpreter.Options()
        tfliteOptions.setNumThreads(2)
        tflite = Interpreter(tfliteModel, tfliteOptions)

        /**
         * Step [2] Obtain Input and Output Tensor Size Required by Model
         * for urban sound classification, input tensor should be of 1x40x1x1 shape
         * for Panikee classification, input tensor should be of 1x16x16x1 shape
         * **/
        val imageTensorIndex = 0  // Input Tensor
        val imageShape = tflite.getInputTensor(imageTensorIndex).shape()
        val imageDataType: DataType = tflite.getInputTensor(imageTensorIndex).dataType()
        val probabilityTensorIndex = 0  // Output Tensor
        val probabilityShape = tflite.getOutputTensor(probabilityTensorIndex).shape()
        val probabilityDataType: DataType = tflite.getOutputTensor(probabilityTensorIndex).dataType()

        /** Step [3] Transform the MFCC 1D Float Buffer into Desired Dimenstion Tensor **/
        val inBuffer: TensorBuffer = TensorBuffer.createDynamic(imageDataType)
        inBuffer.loadArray(meanMFCCValues, imageShape)
        val inpBuffer: ByteBuffer = inBuffer.getBuffer()
        val outputTensorBuffer: TensorBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)

        /**
         * Step[4] Run the Predictions with Input and Output Buffer Tensor
         * To Get Probability Values
         * **/
        tflite.run(inpBuffer, outputTensorBuffer.getBuffer())

        //Code to transform the probability predictions into label values
        val ASSOCIATED_AXIS_LABELS = "labels.txt"
        var associatedAxisLabels: List<String?>? = null
        try {
            associatedAxisLabels = FileUtil.loadLabels(this, ASSOCIATED_AXIS_LABELS)
        } catch (e: IOException) {
            Log.e("tfliteSupport", "Error reading label file", e)
        }

        //Tensor processor for processing the probability values and to sort them based on the descending order of probabilities
        val probabilityProcessor: TensorProcessor = TensorProcessor.Builder()
            .add(NormalizeOp(0.0f, 255.0f)).build()
        if (null != associatedAxisLabels) {
            // Map of labels and their corresponding probability
            val labels = TensorLabel(
                associatedAxisLabels,
                probabilityProcessor.process(outputTensorBuffer)
            )

            // Create a map to access the result based on label
            val floatMap: Map<String, Float> =
                labels.getMapWithFloatValue()

            //function to retrieve the top K probability values, in this case 'k' value is 1.
            //retrieved values are storied in 'Recognition' object with label details.
            val resultPrediction: List<Recognition>? = getTopKProbability(floatMap);

            //get the top 1 prediction from the retrieved list of top predictions
            predictedResult = getPredictedValue(resultPrediction)
        }
        return predictedResult
    }

    fun getPredictedValue(predictedList:List<Recognition>?): String?{
        val top1PredictedValue : Recognition? = predictedList?.get(0)
        return top1PredictedValue?.getTitle()
    }

    /** Gets the top-k results.  */
    protected fun getTopKProbability(labelProb: Map<String, Float>): List<Recognition>? {
        // Find the best classifications.
        val MAX_RESULTS: Int = 1
        val pq: PriorityQueue<Recognition> = PriorityQueue(
            MAX_RESULTS,
            Comparator<Recognition> { lhs, rhs -> // Intentionally reversed to put high confidence at the head of the queue.
                java.lang.Float.compare(rhs.getConfidence(), lhs.getConfidence())
            })
        for (entry in labelProb.entries) {
            pq.add(Recognition("" + entry.key, entry.key, entry.value))
        }
        val recognitions: ArrayList<Recognition> = ArrayList()
        val recognitionsSize: Int = Math.min(pq.size, MAX_RESULTS)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }
        return recognitions
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
        Log.d("JLIBROSATEST", initializingInterference(meanMFCCValues).toString())
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