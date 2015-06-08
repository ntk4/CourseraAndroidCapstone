package org.ntk.mutibo.json;

import java.io.Serializable;

import com.google.common.base.Objects;

/**
 * A simple object to represent a user for the custom implementation of UserDetailsService
 */
public class MutiboUser implements Serializable {

	private long id;

	private String name;
	
	private String username;
	
	private String password;
	
	public MutiboUser() {
	}


	public MutiboUser(long id, String name, String username, String password) {
		super();
		this.id = id;
		this.name = name;
		this.username = username;
		this.password = password;
	}


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
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


	/**
	 * Two users will generate the same hashcode if they have exactly the same values for their name, username
	 * 
	 */
	@Override
	public int hashCode() {
		// Google Guava provides great utilities for hashing
		return Objects.hashCode(name, username);
	}

	/**
	 * Two users are considered equal if they have exactly the same values for their name, username.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MutiboUser) {
			MutiboUser other = (MutiboUser) obj;
			// Google Guava provides great utilities for equals too!
			return Objects.equal(name, other.name) && Objects.equal(username, other.username);
		} else {
			return false;
		}
	}

}
