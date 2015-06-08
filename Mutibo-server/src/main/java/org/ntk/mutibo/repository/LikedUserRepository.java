package org.ntk.mutibo.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

@Transactional
public interface LikedUserRepository extends CrudRepository<LikedUser, Long>{
	
	List<LikedUser> findByLocalUserAndSetId(String localUser, Long setId);
	
}