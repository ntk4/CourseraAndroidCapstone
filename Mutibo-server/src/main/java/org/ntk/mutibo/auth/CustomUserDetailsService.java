package org.ntk.mutibo.auth;

import org.ntk.mutibo.repository.MutiboUser;
import org.ntk.mutibo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsService {

	private UserDetails admin = User.create("admin", "pass", "ADMIN", "USER");
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if ("admin".equals(username)) {
			return admin;
		}
		
		MutiboUser user = userRepository.findByUsername(username);
		if (user != null) {
			return User.create(user.getUsername(), user.getPassword(), "USER");
		}
		
		return null;
	}

}
