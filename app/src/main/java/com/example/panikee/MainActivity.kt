package com.example.panikee

// import all necessary plugins

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.panikee.audioProcessing.audioPermission
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import org.tensorflow.lite.examples.soundclassifier.SoundClassifier
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener{

    private lateinit var contactButton : ImageView

    private val defaultTimeInterval:Long = 1000L
    private val defaultMaxWaitTime: Long = defaultTimeInterval * 5

    lateinit var mapboxMap: MapboxMap
    private lateinit var mapView: MapView
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var tfclassifier : SoundClassifier

    // Variables needed to listen to location updates
    private val callback: MainActivityLocationCallback = MainActivityLocationCallback(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox token is configured here
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        // Set content view to where the maps is available
        setContentView(R.layout.main)

        contactButton = findViewById(R.id.bell)
        contactButton.setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            startActivity(intent)
        }

        // Audio for Sound Classifier
        if( audioPermission().checkAudioPermission(this, this)){
            tfclassifier = SoundClassifier(this, SoundClassifier.Options()).also {
                it.lifecycleOwner = this
            }
            tfclassifier.start()
        }

        val labelName = tfclassifier.labelList[1]
        tfclassifier.probabilities.observe(this) { resultMap ->
            val probability = resultMap[labelName]
            Log.d("Classifier", probability.toString())
        }

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(mbx: MapboxMap) {
        mapboxMap = mbx
        mbx.setStyle(Style.LIGHT) {
            enableLocationComponent(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style?){

        // check if permission is enabled
        if (PermissionsManager.areLocationPermissionsGranted(this )){

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.mapbox_blue))
                .build()

            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(this, loadedMapStyle!!)
                    .locationComponentOptions(customLocationComponentOptions)
                    .build()

            // Get an instance of the LocationComponent and then adjust its settings
            mapboxMap.locationComponent.apply {
                // 1. Activate the LocationComponent with options
                // 2. Enable to make the LocationComponent visible
                // 3. Set the LocationComponent's camera mode
                // 4. Set the LocationComponent's render mode
                activateLocationComponent(locationComponentActivationOptions)
                isLocationComponentEnabled = true
                cameraMode = CameraMode.TRACKING
                renderMode = RenderMode.COMPASS
            }
            initLocationEngine()
            return
        }
        // if permission disabled
        permissionsManager = PermissionsManager(this)
        permissionsManager.requestLocationPermissions(this)
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        val request = LocationEngineRequest.Builder(defaultTimeInterval)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(defaultMaxWaitTime).build()
        locationEngine.requestLocationUpdates(request, callback, mainLooper)
        locationEngine.getLastLocation(callback)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        TODO("Not yet implemented")
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted){
            enableLocationComponent(mapboxMap.style)
            return
        }
        Toast.makeText(this, "Permission Disabled", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}

// Location Callback
class MainActivityLocationCallback(mainActivity: MainActivity):LocationEngineCallback<LocationEngineResult> {
    private var activityWeakReference: WeakReference<MainActivity>? = WeakReference(mainActivity)
    override fun onSuccess(result: LocationEngineResult) {
        val activity: MainActivity? = activityWeakReference?.get()

        if(activity != null){
            val location: Location = result.lastLocation ?: return
            val stringData = "Lat : " + location.latitude.toString() +
                             " Long : " + location.longitude.toString()

            // Show toast on screen
            //Toast.makeText(activity,  stringData, Toast.LENGTH_SHORT).show()

            if(result.lastLocation != null){
                activity.mapboxMap.locationComponent.forceLocationUpdate(result.lastLocation)
            }
        }
    }
    override fun onFailure(exception: Exception) {
        Log.d("LocationChangeActivity", "FAILED")
        Log.d("LocationChangeActivity", exception.localizedMessage)
        val activity = activityWeakReference!!.get()
        if (activity != null) {
            Toast.makeText(
                activity, exception.localizedMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
