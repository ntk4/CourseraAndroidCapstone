package org.ntk.mutibo.android;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class OptionsFragment extends PreferenceFragment {

	public OptionsFragment() {
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
