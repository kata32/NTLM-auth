package it.methods.ntlmauth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat
{
	@Override
	public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey)
	{
		addPreferencesFromResource(R.xml.pref_settings);
	}
}
