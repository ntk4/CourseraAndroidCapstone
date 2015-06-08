package org.ntk.mutibo.android.model;

import java.io.Serializable;

import org.ntk.mutibo.api.MutiboApi;

public class LoginInfo implements Serializable {

	private String username;
	private String password;
	private String serverAddress;
	private MutiboApi service;

	public LoginInfo(String username, String password, String serverAddress, MutiboApi service) {
		super();
		this.username = username;
		this.password = password;
		this.serverAddress = serverAddress;
		this.service = service;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public MutiboApi getService() {
		return service;
	}

	public void setService(MutiboApi service) {
		this.service = service;
	}

}
