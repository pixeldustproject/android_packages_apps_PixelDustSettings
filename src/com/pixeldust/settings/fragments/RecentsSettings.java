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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.util.pixeldust.ActionUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.pixeldust.settings.preferences.SystemSettingSwitchPreference;

public class RecentsSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String RECENTS_USE_SLIM = "recents_use_slim";
    private static final String RECENTS_USE_AOSP = "recents_use_aosp";
    private static final String RECENTS_USE_OMNISWITCH = "recents_use_omniswitch";
    private static final String OMNISWITCH_START_SETTINGS = "omniswitch_start_settings";
    public static final String OMNISWITCH_PACKAGE_NAME = "org.omnirom.omniswitch";
    public static Intent INTENT_OMNISWITCH_SETTINGS = new Intent(Intent.ACTION_MAIN).setClassName(OMNISWITCH_PACKAGE_NAME,
                                OMNISWITCH_PACKAGE_NAME + ".SettingsActivity");
    private static final String SLIM_RECENTS_SETTINGS = "slim_recent_panel";
    private static final String CATEGORY_AOSP_RECENTS = "aosp_recents";
    private static final String CATEGORY_OMNI_RECENTS = "omni_recents";
    private static final String CATEGORY_SLIM_RECENTS = "slim_recents";

    private Preference mOmniSwitchSettings;
    private Preference mSlimRecentsSettings;
    private PreferenceCategory mOmniRecents;
    private PreferenceCategory mAOSPRecents;
    private PreferenceCategory mSlimRecents;
    private SwitchPreference mUseAOSPRecents;
    private SwitchPreference mRecentsUseSlim;
    private SwitchPreference mRecentsUseOmniSwitch;

    private boolean mOmniSwitchInitCalled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pixeldust_settings_recents);
        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();

        mAOSPRecents = (PreferenceCategory) findPreference(CATEGORY_AOSP_RECENTS);
        mOmniRecents = (PreferenceCategory) findPreference(CATEGORY_OMNI_RECENTS);
        mSlimRecents = (PreferenceCategory) findPreference(CATEGORY_SLIM_RECENTS);

        mRecentsUseOmniSwitch = (SwitchPreference) prefSet.findPreference(RECENTS_USE_OMNISWITCH);
        try {
            mRecentsUseOmniSwitch.setChecked(Settings.System.getInt(resolver,
                    Settings.System.RECENTS_USE_OMNISWITCH) == 1);
            mOmniSwitchInitCalled = true;
        } catch(SettingNotFoundException e){
            // if the settings value is unset
        }
        mRecentsUseOmniSwitch.setOnPreferenceChangeListener(this);

        mOmniSwitchSettings = (Preference) prefSet.findPreference(OMNISWITCH_START_SETTINGS);

        mOmniSwitchSettings.setEnabled(mRecentsUseOmniSwitch.isChecked());
        if (!ActionUtils.isAvailableApp(OMNISWITCH_PACKAGE_NAME, getActivity())) {
            prefSet.removePreference(mOmniSwitchSettings);
        }

        mUseAOSPRecents = (SwitchPreference) prefSet.findPreference(RECENTS_USE_AOSP);
        mRecentsUseSlim = (SwitchPreference) prefSet.findPreference(RECENTS_USE_SLIM);
        mRecentsUseSlim.setOnPreferenceChangeListener(this);
        mSlimRecentsSettings = (Preference) prefSet.findPreference(SLIM_RECENTS_SETTINGS);
        updateRecents();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mOmniSwitchSettings){
            startActivity(INTENT_OMNISWITCH_SETTINGS);
            return true;
        } else if (preference == mSlimRecentsSettings) {
            Intent intent = new Intent(getActivity(), SubActivity.class);
            intent.putExtra(SubActivity.EXTRA_FRAGMENT_CLASS, SlimRecentPanel.class.getName());
            intent.putExtra(SubActivity.EXTRA_TITLE,
                    getResources().getString(R.string.recent_panel_category));
            getActivity().startActivity(intent);
        return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean omniRecents = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.RECENTS_USE_OMNISWITCH, 0) == 1;
        boolean slimRecents = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.USE_SLIM_RECENTS, 0) == 1;
        if (preference == mRecentsUseOmniSwitch) {
            boolean value = (Boolean) newValue;
            if (value && !mOmniSwitchInitCalled){
                openOmniSwitchFirstTimeWarning();
                mOmniSwitchInitCalled = true;
            }
            Settings.System.putInt(
                    resolver, Settings.System.RECENTS_USE_OMNISWITCH, value ? 1 : 0);
            mOmniSwitchSettings.setEnabled(value);
            updateRecents();
            return true;
        } else if (preference == mRecentsUseSlim) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(
                    resolver, Settings.System.USE_SLIM_RECENTS, value ? 1 : 0);
            updateRecents();
            return true;
        }
        return false;
    }

    private void openOmniSwitchFirstTimeWarning() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.omniswitch_first_time_title))
                .setMessage(getResources().getString(R.string.omniswitch_first_time_message))
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
    }

    private void updateRecents() {
        boolean omniRecents = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.RECENTS_USE_OMNISWITCH, 0) == 1;
        boolean slimRecents = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.USE_SLIM_RECENTS, 0) == 1;

        // update checked state for all toggles
        mUseAOSPRecents.setChecked(!omniRecents && !slimRecents);
        mRecentsUseOmniSwitch.setChecked(omniRecents && !slimRecents);
        mRecentsUseSlim.setChecked(slimRecents && !omniRecents);

        // update the enabled state for all options
        mAOSPRecents.setEnabled(!omniRecents && !slimRecents);
        // Slim recents overwrites omni recents
        mOmniRecents.setEnabled(omniRecents || !slimRecents);
        // Don't allow OmniSwitch if we're already using slim recents
        mSlimRecents.setEnabled(slimRecents || !omniRecents);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.PIXELDUST;
    }
}
