/* 
 **
 ** Copyright 2014, Jules White
 **
 ** 
 */
package org.ntk.mutibo.client;

import org.ntk.mutibo.android.MainActivity;
import org.ntk.mutibo.api.MutiboApi;
import org.ntk.mutibo.client.oauth.EasyHttpClient;
import org.ntk.mutibo.client.oauth.SecuredRestBuilder;

import retrofit.RestAdapter.LogLevel;
import retrofit.client.ApacheClient;
import android.content.Context;
import android.content.Intent;

public class MutiboClientSvc {

	public static final String CLIENT_ID = "mobile";

	private static MutiboApi mutiboApi;

	public static synchronized MutiboApi getOrShowLogin(Context ctx) {
		if (mutiboApi != null) {
			return mutiboApi;
		} else {
			Intent i = new Intent(ctx, MainActivity.class);
			ctx.startActivity(i);
			return null;
		}
	}

	public static synchronized MutiboApi init(String server, String user,
			String pass) {

		mutiboApi = new SecuredRestBuilder()
				.setLoginEndpoint(server + MutiboApi.TOKEN_PATH)
				.setUsername(user)
				.setPassword(pass)
				.setClientId(CLIENT_ID)
				.setClient(
						new ApacheClient(new EasyHttpClient()))
				.setEndpoint(server).setLogLevel(LogLevel.FULL).build()
				.create(MutiboApi.class);

		return mutiboApi;
	}
}
