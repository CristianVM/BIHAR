package com.example.bihar.controller;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

public class ServicioFirebase extends FirebaseMessagingService {

    @Override
    public void onNewToken(String refreshedToken) {
        super.onNewToken(refreshedToken);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("token", refreshedToken);
        editor.apply();
    }
}
