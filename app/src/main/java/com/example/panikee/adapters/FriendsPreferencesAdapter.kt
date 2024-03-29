package com.example.panikee.adapters

import android.app.Activity
import android.preference.PreferenceManager
import com.example.panikee.data.vo.Contact
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser

class FriendsPreferencesAdapter {

    /** Settings for passing update to SharedPreferences */
    fun update(ctx: Activity?, keyName : String, data: Any){
        val preferences = PreferenceManager.getDefaultSharedPreferences(ctx).edit()
        preferences.putString(keyName, Gson().toJson(data)).apply()
    }

    /** Getting List of Friends from SharedPreferences */
    fun get(ctx : Activity?) : MutableList<Contact>{
        val preferences = PreferenceManager.getDefaultSharedPreferences(ctx)
        val jsonString = preferences.getString("friends", null)
        val mutableList : MutableList<Contact> = mutableListOf()
        if(jsonString != null){
            val jsonArray : JsonArray = JsonParser().parse(jsonString).asJsonArray
            for(jsonElement : JsonElement in jsonArray){
                val cyx : Contact = Gson().fromJson(jsonElement, Contact::class.java)
                mutableList.add(cyx)
            }
            return mutableList
        }
        return mutableListOf()
    }

}