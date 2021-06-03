package com.example.panikee.audioProcessing

import android.content.Context
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Interpreter.Options
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
class TensorflowLite {

    private lateinit var context: Context
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

        /** Assign Context */
        context = ctx

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

    fun predict(meanMFCCValues: Array<FloatArray>){

        /** Step [3] Transform the MFCC 1D Float Buffer into Desired Dimenstion Tensor **/
        inBuffer = TensorBuffer.createDynamic(imageDataType)
        //inBuffer.loadArray(meanMFCCValues, imageShape)
        inpBuffer = inBuffer.buffer
        outputTensorBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)

        /**
         * Step[4] Run the Predictions with Input and Output Buffer Tensor
         * To Get Probability Values
         * **/
        tflite.run(inpBuffer, outputTensorBuffer.buffer)
    }

    fun getOutputAsLabel() : String?{

        var predictedResult: String? = "unknown"
        val labelProcess = MFCCProcessing()

        // Transform the probability predictions into label values
        val associatedAxisLabelFilename = "labels.txt"
        var associatedAxisLabels: List<String?>? = null
        try {
            associatedAxisLabels = FileUtil.loadLabels(context, associatedAxisLabelFilename)
        }
        catch (e: IOException) {
            Log.e("tfliteSupport", "Error reading label file", e)
        }

        /** Tensor Processor for processing the probability values
         *  and sort them based on descending order of probabilites
         * */
        val probabilityProcessor: TensorProcessor = TensorProcessor.Builder()
            .add(NormalizeOp(0.0f, 255.0f)).build()

        if (null != associatedAxisLabels) {

            // Map of labels and their corresponding probability
            val labels = TensorLabel(
                associatedAxisLabels,
                probabilityProcessor.process(outputTensorBuffer)
            )

            /** Create a map to access the result based on label */
            val floatMap: Map<String, Float> =
                labels.mapWithFloatValue

            /**
             * Function to retrieve the Top K Probability Values, in this case 'K' value is 1
             * Retrieved values are storied in 'Recognition' object with label detail
             * */
            val resultPrediction: List<Recognition>? = labelProcess.getTopKProbability(floatMap)

            /** Get the top 1 prediction from retrieved list of top predictions */
            predictedResult = labelProcess.getPredictedValue(resultPrediction)
        }
        return predictedResult
    }

    /** [TENSORFLOW] Get Model Path **/
    private fun getModelPath(): String {
        return "model.tflite"
    }

}