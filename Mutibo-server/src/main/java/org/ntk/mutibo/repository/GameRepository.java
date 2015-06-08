package org.ntk.mutibo.repository;

import java.util.Collection;
import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

@Transactional
public interface GameRepository extends CrudRepository<Game, Long>{

	Collection<Game> findByType(String type);
	
	Collection<Game> findByUser1(String user1);
	
	Collection<Game> findByUser2(String user2);
	
	Collection<Game> findByStartedGreaterThan(Date started);
	
	Collection<Game> findByFinishedIsNull();
}