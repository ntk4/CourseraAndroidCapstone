package org.ntk.mutibo.android;

import java.util.concurrent.Callable;

import org.ntk.mutibo.android.helpers.WaitDialog;
import org.ntk.mutibo.android.helpers.googleplus.GooglePlusActivity;
import org.ntk.mutibo.android.helpers.googleplus.GooglePlusFragment;
import org.ntk.mutibo.android.model.GameEventListener;
import org.ntk.mutibo.android.model.GameManager;
import org.ntk.mutibo.android.model.GameManagerFactory;
import org.ntk.mutibo.android.model.LoginInfo;
import org.ntk.mutibo.android.model.Playable;
import org.ntk.mutibo.android.model.TaskType;
import org.ntk.mutibo.api.MutiboApi;
import org.ntk.mutibo.async.CallableTask;
import org.ntk.mutibo.async.TaskCallback;
import org.ntk.mutibo.client.MutiboClientSvc;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class LoginFragment extends Fragment implements OnClickListener {

	protected EditText txtUserName;
	protected EditText txtPassword;
	protected EditText txtServer;
	protected Button btSignin, btSignup;
	protected Button btPlayDemo;

	public LoginFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_login, container, false);

		resolveComponents(rootView);

		return rootView;
	}

	private void resolveComponents(View rootView) {
		txtUserName = (EditText) rootView.findViewById(R.id.logUserName);
		txtPassword = (EditText) rootView.findViewById(R.id.loginPassword);
		txtServer = (EditText) rootView.findViewById(R.id.server);
		btSignin = (Button) rootView.findViewById(R.id.btSignin);
		btSignup = (Button) rootView.findViewById(R.id.btSignup);
		btPlayDemo = (Button) rootView.findViewById(R.id.btPlayDemo);

		btSignin.setOnClickListener(this);
		btSignup.setOnClickListener(this);
		btPlayDemo.setOnClickListener(this);
		
		addGooglePlusFragment();

	}

	public void addGooglePlusFragment() {
		getChildFragmentManager().beginTransaction().add(R.id.googlePlusFragmentContainer, new GooglePlusFragment())
				.commit();
		getChildFragmentManager().executePendingTransactions();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btSignin:
			login(v);
			break;
		case R.id.btSignup:
			signUp(v);
			break;
		case R.id.btPlayDemo:
			Intent intent = new Intent(getActivity(), GooglePlusActivity.class);
			startActivity(intent);
			// playDemo(v);
			break;
		}
	}

	private void signUp(View v) {
		((GameEventListener) getActivity()).onSignUp(txtServer.getText().toString());
	}

	private void playDemo(View v) {
		GameManager manager = GameManagerFactory.getManager(Playable.Type.DEMO, null,
				(GameEventListener) getActivity(), "");
		((MainActivity) getActivity()).setGameManager(manager); // find a way to avoid such casts!
		manager.startNewGame(Playable.Type.DEMO, "test", null);
	}

	public void login(View view) {
		final String user = txtUserName.getText().toString().trim();
		final String pass = txtPassword.getText().toString().trim();
		final String server = txtServer.getText().toString().trim();

		btSignin.setEnabled(false);
		txtUserName.setEnabled(false);
		txtPassword.setEnabled(false);
		txtServer.setEnabled(false);

		try {
			final MutiboApi svc = MutiboClientSvc.init(server, user, pass);
			WaitDialog.show();
			CallableTask.invoke(new Callable<LoginInfo>() {

				@Override
				public LoginInfo call() throws Exception {
					svc.getItemSet(1); // test call that will show if the service reference is correct

					return new LoginInfo(user, pass, server, svc);
				}
			}, new TaskCallback<LoginInfo>() {

				@Override
				public void success(final LoginInfo result) {
					// OAuth 2.0 grant was successful and we can talk to the server
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							((GameEventListener) getActivity()).onLoginSuccess(result);
							WaitDialog.dismiss();
						}
					});
				}

				@Override
				public void error(Exception e) {
					Log.e(MainActivity.class.getName(), "Error logging in via OAuth.", e);

					((GameEventListener) getActivity()).onErrorFromTask(TaskType.LOGIN);
				}
			});
		} finally {
			btSignin.setEnabled(true);
			txtUserName.setEnabled(true);
			txtPassword.setEnabled(true);
			txtServer.setEnabled(true);
		}
	}
}
