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


class BottomSheet_Contact : BottomSheetDialogFragment(){

    private lateinit var adapter : BottomSheetContactAdapter
    private var layoutManager : RecyclerView.LayoutManager? = null
    private lateinit var contact_list : RecyclerView
    private lateinit var contact_zero : LinearLayout
    private lateinit var contact_add : Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetStyle)
        adapter = BottomSheetContactAdapter(activity)
        adapter.setContentData(FriendsPreferencesAdapter().get(activity))
        contact_list = view.findViewById(R.id.contact_list)
        contact_zero = view.findViewById(R.id.contact_zero)
        layoutManager = LinearLayoutManager(activity)
        contact_list.layoutManager = layoutManager
        contact_list.adapter = adapter

        /** To [ContactAdd] */
        contact_add  = view.findViewById(R.id.fragment_contact_btn_add)
        contact_add.setOnClickListener {
            val intent = Intent(activity, ContactAdd::class.java)
            startActivity(intent)
        }

        if(adapter.itemCount > 0) {
            contact_zero.visibility = View.GONE
            contact_list.visibility = View.VISIBLE
        }
        else{
            contact_zero.visibility = View.VISIBLE
            contact_list.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.setContentData(FriendsPreferencesAdapter().get(activity))
        adapter.notifyDataSetChanged()
        if(adapter.itemCount > 0) {
            contact_zero.visibility = View.GONE
            contact_list.visibility = View.VISIBLE
        }
        else{
            contact_zero.visibility = View.VISIBLE
            contact_list.visibility = View.GONE
        }
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

}