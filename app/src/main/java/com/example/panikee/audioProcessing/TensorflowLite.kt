package com.example.panikee.audioProcessing

import android.content.Context
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Interpreter.Options
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer

class TensorflowLite {

    var predictedResult: String? = "unknown"

    /** TFLite Interpreter **/
    private lateinit var tflite: Interpreter
    private lateinit var tfliteOptions : Options

    /** Input Tensor */
    private val imageTensorIndex = 0
    private lateinit var imageShape: IntArray
    private lateinit var imageDataType: DataType
    private lateinit var inBuffer: TensorBuffer

    /** Output Tensor */
    private val probabilityTensorIndex = 0
    private lateinit var probabilityShape : IntArray
    private lateinit var probabilityDataType: DataType

    /** Tensorflow Buffer for Input and Output */
    private lateinit var inpBuffer: ByteBuffer
    private lateinit var outputTensorBuffer: TensorBuffer

    fun init(ctx:Context){

        /**
         * Step [1] Load the TFLite Model in MappedByteBuffer
         * Load options also with threads of 2
         * **/
        val tfliteModel: MappedByteBuffer = FileUtil.loadMappedFile(ctx, getModelPath())
        tfliteOptions = Options()
        tfliteOptions.setNumThreads(2)
        tflite = Interpreter(tfliteModel, tfliteOptions)

        /**
         * Step [2] Obtain Input and Output Tensor Size Required by Model
         * for urban sound classification, input tensor should be of 1x40x1x1 shape
         * for Panikee classification, input tensor should be of 1x16x16x1 shape
         * **/
        imageShape = tflite.getInputTensor(imageTensorIndex).shape()
        imageDataType = tflite.getInputTensor(imageTensorIndex).dataType()
        probabilityShape = tflite.getOutputTensor(probabilityTensorIndex).shape()
        probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType()
    }

    fun predict(ctx: Context, meanMFCCValues : FloatArray){

        /** Step [3] Transform the MFCC 1D Float Buffer into Desired Dimenstion Tensor **/
        inBuffer = TensorBuffer.createDynamic(imageDataType)
        inBuffer.loadArray(meanMFCCValues, imageShape)
        inpBuffer = inBuffer.buffer
        outputTensorBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)

        /**
         * Step[4] Run the Predictions with Input and Output Buffer Tensor
         * To Get Probability Values
         * **/
        tflite.run(inpBuffer, outputTensorBuffer.buffer)

    }

    /** [TENSORFLOW] Get Model Path **/
    private fun getModelPath(): String {
        return "model.tflite"
    }

}