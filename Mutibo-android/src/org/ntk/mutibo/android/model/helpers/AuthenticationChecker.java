package org.ntk.mutibo.android.model.helpers;

import org.ntk.mutibo.android.model.LoginInfo;

public class AuthenticationChecker {

	public static boolean checkValidSession(LoginInfo login) {
		return login != null && login.getUsername() != null && !"".equals(login.getUsername())
				&& login.getPassword() != null && !"".equals(login.getPassword()) && login.getServerAddress() != null
				&& !"".equals(login.getServerAddress());
	}
}
