package com.example.panikee.adapters

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.panikee.R
import com.example.panikee.fragments.BottomSheetEmergencyFacility
import com.example.panikee.model.EmergencyFacility
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils

class MapMarkerAdapter(ctx : Context, supportFragmentManager: FragmentManager){

    private val context = ctx
    private val sfm = supportFragmentManager

    private lateinit var emergencyFacilityList : ArrayList<EmergencyFacility>
    private val markerList = mutableListOf<SymbolOptions>()
    private val symbolManagerList = mutableListOf<Symbol>()

    /** Police Initializer */
    private val ID_ICON_POLICE  = "police"
    private fun addPolice(style: Style){
        val btm = BitmapUtils.getDrawableFromRes(context, R.drawable.emergencypolice)
        if (btm != null) { style.addImage(ID_ICON_POLICE, btm) }
    }

    private val ID_ICON_MEDICAL  = "medical"
    private fun addMedical(style: Style){
        val btm = BitmapUtils.getDrawableFromRes(context, R.drawable.emergencyambulance)
        if (btm != null) { style.addImage(ID_ICON_MEDICAL, btm) }
    }

    /** Set the data for Marker From Retrofit **/
    fun setData(arrayList: ArrayList<EmergencyFacility>){
        emergencyFacilityList = arrayList
        for(data in arrayList){
            val symbolOptions = SymbolOptions()
                .withLatLng(LatLng(data.EmergencyFacilityLat!!, data.EmergencyFacilityLong!!))
                .withIconSize(0.08f)
            when(data.EmergencyFacilityCategory){
                "police" -> { symbolOptions.withIconImage(ID_ICON_POLICE) }
                "medical" -> { symbolOptions.withIconImage(ID_ICON_MEDICAL)}
            }
            markerList.add(symbolOptions)
        }
    }

    fun create(mapView: MapView, mapboxMap:MapboxMap, it: Style){
        /** Add Icon To List */
        addMedical(it)
        addPolice(it)

        /** Create Symbol Manager */
        val symbolManager = SymbolManager(mapView, mapboxMap, it)
        symbolManager.iconAllowOverlap = true
        symbolManager.iconIgnorePlacement = true

        /** Iterate Over MarkerList and Create Marker */
        for (data in markerList){
            val tempSymbol = symbolManager.create(data)
            symbolManagerList.add(tempSymbol)
        }

        /** Click Listener */
        symbolManager.addClickListener(OnSymbolClickListener {
            val fragmentContact = BottomSheetEmergencyFacility()
            fragmentContact.setData(emergencyFacilityList[it.id.toInt()])
            fragmentContact.show(sfm, "ContactBottomSheetDialog")
            Log.d("tes", emergencyFacilityList[it.id.toInt()].EmergencyFacilityName.toString())
            return@OnSymbolClickListener false
        })
    }
}