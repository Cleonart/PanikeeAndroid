package com.example.panikee.audioProcessing

import android.content.ContentValues
import android.os.Environment
import android.util.Log
import com.jlibrosa.audio.JLibrosa
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class MFCCProcessing {

    private lateinit var mfccValues : Array<FloatArray>
    private lateinit var meanMFCCValues: FloatArray

    fun process(){

        /**
         * Step 1 Create the options and Jlibrosa Instance
         * from Jlibros() class
         */
        val audioFilePath = Environment.getExternalStorageDirectory().absolutePath + "/audioData/dog.wav"
        val defaultSampleRate = -1    //-1 value implies the method to use default sample rate
        val defaultAudioDuration = -1 //-1 value implies the method to process complete audio duration
        val jLibrosa = JLibrosa()

        /**
         * To Read The Magnitude Values of Audio Files
         * equivalent to librosa.load('../audioFiles/1995-1826-0003.wav', sr=None) function
         */
        val audioFeaturesValues = jLibrosa.loadAndRead(audioFilePath, defaultSampleRate, defaultAudioDuration)

        /**
         * To read the no of frames present in audio file
         * To read sample rate of audio file
         * To read number of channels in audio file
         */
        val nNoOfFrames = jLibrosa.noOfFrames
        val sampleRate = jLibrosa.sampleRate
        val noOfChannels = jLibrosa.noOfChannels
        val buffer = Array(noOfChannels) { DoubleArray(nNoOfFrames) }

        /**
         * Process the MFCC
         * **/
        mfccValues = jLibrosa.generateMFCCFeatures(audioFeaturesValues, sampleRate, 40)
        meanMFCCValues = jLibrosa.generateMeanMFCCFeatures(mfccValues, mfccValues.size, mfccValues[0].size)
        Log.d("MFCC_PROCESSING","Size of MFCC Feature Values: (" + mfccValues.size + " , " + mfccValues[0].size + " )")
    }

    fun getMFCCValues() : Array<FloatArray>{
        return mfccValues
    }

    fun getMeanMFCCValues() : FloatArray{
        return meanMFCCValues
    }

    /** Gets the predicted value and map it on Recognition Class */
    fun getPredictedValue(predictedList:List<Recognition>?): String?{
        val top1PredictedValue : Recognition? = predictedList?.get(0)
        return top1PredictedValue?.getTitle()
    }

    /** Gets the Top-K results.  */
    fun getTopKProbability(labelProb: Map<String, Float>): List<Recognition>? {
        // Find the best classifications.
        val MAX_RESULTS: Int = 1
        val pq: PriorityQueue<Recognition> = PriorityQueue(MAX_RESULTS,
            Comparator<Recognition> { lhs, rhs ->
                // Intentionally reversed to put high confidence at the head of the queue.
                java.lang.Float.compare(rhs.getConfidence(), lhs.getConfidence()) })

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
}