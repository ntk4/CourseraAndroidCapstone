package org.ntk.mutibo.android.helpers.googleplus;

import org.ntk.mutibo.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

public class GooglePlusHelper implements ConnectionCallbacks, OnConnectionFailedListener,
		ResultCallback<LoadPeopleResult> {

	private static final String TAG = "GooglePlusHelper";

	private static final int STATE_DEFAULT = 0;
	private static final int STATE_SIGN_IN = 1;
	private static final int STATE_IN_PROGRESS = 2;

	private static final int RC_SIGN_IN = 0;

	private static final int DIALOG_PLAY_SERVICES_ERROR = 0;

	private static final String SAVED_PROGRESS = "sign_in_progress";

	//@formatter:off
    // We use mSignInProgress to track whether user has clicked sign in.
    // mSignInProgress can be one of three values:
    //
    //       STATE_DEFAULT: The default state of the application before the user
    //                      has clicked 'sign in', or after they have clicked
    //                      'sign out'.  In this state we will not attempt to
    //                      resolve sign in errors and so will display our
    //                      Activity in a signed out state.
    //       STATE_SIGN_IN: This state indicates that the user has clicked 'sign
    //                      in', so resolve successive errors preventing sign in
    //                      until the user has successfully authorized an account
    //                      for our app.
    //   STATE_IN_PROGRESS: This state indicates that we have started an intent to
    //                      resolve an error, and so we should not start further
    //                      intents until the current intent completes.
    //@formatter:on
	private int mSignInProgress;

	// Used to store the PendingIntent most recently returned by Google Play
	// services until the user clicks 'sign in'.
	private PendingIntent mSignInIntent;

	// Used to store the error code most recently returned by Google Play services
	// until the user clicks 'sign in'.
	private int mSignInError;

	private static GooglePlusHelper theInstance; // singleton instance

	// GoogleApiClient wraps our service connection to Google Play services and
	// provides access to the users sign in state and Google's APIs.
	private GoogleApiClient mGoogleApiClient;
	private Activity activity;

	/**
	 * Listener for the google+ events. Wraps the default ConnectionCallbacks, OnConnectionFailedListener so as not to
	 * expose them outside the scope of this helper. Any connection information necessary to clients will be given via
	 * this listener
	 * 
	 * @author Nick
	 * 
	 */
	public interface GooglePlusListener {
		void onConnectionSucceeded(Person currentUser);

		void onConnectionFailed(ConnectionResult result);

		/**
		 * Event handler that provides the result of loadVisible Google+ API method.
		 * 
		 * TODO: the PersonBuffer has to be wrapped. We shouldn't rely on the client to close it when done working with
		 * its data
		 * 
		 * @param personsInCircle
		 */
		void onLoadPeopleDataSucceeded(PersonBuffer personsInCircle);

		void onSignedOut();
	}

	/** External listener that are called once we have verified connection failed or success */
	private GooglePlusListener listener;

	// make it inaccessible to the outside world
	private GooglePlusHelper() {
	}

	public static GooglePlusHelper instance() {
		if (theInstance == null) {
			theInstance = new GooglePlusHelper();
		}
		return theInstance;
	}

	public void init(Activity activity, GooglePlusListener connectionListener) {

		this.activity = activity;
		this.listener = connectionListener;

		mGoogleApiClient = buildGoogleApiClient(activity);
	}

	public GoogleApiClient getApiClient() {
		if (mGoogleApiClient == null)
			throw new GooglePlusException("GoogleApiClient has not been initialized. Please call init(Context)");
		return mGoogleApiClient;
	}

	private GoogleApiClient buildGoogleApiClient(Context context) {
		// When we build the GoogleApiClient we specify where connected and
		// connection failed callbacks should be returned, which Google APIs our
		// app uses and which OAuth 2.0 scopes our app requests.
		return new GoogleApiClient.Builder(context).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
				.addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN).build();
	}

	public void connect() {
		getApiClient().connect();
	}

	public void disconnect() {
		if (getApiClient().isConnected()) {
			getApiClient().disconnect();
		}

	}

	/*
	 * onConnected is called when our Activity successfully connects to Google Play services. onConnected indicates that
	 * an account was selected on the device, that the selected account has granted any requested permissions to our app
	 * and that we were able to establish a service connection to Google Play services.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		// Reaching onConnected means we consider the user signed in.
		Log.i(TAG, "onConnected");

		// Retrieve some profile information to personalize our app for the user.
		Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);

		if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
			Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

			if (listener != null)
				listener.onConnectionSucceeded(currentPerson);

		}
		// Indicate that the sign in process is complete.
		mSignInProgress = STATE_DEFAULT;
	}

	@Override
	public void onResult(LoadPeopleResult peopleData) {
		if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {

			if (listener != null)
				listener.onLoadPeopleDataSucceeded(peopleData.getPersonBuffer());

		} else {
			Log.e(TAG, "Error requesting visible circles: " + peopleData.getStatus());
		}
	}

	/*
	 * onConnectionFailed is called when our Activity could not connect to Google Play services. onConnectionFailed
	 * indicates that the user needs to select an account, grant permissions or resolve an error in order to sign in.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the javadoc for ConnectionResult to see what error codes might
		// be returned in onConnectionFailed.
		Log.i(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());

		if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
			// An API requested for GoogleApiClient is not available. The device's current
			// configuration might not be supported with the requested API or a required component
			// may not be installed, such as the Android Wear application. You may need to use a
			// second GoogleApiClient to manage the application's optional APIs.
		} else if (mSignInProgress != STATE_IN_PROGRESS) {
			// We do not have an intent in progress so we should store the latest
			// error resolution intent for use when the sign in button is clicked.
			mSignInIntent = result.getResolution();
			mSignInError = result.getErrorCode();

			if (mSignInProgress == STATE_SIGN_IN) {
				// STATE_SIGN_IN indicates the user already clicked the sign in button
				// so we should continue processing errors until the user is signed in
				// or they click cancel.
				resolveSignInError();
			}
			
			// In this sample we consider the user signed out whenever they do not have
			// a connection to Google Play services.
			if (listener != null)
				listener.onSignedOut();
		}

	}

	public void saveState(Bundle outState) {
		outState.putInt(SAVED_PROGRESS, mSignInProgress);
	}

	public void loadState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mSignInProgress = savedInstanceState.getInt(SAVED_PROGRESS, STATE_DEFAULT);
		}
	}

	public void revokeAccess() {
		// After we revoke permissions for the user with a GoogleApiClient
		// instance, we must discard it and create a new one.
		Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
		// Our sample has caches no user data from Google+, however we
		// would normally register a callback on revokeAccessAndDisconnect
		// to delete user data so that we comply with Google developer
		// policies.
		Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
		mGoogleApiClient = buildGoogleApiClient(activity);
		mGoogleApiClient.connect();
	}

	public void signOut() {
		if (getApiClient().isConnected()) {
			// We clear the default account on sign out so that Google Play
			// services will not return an onConnected callback without user
			// interaction.
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			mGoogleApiClient.connect();
		}
	}

	public boolean isConnecting() {
		return getApiClient().isConnecting();
	}
	

	public boolean isConnected() {
		return getApiClient().isConnected();
	}

	/*
	 * Starts an appropriate intent or dialog for user interaction to resolve the current error preventing the user from
	 * being signed in. This could be a dialog allowing the user to select an account, an activity allowing the user to
	 * consent to the permissions being requested by your app, a setting to enable device networking, etc.
	 */
	public void resolveSignInError() {
		if (mSignInIntent != null) {
			// We have an intent which will allow our user to sign in or
			// resolve an error. For example if the user needs to
			// select an account to sign in with, or if they need to consent
			// to the permissions your app is requesting.

			try {
				// Send the pending intent that we stored on the most recent
				// OnConnectionFailed callback. This will allow the user to
				// resolve the error currently preventing our connection to
				// Google Play services.
				mSignInProgress = STATE_IN_PROGRESS;
				activity.startIntentSenderForResult(mSignInIntent.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
			} catch (SendIntentException e) {
				Log.i(TAG, "Sign in intent could not be sent: " + e.getLocalizedMessage());
				// The intent was canceled before it was sent. Attempt to connect to
				// get an updated ConnectionResult.
				mSignInProgress = STATE_SIGN_IN;
				mGoogleApiClient.connect();
			}
		} else {
			// Google Play services wasn't able to provide an intent for some
			// error types, so we show the default Google Play services error
			// dialog which may still start an intent on our behalf if the
			// user can resolve the issue.
			showDialog(DIALOG_PLAY_SERVICES_ERROR);
		}
	}

	protected Dialog showDialog(int id) {
		switch (id) {
		case DIALOG_PLAY_SERVICES_ERROR:
			if (GooglePlayServicesUtil.isUserRecoverableError(mSignInError)) {
				return GooglePlayServicesUtil.getErrorDialog(mSignInError, activity, RC_SIGN_IN,
						new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								Log.e(TAG, "Google Play services resolution cancelled");
								mSignInProgress = STATE_DEFAULT;
								if (listener != null)
									listener.onSignedOut();

							}
						});
			} else {
				return new AlertDialog.Builder(activity).setMessage(R.string.play_services_error)
						.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Log.e(TAG, "Google Play services error could not be " + "resolved: " + mSignInError);
								mSignInProgress = STATE_DEFAULT;
								if (listener != null)
									listener.onSignedOut();
							}
						}).create();
			}
		default:
			return null;
		}
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// The connection to Google Play services was lost for some reason.
		// We call connect() to attempt to re-establish the connection or get a
		// ConnectionResult that we can attempt to resolve.
		getApiClient().connect();
	}

	public void handleActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case RC_SIGN_IN:
			if (resultCode == Activity.RESULT_OK) {
				// If the error resolution was successful we should continue
				// processing errors.
				mSignInProgress = STATE_SIGN_IN;
			} else {
				// If the error resolution was not successful or the user canceled,
				// we should stop processing errors.
				mSignInProgress = STATE_DEFAULT;
			}

			if (!getApiClient().isConnecting()) {
				// If Google Play services resolved the issue with a dialog then
				// onStart is not called so we need to re-attempt connection here.
				getApiClient().connect();
			}
			break;
		}
	}

}
