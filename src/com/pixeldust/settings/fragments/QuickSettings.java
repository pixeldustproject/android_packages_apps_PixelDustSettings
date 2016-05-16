/*
 *  Copyright (C) 2017 The PixelDust Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.pixeldust.settings.fragments;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.Utils;

import java.util.List;
import java.util.ArrayList;

public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
    private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";

    private ListPreference mTileAnimationStyle;
    private ListPreference mTileAnimationDuration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pixeldust_settings_quicksettings);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        mTileAnimationStyle = (ListPreference) findPreference(PREF_TILE_ANIM_STYLE);
        int tileAnimationStyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_STYLE, 0,
                UserHandle.USER_CURRENT);
        mTileAnimationStyle.setValue(String.valueOf(tileAnimationStyle));
        updateTileAnimationStyleSummary(tileAnimationStyle);
        updateAnimTileDuration(tileAnimationStyle);
        mTileAnimationStyle.setOnPreferenceChangeListener(this);

        mTileAnimationDuration = (ListPreference) findPreference(PREF_TILE_ANIM_DURATION);
        int tileAnimationDuration = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_DURATION, 2000,
                UserHandle.USER_CURRENT);
        mTileAnimationDuration.setValue(String.valueOf(tileAnimationDuration));
        updateTileAnimationDurationSummary(tileAnimationDuration);
        mTileAnimationDuration.setOnPreferenceChangeListener(this);
        }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mTileAnimationStyle) {
            int tileAnimationStyle = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.ANIM_TILE_STYLE,
                    tileAnimationStyle, UserHandle.USER_CURRENT);
            updateTileAnimationStyleSummary(tileAnimationStyle);
            updateAnimTileDuration(tileAnimationStyle);
            return true;
        } else if (preference == mTileAnimationDuration) {
            int tileAnimationDuration = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.ANIM_TILE_DURATION,
                    tileAnimationDuration, UserHandle.USER_CURRENT);
            updateTileAnimationDurationSummary(tileAnimationDuration);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateTileAnimationStyleSummary(int tileAnimationStyle) {
        String prefix = (String) mTileAnimationStyle.getEntries()[mTileAnimationStyle.findIndexOfValue(String
                .valueOf(tileAnimationStyle))];
        mTileAnimationStyle.setSummary(getResources().getString(R.string.qs_set_animation_style, prefix));
    }

    private void updateTileAnimationDurationSummary(int tileAnimationDuration) {
        String prefix = (String) mTileAnimationDuration.getEntries()[mTileAnimationDuration.findIndexOfValue(String
                .valueOf(tileAnimationDuration))];
        mTileAnimationDuration.setSummary(getResources().getString(R.string.qs_set_animation_duration, prefix));
    }

    private void updateAnimTileDuration(int tileAnimationStyle) {
        if (mTileAnimationDuration != null) {
            if (tileAnimationStyle == 0) {
                mTileAnimationDuration.setSelectable(false);
            } else {
                mTileAnimationDuration.setSelectable(true);
            }
        }
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.PIXELDUST;
    }

}
