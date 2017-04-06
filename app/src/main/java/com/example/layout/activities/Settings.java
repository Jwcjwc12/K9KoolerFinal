package com.example.layout.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.layout.R;

/**
 * Created by jwcam on 3/22/2017.
 */

public class Settings extends PreferenceFragment {
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
