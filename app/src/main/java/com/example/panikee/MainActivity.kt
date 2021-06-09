package com.example.panikee

// import all necessary plugins
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.panikee.adapters.MapMarkerAdapter
import com.example.panikee.adapters.PermissionsAdapter
import com.example.panikee.adapters.RetrofitAdapter
import com.example.panikee.adapters.SMSAdapter
import com.example.panikee.fragments.BottomSheetContact
import com.example.panikee.fragments.BottomSheetEmergencyFacility
import com.example.panikee.fragments.BottomSheetPassword
import com.example.panikee.model.EmergencyFacility
import com.example.panikee.pages.ContactAdd
import com.example.panikee.pages.Login
import com.example.panikee.pages.Register
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
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import kotlinx.coroutines.delay
import org.tensorflow.lite.examples.soundclassifier.SoundClassifier
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener{

    private lateinit var contactButton : ImageView
    private lateinit var panicButton : ImageView

    /** Panic Button Set */
    private val clickUntilTrigger = 8
    private val timeInterval : Long = 2500
    private var numberOfClick = 0
    private var previousClickTimestamp : Long = 0

    // Start Siren
    lateinit var mediaPlayer: MediaPlayer

    /** Map Settings */
    private val defaultTimeInterval:Long = 1000L
    private val defaultMaxWaitTime: Long = defaultTimeInterval * 5
    lateinit var mapboxMap: MapboxMap
    private lateinit var mapView: MapView
    lateinit var symbol: Symbol
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var tfclassifier : SoundClassifier

    var positionLatitude = "LATITUDE"
    var positionLongtitude = "LONGTITUDE"

    /** Listening to Location Updates */
    private val callback: MainActivityLocationCallback = MainActivityLocationCallback(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val loggedIn = preferences.getString("logged_in", null)
        if (loggedIn == null){
            val intent = Intent(this, Login::class.java)
            finish()
            startActivity(intent)
        }

        /** Mapbox Instances */
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.main)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        /** Init Media Player */
        mediaPlayer = MediaPlayer.create(this, R.raw.siren)
        mediaPlayer.isLooping = true

        /** Initializing View Element */
        buttonInitializing()

        /** Initializing Classifier */
        classifierInitializing()
    }

    /** Initialize Button With The Click Listener */
    private fun buttonInitializing(){

        /** Contact Button */
        contactButton = findViewById(R.id.bell)
        contactButton.setOnClickListener {
            contactButton.animate().apply {
                duration = 100
                scaleX(1.1f)
                scaleY(1.1f)
            }.withEndAction {
                contactButton.animate().apply {
                    duration=100
                    scaleX(1.0f)
                    scaleY(1.0f)
                }
                val fragmentContact = BottomSheetContact()
                fragmentContact.show(supportFragmentManager, "ContactBottomSheetDialog")
            }
        }

        /** Panic Button */
        panicButton = findViewById(R.id.siren)
        panicButton.setOnClickListener {
            panicButton.animate().apply {
                duration = 100
                scaleX(1.1f)
                scaleY(1.1f)
            }.withEndAction {
                panicButton.animate().apply {
                    duration=100
                    scaleX(1.0f)
                    scaleY(1.0f)
                }
                checkEmergencyStatus()
            }
        }
    }

    private fun checkEmergencyStatus(){
        val timeNow = System.currentTimeMillis()

        /** Number of click add when timeNow - previousClickTimestamp */
        if ((timeNow - previousClickTimestamp) < timeInterval){ numberOfClick++ }
        else{
            numberOfClick = 0
            Log.d("tes", "Resetted")
        }

        if (numberOfClick > clickUntilTrigger){

            /** Set Number of click back to 0 */
            numberOfClick = 0

            /** Audio Player and Manager */
            var volumeLevelCounter = 0
            val audioManager : AudioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
            while (volumeLevelCounter < 100){
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
                volumeLevelCounter++
            }
            mediaPlayer.start()

            /** SMS Adapter */
            val sms = SMSAdapter()
            sms.setContent(positionLatitude, positionLongtitude)
            sms.sendToAllFriends(this)

            /** Open Password Input */
            val fragmentContact = BottomSheetPassword(this, mediaPlayer)
            fragmentContact.isCancelable = false
            fragmentContact.show(supportFragmentManager, "PasswordBottomSheetDialog")
        }
        previousClickTimestamp = timeNow
    }

    /** Initialize Audio Classifer */
    private fun classifierInitializing(){
        if(PermissionsAdapter().check(this, this)){
            tfclassifier = SoundClassifier(this,
                SoundClassifier.Options()).also {
                it.lifecycleOwner = this
            }
            tfclassifier.start()
            val labelName = tfclassifier.labelList[1]
            tfclassifier.probabilities.observe(this) { resultMap ->
                val probability = resultMap[labelName]
                val probs = probability?.times(100)?.toInt()

                if (probs?.toInt()!! > 90 && !mediaPlayer.isPlaying){
                    Log.d("Classifier", "NO")
                    checkEmergencyStatus()
                }

            }
        }
    }

    override fun onMapReady(mbx: MapboxMap) {
        mapboxMap = mbx
        val context = this
        RetrofitAdapter.instance.getEmergencyFacility().enqueue(object : Callback<ArrayList<EmergencyFacility>>{
            override fun onResponse(
                call: Call<ArrayList<EmergencyFacility>>,
                response: Response<ArrayList<EmergencyFacility>>
            ) {
                val mapMarkerAdapter = MapMarkerAdapter(context, supportFragmentManager)
                mapMarkerAdapter.setData(response.body()!!)
                mbx.setStyle(Style.LIGHT) {
                    enableLocationComponent(it)
                    mapMarkerAdapter.create(mapView, mapboxMap, it)
                }
            }
            override fun onFailure(call: Call<ArrayList<EmergencyFacility>>, t: Throwable) {
                Log.d("tes","failed")
                Log.d("tes", t.message.toString())
            }
        })

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

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {}

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

    override fun onResume() {
        super.onResume()
    }
}

/** Location Callback */
class MainActivityLocationCallback(mainActivity: MainActivity):LocationEngineCallback<LocationEngineResult> {
    private var activityWeakReference: WeakReference<MainActivity>? = WeakReference(mainActivity)
    override fun onSuccess(result: LocationEngineResult) {
        val activity: MainActivity? = activityWeakReference?.get()
        if(activity != null){
            val location: Location = result.lastLocation ?: return
            val stringData = "Lat : " + location.latitude.toString() +
                             " Long : " + location.longitude.toString()

            activity.positionLatitude = location.latitude.toString()
            activity.positionLongtitude = location.longitude.toString()
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
