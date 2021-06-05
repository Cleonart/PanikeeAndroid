package com.example.panikee.adapters

import android.app.Activity
import android.telephony.SmsManager
import com.example.panikee.model.Contact

class SMSAdapter {

    private var content = ""

    fun sendToAllFriends(ctx : Activity?){
        val friends = FriendsPreferencesAdapter().get(ctx)
        if(friends.size > 0){
            friends.forEach { contact ->
                send(contact)
            }
        }
    }

    fun send(contact : Contact){
        val adapter = SmsManager.getDefault()
        adapter.sendTextMessage(contact.contactNumber,
                                null,
                                content,null,null)
    }

    fun setContent(lat : String, long : String) {
        val googleMapsLink = "https://www.google.com/maps/place/$lat,+$long"
        content = "I'm in distress, please help me. find me at $googleMapsLink"
    }

}