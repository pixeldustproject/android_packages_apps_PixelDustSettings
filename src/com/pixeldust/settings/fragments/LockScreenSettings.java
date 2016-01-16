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

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.widget.LockPatternUtils;
import com.pixeldust.settings.preferences.SeekBarPreference;
import android.widget.Toast;

import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.pixeldust.settings.utils.Utils;
import com.pixeldust.settings.preferences.SystemSettingSwitchPreference;

public class LockScreenSettings extends SettingsPreferenceFragment {

    private static final String KEYGUARD_TORCH = "keyguard_toggle_torch";
    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";

    private SystemSettingSwitchPreference mLsTorch;
    private FingerprintManager mFingerprintManager;

    private SystemSettingSwitchPreference mFingerprintVib;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pixeldust_settings_lockscreen);
        PreferenceScreen prefScreen = getPreferenceScreen();

        mLsTorch = (SystemSettingSwitchPreference) findPreference(KEYGUARD_TORCH);
        if (!Utils.deviceSupportsFlashLight(getActivity())) {
            prefScreen.removePreference(mLsTorch);
        }

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SystemSettingSwitchPreference) findPreference(FINGERPRINT_VIB);
        if (!mFingerprintManager.isHardwareDetected()){
            prefScreen.removePreference(mFingerprintVib);
        }
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.PIXELDUST;
    }

}
