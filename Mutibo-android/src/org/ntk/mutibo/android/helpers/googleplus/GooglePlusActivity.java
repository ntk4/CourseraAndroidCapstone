/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ntk.mutibo.android.helpers.googleplus;

import java.util.ArrayList;

import org.ntk.mutibo.android.R;
import org.ntk.mutibo.android.helpers.googleplus.GooglePlusHelper.GooglePlusListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

/**
 * Android Google+ Quickstart activity.
 * 
 * Demonstrates Google+ Sign-In and usage of the Google+ APIs to retrieve a users profile information.
 */
public class GooglePlusActivity extends FragmentActivity implements GooglePlusListener, View.OnClickListener {

	private static final String TAG = "android-plus-quickstart";

	private SignInButton mSignInButton;
	private ImageButton mSignOutButton;
	private Button mRevokeButton;
	private TextView mStatus;
	private ListView mCirclesListView;
	private ArrayAdapter<String> mCirclesAdapter;
	private ArrayList<String> mCirclesList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.googleplus_activity);

		mSignInButton = (SignInButton) findViewById(R.id.googleplus_sign_in_button2);
		mSignOutButton = (ImageButton) findViewById(R.id.googleplus_sign_out_button2);
		mRevokeButton = (Button) findViewById(R.id.revoke_access_button);
		mStatus = (TextView) findViewById(R.id.sign_in_status);
		mCirclesListView = (ListView) findViewById(R.id.circles_list);

		mSignInButton.setOnClickListener(this);
		mSignOutButton.setOnClickListener(this);
		mRevokeButton.setOnClickListener(this);

		mCirclesList = new ArrayList<String>();
		mCirclesAdapter = new ArrayAdapter<String>(this, R.layout.circle_member, mCirclesList);
		mCirclesListView.setAdapter(mCirclesAdapter);

		GooglePlusHelper.instance().init(this, this);
		GooglePlusHelper.instance().loadState(savedInstanceState);
	}

	@Override
	protected void onStop() {
		super.onStop();
		GooglePlusHelper.instance().disconnect();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		GooglePlusHelper.instance().saveState(outState);
	}

	@Override
	public void onClick(View v) {
		if (!GooglePlusHelper.instance().isConnecting()) {
			// We only process button clicks when GoogleApiClient is not transitioning
			// between connected and not connected.
			switch (v.getId()) {
			case R.id.googleplus_sign_in_button2:
				mStatus.setText(R.string.status_signing_in);
				GooglePlusHelper.instance().connect();
				GooglePlusHelper.instance().resolveSignInError();
				break;
			case R.id.googleplus_sign_out_button2:
				GooglePlusHelper.instance().signOut();
				break;
			case R.id.revoke_access_button:
				GooglePlusHelper.instance().revokeAccess();
				break;
			}
		}
	}

	/*
	 * onConnected is called when our Activity successfully connects to Google Play services. onConnected indicates that
	 * an account was selected on the device, that the selected account has granted any requested permissions to our app
	 * and that we were able to establish a service connection to Google Play services.
	 */
	@Override
	public void onConnectionSucceeded(Person currentUser) {
		// Update the user interface to reflect that the user is signed in.
		mSignInButton.setEnabled(false);
		mSignOutButton.setEnabled(true);
		mRevokeButton.setEnabled(true);

		mStatus.setText(String.format(getResources().getString(R.string.signed_in_as), currentUser.getDisplayName()));

	}

	@Override
	public void onLoadPeopleDataSucceeded(PersonBuffer personsInCircle) {
		mCirclesList.clear();
		try {
			int count = personsInCircle.getCount();
			for (int i = 0; i < count; i++) {
				mCirclesList.add(personsInCircle.get(i).getDisplayName());
			}
		} finally {
			personsInCircle.close();
		}

		mCirclesAdapter.notifyDataSetChanged();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignedOut() {

		// Update the UI to reflect that the user is signed out.
		mSignInButton.setEnabled(true);
		mSignOutButton.setEnabled(false);
		mRevokeButton.setEnabled(false);

		mStatus.setText(R.string.status_signed_out);

		mCirclesList.clear();
		mCirclesAdapter.notifyDataSetChanged();
	}
	


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		GooglePlusHelper.instance().handleActivityResult(requestCode, resultCode, data);
	}
}
