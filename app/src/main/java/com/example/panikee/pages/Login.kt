package com.example.panikee.pages

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.panikee.MainActivity
import com.example.panikee.R
import com.example.panikee.data.vo.Contact
import com.example.panikee.data.vo.User
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser

class Login : AppCompatActivity(){

    private lateinit var usernameText : EditText
    private lateinit var passwordText : EditText
    private lateinit var loginButton : Button
    private lateinit var loginToRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameText = findViewById(R.id.loginUsername)
        passwordText = findViewById(R.id.loginPassword)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val loggedIn = preferences.getString("logged_in", null)
        if (loggedIn != null){
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }

        loginToRegister = findViewById(R.id.loginToRegister)
        loginToRegister.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            finish()
            startActivity(intent)
        }

        loginButton = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            login()
        }
    }

    fun formsValid() : Boolean{
        if(usernameText.length() == 0) { usernameText.error = "Username need to be filled" }
        if(passwordText.length() == 0){ passwordText.error = "Password need to be filled" }
        if (usernameText.length() > 0 &&
            passwordText.length() > 0) {
            return true
        }
        return false
    }

    fun login(){
        if (formsValid()){
            val usernameTextData = usernameText.text.toString()
            val usernamePasswordData = passwordText.text.toString()

            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            val jsonString = preferences.getString("user", null)

            if(jsonString != null){
                val jsonElem : JsonElement = JsonParser().parse(jsonString)
                val cyx : User = Gson().fromJson(jsonElem, User::class.java)
                Log.d("tes", cyx.username)
                Log.d("tes", cyx.password)
                if(usernameTextData == cyx.username && usernamePasswordData == cyx.password){
                    val intent = Intent(this, MainActivity::class.java)
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                    preferences.edit().putString("logged_in", "logged").apply()
                    finish()
                    startActivity(intent)
                    return
                }
                Toast.makeText(this, "Username and password didn't match!", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Username and password didn't match!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}