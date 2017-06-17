/*
 * Copyright (C) 2017 The PixelDust Project
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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.pixeldust.settings.preferences.CustomSeekBarPreference;
import com.pixeldust.settings.utils.Utils;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_STATUS_BAR_WEATHER = "status_bar_weather";
    private static final String PREF_CATEGORY_WEATHER = "pref_cat_weather";
    private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";
    private static final String STATUS_BAR_SHOW_TICKER = "status_bar_show_ticker";
    private static final String PREF_TEXT_COLOR = "status_bar_ticker_text_color";
    private static final String PREF_ICON_COLOR = "status_bar_ticker_icon_color";
    private static final String STATUS_BAR_TICKER_FONT_STYLE = "status_bar_ticker_font_style";
    private static final String STATUS_BAR_TICKER_FONT_SIZE  = "status_bar_ticker_font_size";

    private ListPreference mShowTicker;
    private ColorPickerPreference mTextColor;
    private ColorPickerPreference mIconColor;
    private ListPreference mStatusBarWeather;
    private CustomSeekBarPreference mTickerFontSize;
    private ListPreference mTickerFontStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pixeldust_settings_statusbar);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final PreferenceCategory categoryWeather = (PreferenceCategory) prefScreen.findPreference(PREF_CATEGORY_WEATHER);
        PackageManager pm = getPackageManager();

        // Status bar weather
        mStatusBarWeather = (ListPreference) prefScreen.findPreference(PREF_STATUS_BAR_WEATHER);
        if (mStatusBarWeather != null && (!Utils.isPackageInstalled(WEATHER_SERVICE_PACKAGE, pm))) {
            categoryWeather.removePreference(mStatusBarWeather);
        } else {
            int temperatureShow = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, 0,
                UserHandle.USER_CURRENT);
            mStatusBarWeather.setValue(String.valueOf(temperatureShow));
            if (temperatureShow == 0) {
                mStatusBarWeather.setSummary(R.string.statusbar_weather_summary);
            } else {
                mStatusBarWeather.setSummary(mStatusBarWeather.getEntry());
            }
            mStatusBarWeather.setOnPreferenceChangeListener(this);
        }
        // Notification ticker
        mShowTicker = (ListPreference) findPreference(STATUS_BAR_SHOW_TICKER);
        mShowTicker.setOnPreferenceChangeListener(this);
        int tickerMode = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_SHOW_TICKER,
                0, UserHandle.USER_CURRENT);
        mShowTicker.setValue(String.valueOf(tickerMode));
        mShowTicker.setSummary(mShowTicker.getEntry());

        mTextColor = (ColorPickerPreference) findPreference(PREF_TEXT_COLOR);
        mTextColor.setOnPreferenceChangeListener(this);
        int textColor = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_TICKER_TEXT_COLOR, 0xffffab00);
        String textHexColor = String.format("#%08x", (0xffffab00 & textColor));
        mTextColor.setSummary(textHexColor);
        mTextColor.setNewPreviewColor(textColor);

        mIconColor = (ColorPickerPreference) findPreference(PREF_ICON_COLOR);
        mIconColor.setOnPreferenceChangeListener(this);
        int iconColor = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_TICKER_ICON_COLOR, 0xffffffff);
        String iconHexColor = String.format("#%08x", (0xffffffff & iconColor));
        mIconColor.setSummary(iconHexColor);
        mIconColor.setNewPreviewColor(iconColor);

        mTickerFontSize = (CustomSeekBarPreference) findPreference(STATUS_BAR_TICKER_FONT_SIZE);
        mTickerFontSize.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_TICKER_FONT_SIZE, 14));
        mTickerFontSize.setOnPreferenceChangeListener(this);
  
        mTickerFontStyle = (ListPreference) findPreference(STATUS_BAR_TICKER_FONT_STYLE);
        mTickerFontStyle.setOnPreferenceChangeListener(this);
        mTickerFontStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_TICKER_FONT_STYLE, 0)));
        mTickerFontStyle.setSummary(mTickerFontStyle.getEntry());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarWeather) {
            int temperatureShow = Integer.valueOf((String) newValue);
            int index = mStatusBarWeather.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(resolver,
                   Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP,
                   temperatureShow, UserHandle.USER_CURRENT);
            if (temperatureShow == 0) {
                mStatusBarWeather.setSummary(R.string.statusbar_weather_summary);
            } else {
                mStatusBarWeather.setSummary(
                mStatusBarWeather.getEntries()[index]);
            }
            return true;
        } else if (preference.equals(mShowTicker)) {
            int tickerMode = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_TICKER, tickerMode,
                    UserHandle.USER_CURRENT);
            int index = mShowTicker.findIndexOfValue((String) newValue);
            mShowTicker.setSummary(mShowTicker.getEntries()[index] + "\n" + getResources().getString(R.string.ticker_summary));
            return true;
        } else if (preference == mTextColor) {
            String hex = ColorPickerPreference.convertToARGB(
                   Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_TICKER_TEXT_COLOR, intHex);
            return true;
        } else if (preference == mIconColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_TICKER_ICON_COLOR, intHex);
            return true;
        } else if (preference == mTickerFontSize) {
            int width = ((Integer)newValue).intValue();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_TICKER_FONT_SIZE, width);
            return true;
        } else if (preference == mTickerFontStyle) {
            int val = Integer.parseInt((String) newValue);
            int index = mTickerFontStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_TICKER_FONT_STYLE, val);
            mTickerFontStyle.setSummary(mTickerFontStyle.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.PIXELDUST;
    }

}
