package com.example.panikee.adapters

import android.app.Activity
import android.preference.PreferenceManager
import com.example.panikee.model.Contact
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser

class FriendsPreferencesAdapter {

    fun update(ctx: Activity?, keyName : String, data: Any){
        val preferences = PreferenceManager.getDefaultSharedPreferences(ctx).edit()
        preferences.putString(keyName, Gson().toJson(data)).apply()
    }

    fun get(ctx : Activity?) : MutableList<Contact>{
        val preferences = PreferenceManager.getDefaultSharedPreferences(ctx)
        val jsonString = preferences.getString("friends", null)
        val mutableList : MutableList<Contact> = mutableListOf()
        val jsonArray : JsonArray = JsonParser().parse(jsonString).asJsonArray
        for(jsonElement : JsonElement in jsonArray){
            val cyx : Contact = Gson().fromJson(jsonElement, Contact::class.java)
            mutableList.add(cyx)
        }
        return mutableList
    }

}