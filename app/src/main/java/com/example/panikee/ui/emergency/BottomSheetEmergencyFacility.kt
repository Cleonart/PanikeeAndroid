package com.example.panikee.ui.emergency

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.panikee.R
import com.example.panikee.adapters.BottomSheetContactAdapter
import com.example.panikee.adapters.FriendsPreferencesAdapter
import com.example.panikee.data.vo.Contact
import com.example.panikee.data.vo.EmergencyFacility
import com.example.panikee.pages.ContactAdd
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.w3c.dom.Text
import java.util.prefs.Preferences

class BottomSheetEmergencyFacility : BottomSheetDialogFragment(){

    private lateinit var emergencyFacilityImage : ImageView
    private lateinit var emergencyFacilityName : TextView
    private lateinit var emergencyFacilityNumber : TextView
    private lateinit var emergencyFacilityDetail : TextView
    private lateinit var emergencyFacility : EmergencyFacility

    fun setData(emergencyFacilityLocal: EmergencyFacility){
        emergencyFacility = emergencyFacilityLocal
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_emergency_facility, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetStyle)

        emergencyFacilityName = view.findViewById(R.id.emergencyFacilityName)
        emergencyFacilityName.text = emergencyFacility.EmergencyFacilityName

        emergencyFacilityDetail = view.findViewById(R.id.emergencyFacilityDetail)
        emergencyFacilityImage = view.findViewById(R.id.emergencyFacilityImage)
        var drawable : Drawable = resources.getDrawable(R.drawable.siren_icon)

        when(emergencyFacility.EmergencyFacilityCategory){
            "police" -> {
                drawable = resources.getDrawable(R.drawable.emergencypolice)
                emergencyFacilityDetail.text = "Law enforcement service that handles criminal case, you can call this number ${emergencyFacility.EmergencyFacilityNumber} or press the button call for assistance"
            }
            "medical" -> {
                drawable = resources.getDrawable(R.drawable.emergencyambulance)
                emergencyFacilityDetail.text = "Provide Medical Service such as ambulance, You can call this number or, press fhe call service button below."
            }
        }
        emergencyFacilityImage.setImageDrawable(drawable)

    }

    override fun onResume() {
        super.onResume()
    }

    /** Override the current no-rounded corner theme */
    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

}