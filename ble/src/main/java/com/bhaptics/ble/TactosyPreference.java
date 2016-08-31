package com.bhaptics.ble;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by westside on 2016-04-25.
 */
public class TactosyPreference {
    public static final String TAG = TactosyPreference.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private String prefix;

    public TactosyPreference(Context con) {
        super();
        this.prefix = TAG;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(con);
        this.setBooleanPreference(this.prefix + "_" + "Exists",true);
        Log.d("TactosyPreference","Instantiated a new preference reader/writer with prefix : \"" + this.prefix + "_\"");
    }

    public static boolean isKnown(Context con) {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(con);
        return s.getBoolean(TAG + "_" + "Exists",false);
    }

    /* String settings */
    public String getStringPreference(String prefName, String defaultValue) {
        return this.sharedPreferences.getString(this.prefix + "_" + prefName, defaultValue);
    }

    public boolean setStringPreference(String prefName, String prefValue) {
        SharedPreferences.Editor ed = this.sharedPreferences.edit();
        ed.putString(this.prefix + "_" + prefName, prefValue);
        return ed.commit();
    }

    public boolean setSetPreference(String prefName, Set<String> set) {
        SharedPreferences.Editor ed = this.sharedPreferences.edit();

        ed.putStringSet(this.prefix + "_" + prefName, set);
        return ed.commit();
    }

    public Set<String> getSetPreference(String prefName) {
        return this.sharedPreferences.getStringSet(this.prefix + "_" + prefName, new HashSet<String>());
    }

    /* Boolean settings */

    public boolean getBooleanPreference(String prefName, boolean defaultValue) {
        return this.sharedPreferences.getBoolean(this.prefix + "_" + prefName, defaultValue);
    }

    public boolean setBooleanPreference(String prefName, boolean prefValue) {
        SharedPreferences.Editor ed = this.sharedPreferences.edit();
        ed.putBoolean(this.prefix + "_" + prefName, prefValue);
        return ed.commit();
    }

    /* Integer settings */

    public int getIntegerPreference(String prefName, Integer defaultValue) {
        return this.sharedPreferences.getInt(this.prefix + "_" + prefName, defaultValue);
    }

    public boolean setIntegerPreference(String prefName,int prefValue) {
        SharedPreferences.Editor ed = this.sharedPreferences.edit();
        ed.putInt(this.prefix + "_" + prefName, prefValue);
        return ed.commit();
    }
}
