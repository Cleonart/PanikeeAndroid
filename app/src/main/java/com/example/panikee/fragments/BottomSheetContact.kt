package com.example.panikee.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.panikee.R
import com.example.panikee.adapters.BottomSheetContactAdapter
import com.example.panikee.adapters.FriendsPreferencesAdapter
import com.example.panikee.model.Contact
import com.example.panikee.pages.ContactAdd
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.util.prefs.Preferences

class BottomSheetContact : BottomSheetDialogFragment(){

    /** Adapter and Layout */
    private lateinit var adapter : BottomSheetContactAdapter
    private var layoutManager : RecyclerView.LayoutManager? = null

    /** View Element */
    private lateinit var contactList : RecyclerView
    private lateinit var contactZero : LinearLayout
    private lateinit var contactAdd : Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetStyle)

        /** Set Adapter **/
        adapter = BottomSheetContactAdapter(activity)
        adapter.setContentData(FriendsPreferencesAdapter().get(activity))
        layoutManager = LinearLayoutManager(activity)

        /** View Element Initializing **/
        contactZero = view.findViewById(R.id.contact_zero)
        contactList = view.findViewById(R.id.contact_list)
        contactList.layoutManager = layoutManager
        contactList.adapter = adapter

        /** Button to Intent ContactAdd */
        contactAdd  = view.findViewById(R.id.fragment_contact_btn_add)
        contactAdd.setOnClickListener {
            val intent = Intent(activity, ContactAdd::class.java)
            startActivity(intent)
        }

        setVisibility()
    }

    override fun onResume() {
        super.onResume()
        adapter.setContentData(FriendsPreferencesAdapter().get(activity))
        adapter.notifyDataSetChanged()
        setVisibility()
    }

    /** Override the current no-rounded corner theme */
    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    /**
     * Show the Recycler View is data exist
     * and hide recycler view when there's no data
     */
    private fun setVisibility(){
        if(adapter.itemCount > 0) {
            contactZero.visibility = View.GONE
            contactList.visibility = View.VISIBLE
        }
        else{
            contactZero.visibility = View.VISIBLE
            contactList.visibility = View.GONE
        }
    }

}