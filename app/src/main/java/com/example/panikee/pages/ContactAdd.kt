package com.example.panikee.pages

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.panikee.R
import com.example.panikee.adapters.FriendsPreferencesAdapter
import com.example.panikee.model.Contact
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser

class ContactAdd : AppCompatActivity(){

    private lateinit var contactName : EditText
    private lateinit var contactNumber : EditText
    private lateinit var contactSubmit : Button
    private lateinit var contactCancel : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** View Element Initializing */
        setContentView(R.layout.activity_contact_add)
        contactName = findViewById(R.id.contact_add_contact_name)
        contactNumber = findViewById(R.id.contact_add_contact_number)
        contactSubmit = findViewById(R.id.contact_add_submit)
        contactCancel = findViewById(R.id.contact_add_cancel)

        /** Get List of Frinds from SharedPreferences */
        val listFriends = FriendsPreferencesAdapter().get(this)

        contactSubmit.setOnClickListener {

            /** Form Validation */
            val contactNameValue = contactName.text.toString()
            val contactNumberValue = contactNumber.text.toString()
            if(contactName.length() == 0) { contactName.error = "Contact name need to be filled" }
            if(contactNumber.length() == 0){ contactNumber.error = "Contact number need to be filled" }

            /** Submit New Friend to SharedPreferences */
            if (contactName.length() > 0 && contactNumber.length() > 0){
                listFriends.add(Contact(contactNameValue, contactNumberValue))
                FriendsPreferencesAdapter().update(this, "friends", listFriends)
                Toast.makeText(this, "New Friend Added!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        contactCancel.setOnClickListener {
            finish()
        }
    }
}