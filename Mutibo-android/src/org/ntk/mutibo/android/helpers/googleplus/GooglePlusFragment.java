package org.ntk.mutibo.android.helpers.googleplus;

import org.ntk.mutibo.android.R;
import org.ntk.mutibo.android.helpers.googleplus.GooglePlusHelper.GooglePlusListener;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

public class GooglePlusFragment extends Fragment implements GooglePlusListener, View.OnClickListener {
	
	private SignInButton mSignInButton;
	private ImageButton mSignOutButton;

	public GooglePlusFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_googleplus, container, false);

		mSignInButton = (SignInButton) rootView.findViewById(R.id.googleplus_sign_in_button);
		mSignOutButton = (ImageButton) rootView.findViewById(R.id.googleplus_sign_out_button);

		mSignInButton.setOnClickListener(this);
		mSignOutButton.setOnClickListener(this);

		GooglePlusHelper.instance().init(this.getActivity(), this);
		GooglePlusHelper.instance().loadState(savedInstanceState);
//		GooglePlusHelper.instance().connect();
		showOrHideButtons();

		return rootView;
	}

	private void showOrHideButtons() {
		if (GooglePlusHelper.instance().isConnected()) {
			mSignInButton.setVisibility(View.INVISIBLE);
			mSignOutButton.setVisibility(View.VISIBLE);
		} else {
			mSignInButton.setVisibility(View.VISIBLE);
			mSignOutButton.setVisibility(View.INVISIBLE);			
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		GooglePlusHelper.instance().disconnect();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		GooglePlusHelper.instance().saveState(outState);
	}

	@Override
	public void onClick(View v) {
		if (!GooglePlusHelper.instance().isConnecting()) {
			// We only process button clicks when GoogleApiClient is not transitioning
			// between connected and not connected.
			switch (v.getId()) {
			case R.id.googleplus_sign_in_button:
				GooglePlusHelper.instance().connect();
				GooglePlusHelper.instance().resolveSignInError();
				break;
			case R.id.googleplus_sign_out_button:
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
		
		showOrHideButtons();
	}

	@Override
	public void onLoadPeopleDataSucceeded(PersonBuffer personsInCircle) {
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		showOrHideButtons();
	}

	@Override
	public void onSignedOut() {

		// Update the UI to reflect that the user is signed out.
		mSignInButton.setEnabled(true);
		mSignOutButton.setEnabled(false);
		
		showOrHideButtons();
	}

}
