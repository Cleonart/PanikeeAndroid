package com.example.panikee.adapters

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentManager
import com.example.panikee.R
import com.example.panikee.ui.emergency.BottomSheetEmergencyFacility
import com.example.panikee.data.vo.EmergencyFacility
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils

class MapMarkerAdapter(ctx: Context, supportFragmentManager: FragmentManager) {

    private val context = ctx
    private val sfm = supportFragmentManager

    private lateinit var emergencyFacilityList: ArrayList<EmergencyFacility>

    private val markerList = mutableListOf<SymbolOptions>()
    private val symbolManagerList = mutableListOf<Symbol>()

    companion object {
        const val ID_ICON_POLICE = "police"
        const val ID_ICON_MEDICAL = "medical"
    }

    private fun addIcon(style: Style, id: Int, icon: String) {
        val btm = BitmapUtils.getDrawableFromRes(context, id)
        if (btm != null) style.addImage(icon, btm)
    }

    /** Set the data for Marker From Retrofit **/
    fun setData(arrayList: ArrayList<EmergencyFacility>) {
        emergencyFacilityList = arrayList

        for (data in arrayList) {
            val symbolOptions = SymbolOptions()
                .withLatLng(LatLng(data.EmergencyFacilityLat!!, data.EmergencyFacilityLong!!))
                .withIconSize(0.08f)

            when (data.EmergencyFacilityCategory) {
                "police" -> symbolOptions.withIconImage(ID_ICON_POLICE)
                "medical" -> symbolOptions.withIconImage(ID_ICON_MEDICAL)
            }

            markerList.add(symbolOptions)
        }
    }

    fun create(mapView: MapView, mapboxMap: MapboxMap, it: Style) {
        /** Add Icon To List */
        addIcon(it, R.drawable.emergencypolice, ID_ICON_POLICE)
        addIcon(it, R.drawable.emergencyambulance, ID_ICON_MEDICAL)

        /** Create Symbol Manager */
        val symbolManager = SymbolManager(mapView, mapboxMap, it)
        symbolManager.iconAllowOverlap = true
        symbolManager.iconIgnorePlacement = true

        /** Iterate Over MarkerList and Create Marker */
        for (data in markerList) {
            val tempSymbol = symbolManager.create(data)
            symbolManagerList.add(tempSymbol)
        }

        /** Click Listener */
        symbolManager.addClickListener(OnSymbolClickListener {
            val fragmentContact = BottomSheetEmergencyFacility()
            fragmentContact.setData(emergencyFacilityList[it.id.toInt()])
            fragmentContact.show(sfm, "ContactBottomSheetDialog")

            Log.d("MapMarkerAdapter", emergencyFacilityList[it.id.toInt()].EmergencyFacilityName.toString())

            return@OnSymbolClickListener false
        })
    }
}