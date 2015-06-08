package org.ntk.mutibo.repository;

import java.util.Collection;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

@Transactional
public interface GameRequestRepository extends CrudRepository<GameRequest, Long>{

	Collection<GameRequest> findByTypeAndRequestingUser(Game.Type type, String requestingUser);
	
	Collection<GameRequest> findByTypeAndRequestingUserAndForUserAndAnswerEquals(Game.Type type, String requestingUser, String forUser, GameRequest.AnswerType answer);
	
	Collection<GameRequest> findByForUserAndAnswerEquals(String forUser, GameRequest.AnswerType answer);
	
	Collection<GameRequest> findByAnswerEquals(GameRequest.AnswerType answer);
	
}