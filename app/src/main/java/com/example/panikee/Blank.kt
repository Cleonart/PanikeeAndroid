package com.example.panikee

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.example.panikee.pages.Login

class Blank : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val loggedIn = preferences.getString("logged_in", null)
        var intent = Intent(this, Login::class.java)

        if (loggedIn != null){
            intent = Intent(this, MainActivity::class.java)
        }

        finish()
        startActivity(intent)
    }
}