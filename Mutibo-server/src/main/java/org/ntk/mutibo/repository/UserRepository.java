package org.ntk.mutibo.repository;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

@Transactional
public interface UserRepository extends CrudRepository<MutiboUser, Long>{

	MutiboUser findByUsername(String username);
}