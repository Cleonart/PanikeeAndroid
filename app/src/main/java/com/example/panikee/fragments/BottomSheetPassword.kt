package com.example.panikee.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.panikee.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetPassword(ctx : Context, mediaPlayer: MediaPlayer) : BottomSheetDialogFragment(){

    private var atx = ctx
    private var mp = mediaPlayer
    private lateinit var passwordButton : Button
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetStyle)
        mediaPlayer = MediaPlayer.create(atx, R.raw.carlock)
        passwordButton = view.findViewById(R.id.passwordButton)
        passwordButton.setOnClickListener {
            mp.pause()
            mediaPlayer.start()
            Thread.sleep(3000)
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    /** Override the current no-rounded corner theme */
    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

}