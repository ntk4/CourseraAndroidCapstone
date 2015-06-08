package org.ntk.mutibo.android.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;

public class WaitDialog {

	private static ProgressDialog progressDialog;

	public static void init(Activity activity) {
		progressDialog = new ProgressDialog(activity);
		progressDialog.setTitle("Working...");
		progressDialog.setMessage("Please wait");
		progressDialog.setCancelable(true);
		progressDialog.setIndeterminate(true);
		progressDialog.setOnCancelListener(new ProgressDialog.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface progressDialog) {
				if (progressDialog != null && ((ProgressDialog) progressDialog).isShowing()) {
					progressDialog.dismiss();
				}
			}

		});
	}

	public static void show() {
		if (progressDialog == null)
			throw new RuntimeException(
					"ProgressDialog has not been initialized, please call init() before calling show()");
		progressDialog.show();
	}

	public static void dismiss() {
		if (progressDialog == null)
			throw new RuntimeException(
					"ProgressDialog has not been initialized, please call init() before calling dismiss()");
		progressDialog.dismiss();
	}
}
