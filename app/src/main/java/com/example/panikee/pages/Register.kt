package com.example.panikee.pages

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.panikee.MainActivity
import com.example.panikee.R
import com.example.panikee.data.vo.User
import com.google.gson.Gson

class Register : AppCompatActivity() {

    private lateinit var usernameText : EditText
    private lateinit var usernamePassword : EditText
    private lateinit var usernamePhoneNumber : EditText
    private lateinit var registerButton : Button
    private lateinit var registerToLogin : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        /** Initializing */
        usernameText = findViewById(R.id.registerUsername)
        usernamePassword = findViewById(R.id.registerPassword)
        usernamePhoneNumber = findViewById(R.id.registerPhone)
        registerButton = findViewById(R.id.registerButton)
        registerButton.setOnClickListener {
            register()
        }
        registerToLogin = findViewById(R.id.registerToLogin)
        registerToLogin.setOnClickListener {
            directToLogin()
        }

    }

    fun formsValid() : Boolean{
        if(usernameText.length() == 0) { usernameText.error = "Username need to be filled" }
        if(usernamePassword.length() == 0){ usernamePassword.error = "Password need to be filled" }
        if(usernamePhoneNumber.length() == 0){ usernamePhoneNumber.error = "Password need to be filled" }
        if (usernameText.length() > 0 &&
            usernamePassword.length() > 0 &&
            usernamePhoneNumber.length() > 0) {
            return true
        }
        Toast.makeText(this, "All fields need to be filled!", Toast.LENGTH_SHORT).show()
        return false
    }

    fun register() {
        if (formsValid()){
            val usernameTextData = usernameText.text.toString()
            val usernamePasswordData = usernamePassword.text.toString()
            val usernamePhoneNumberData = usernamePhoneNumber.text.toString()

            /** Register user to preference */
            val user = User(usernameTextData, usernamePasswordData, usernamePhoneNumberData)
            val preferences = PreferenceManager.getDefaultSharedPreferences(this).edit()
            preferences.putString("user", Gson().toJson(user)).apply()

            Toast.makeText(this, "Successfully Register!", Toast.LENGTH_SHORT).show()

            /** Direct to login */
            directToLogin()
        }
    }

    fun directToLogin(){
        val intent = Intent(this, Login::class.java)
        finish()
        startActivity(intent)
    }
}