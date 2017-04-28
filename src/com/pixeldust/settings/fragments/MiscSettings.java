package com.pixeldust.settings.fragments;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.util.pixeldust.ActionUtils;
import com.android.settings.DevelopmentSettings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.pixeldust.settings.utils.Utils;

import java.util.Arrays;
import java.util.HashSet;

public class MiscSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String INCALL_VIB_OPTIONS = "incall_vib_options";

    private static final String KEY_LOCK_CLOCK = "lock_clock";
    private static final String KEY_LOCK_CLOCK_PACKAGE_NAME = "com.cyanogenmod.lockclock";

    private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
    private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
    private static final String SCROLLINGCACHE_DEFAULT = "1";

    private ListPreference mScrollingCachePref;
    private PreferenceCategory mMiscCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pixeldust_settings_misc);
        PreferenceScreen prefs = getPreferenceScreen();

        PreferenceCategory incallVibCategory = (PreferenceCategory) findPreference(INCALL_VIB_OPTIONS);
        if (!Utils.isVoiceCapable(getActivity())) {
            prefs.removePreference(incallVibCategory);
        }

        mScrollingCachePref = (ListPreference) findPreference(SCROLLINGCACHE_PREF);
        mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
        mScrollingCachePref.setOnPreferenceChangeListener(this);

        mMiscCategory = (PreferenceCategory) prefs.findPreference(KEY_LOCK_CLOCK);
        if (mMiscCategory != null && !isLockclockInstalled()) {
            prefs.removePreference(mMiscCategory);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mScrollingCachePref) {
            if (objValue != null) {
                SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, (String)objValue);
            return true;
            }
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.PIXELDUST;
    }

    private boolean isLockclockInstalled() {
         return ActionUtils.isAvailableApp(KEY_LOCK_CLOCK_PACKAGE_NAME, getActivity());
    }

}
