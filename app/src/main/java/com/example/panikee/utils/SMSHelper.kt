package com.example.panikee.utils

import android.app.Activity
import android.telephony.SmsManager
import com.example.panikee.adapters.FriendsPreferencesAdapter
import com.example.panikee.data.vo.Contact

data class SMSHelper(
    val content: String?
) {
    fun sendToAllFriends(ctx: Activity?) {
        val friends = FriendsPreferencesAdapter().get(ctx)
        if (friends.size > 0) {
            friends.forEach { contact ->
                send(contact)
            }
        }
    }

    fun send(contact: Contact) {
        val adapter = SmsManager.getDefault()
        adapter.sendTextMessage(contact.contactNumber, null, content, null, null)
    }
}