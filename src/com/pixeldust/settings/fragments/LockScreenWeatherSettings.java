/* 
 * Copyright (C) 2014 DarkKat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pixeldust.settings.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.util.pixeldust.ActionUtils;
import com.android.settings.DevelopmentSettings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.pixeldust.settings.preferences.CustomSeekBarPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LockScreenWeatherSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String PREF_HIDE_WEATHER =
            "weather_hide_panel";
    private static final String PREF_NUMBER_OF_NOTIFICATIONS =
            "weather_number_of_notifications";

    private static final String CATEGORY_WEATHER = "weather_category";
    private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";
    private static final String CHRONUS_ICON_PACK_INTENT = "com.dvtonder.chronus.ICON_PACK";

    private CustomSeekBarPreference mNumberOfNotifications;
    private PreferenceCategory mWeatherCategory;
    private ListPreference mHideWeather;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pixeldust_settings_lock_screen_weather);
        mResolver = getActivity().getContentResolver();
        PreferenceScreen prefs = getPreferenceScreen();

        //OmniJaws
        mHideWeather =
                (ListPreference) findPreference(PREF_HIDE_WEATHER);
        int hideWeather = Settings.System.getInt(mResolver,
               Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, 0);
        mHideWeather.setValue(String.valueOf(hideWeather));
        mHideWeather.setOnPreferenceChangeListener(this);

        mNumberOfNotifications =
                (CustomSeekBarPreference) findPreference(PREF_NUMBER_OF_NOTIFICATIONS);
        int numberOfNotifications = Settings.System.getInt(mResolver,
                Settings.System.LOCK_SCREEN_WEATHER_NUMBER_OF_NOTIFICATIONS, 4);
        mNumberOfNotifications.setValue(numberOfNotifications);
        mNumberOfNotifications.setOnPreferenceChangeListener(this);

        updatePreference();

        mWeatherCategory = (PreferenceCategory) prefs.findPreference(CATEGORY_WEATHER);
        if (mWeatherCategory != null && !isOmniJawsServiceInstalled()) {
            prefs.removePreference(mWeatherCategory);
        }
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.PIXELDUST;
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreference();
    }

    private void updatePreference() {
        int hideWeather = Settings.System.getInt(mResolver,
                Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, 0);
        if (hideWeather == 0) {
            mNumberOfNotifications.setEnabled(false);
            mHideWeather.setSummary(R.string.weather_hide_panel_auto_summary);
        } else if (hideWeather == 1) {
            mNumberOfNotifications.setEnabled(true);
            mHideWeather.setSummary(R.string.weather_hide_panel_custom_summary);
        } else {
            mNumberOfNotifications.setEnabled(false);
            mHideWeather.setSummary(R.string.weather_hide_panel_never_summary);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHideWeather) {
            int intValue = Integer.valueOf((String) newValue);
            int index = mHideWeather.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, intValue);
            updatePreference();
            return true;
        } else if (preference == mNumberOfNotifications) {
            int numberOfNotifications = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCK_SCREEN_WEATHER_NUMBER_OF_NOTIFICATIONS,
            numberOfNotifications);
            return true;
        }
        return false;
    }

    private boolean isOmniJawsServiceInstalled() {
         return ActionUtils.isAvailableApp(WEATHER_SERVICE_PACKAGE, getActivity());
    }
}
