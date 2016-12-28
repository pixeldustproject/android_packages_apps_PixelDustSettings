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

import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import com.android.settings.R;
import android.support.annotation.NonNull;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.pixeldust.settings.preferences.SystemSettingSwitchPreference;
import com.pixeldust.settings.preferences.CustomSeekBarPreference;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class PowerMenuSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String PREF_ON_THE_GO_ALPHA = "on_the_go_alpha";

    private static final String POWER_CATEGORY = "power_category";
    private static final String ACTION_CATEGORY = "action_category";

    private static final int MY_USER_ID = UserHandle.myUserId();

    private CustomSeekBarPreference mOnTheGoAlphaPref;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.PIXELDUST;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pixeldust_settings_power);
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final LockPatternUtils lockPatternUtils = new LockPatternUtils(getActivity());
 
        final PreferenceCategory actionCategory =
                (PreferenceCategory) prefScreen.findPreference(ACTION_CATEGORY);
        final PreferenceCategory powerCategory =
                (PreferenceCategory) prefScreen.findPreference(POWER_CATEGORY);

        if (!lockPatternUtils.isSecure(MY_USER_ID)) {
            prefScreen.removePreference(powerCategory);
        }

        mOnTheGoAlphaPref = (CustomSeekBarPreference) findPreference(PREF_ON_THE_GO_ALPHA);
        float OTGAlpha = Settings.System.getFloat(getContentResolver(), Settings.System.ON_THE_GO_ALPHA,
                    0.5f);
        final int alpha = ((int) (OTGAlpha * 100));
        mOnTheGoAlphaPref.setValue(alpha);
        mOnTheGoAlphaPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mOnTheGoAlphaPref) {
            float val = (Integer) newValue;
            Settings.System.putFloat(getContentResolver(), Settings.System.ON_THE_GO_ALPHA,
                    val / 100);
            return true;
        }
        return false;
    }

}
