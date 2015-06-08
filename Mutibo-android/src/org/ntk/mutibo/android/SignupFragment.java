package org.ntk.mutibo.android;

import java.util.concurrent.Callable;

import org.ntk.mutibo.android.model.GameEventListener;
import org.ntk.mutibo.android.model.TaskType;
import org.ntk.mutibo.api.MutiboApi;
import org.ntk.mutibo.async.CallableTask;
import org.ntk.mutibo.async.TaskCallback;
import org.ntk.mutibo.client.MutiboClientSvc;
import org.ntk.mutibo.json.MutiboUser;

import android.app.Fragment;
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

public class SignupFragment extends Fragment implements OnClickListener {

	protected EditText txtUserName;
	protected EditText txtPassword;
	protected EditText txtFullName;
	protected Button btRegister;
	
	private String server;

	public SignupFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_signup, container, false);

		resolveComponents(rootView);
		return rootView;
	}

	private void resolveComponents(View rootView) {
		txtUserName = (EditText) rootView.findViewById(R.id.newUserName);
		txtPassword = (EditText) rootView.findViewById(R.id.newUserPassword);
		txtFullName = (EditText) rootView.findViewById(R.id.newUserFullname);
		btRegister = (Button) rootView.findViewById(R.id.btRegister);

		btRegister.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btRegister:
			register(v);
			break;
		}
	}

	public void register(View view) {
		final String user = txtUserName.getText().toString();
		final String pass = txtPassword.getText().toString();
		final String fullName = txtFullName.getText().toString();

		btRegister.setEnabled(false);
		txtUserName.setEnabled(false);
		txtPassword.setEnabled(false);
		txtFullName.setEnabled(false);

		try {
			// TODO: remove hardcoded admin user and password
			final MutiboApi svc = MutiboClientSvc.init(server, "admin", "pass");

			final MutiboUser newUser = new MutiboUser(0, fullName, user, pass); 
			
			CallableTask.invoke(new Callable<MutiboUser>() {

				@Override
				public MutiboUser call() throws Exception {
					MutiboUser registeredUser = svc.registerUser(newUser);
					
					return registeredUser;
				}
			}, new TaskCallback<MutiboUser>() {

				@Override
				public void success(final MutiboUser result) {
					// OAuth 2.0 grant was successful and we can talk to the server
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							((GameEventListener) getActivity()).onUserRegistered(result);
						}
					});
				}

				@Override
				public void error(Exception e) {
					Log.e(MainActivity.class.getName(), "Error registering user", e);

					((GameEventListener) getActivity()).onErrorFromTask(TaskType.LOGIN);
				}
			});
		} finally {
			btRegister.setEnabled(true);
			txtUserName.setEnabled(true);
			txtPassword.setEnabled(true);
			txtFullName.setEnabled(true);
		}
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
}
