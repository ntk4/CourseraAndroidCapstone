package org.ntk.mutibo.repository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.ntk.mutibo.repository.Game.Type;

import com.google.common.base.Objects;

/**
 * A simple object to represent a game request by a friend
 */
@Entity
public class GameRequest {

	public enum AnswerType {
		PENDING, START_GAME, REJECT_REQUEST;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "type")
	private Type type;

	@Column(name = "requestingUser")
	private String requestingUser;

	@Column(name = "forUser")
	private String forUser;

	@Column(name = "requestedOn")
	private String requestedOn;

	@Column(name = "answer")
	private AnswerType answer;

//	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Game.class)
//	@JoinColumn(name = "GameID", nullable = true)
	@Column(name = "gameId")
	private long gameId;

	public GameRequest() {
	}

	public GameRequest(Type type, String requestingUser, String forUser, String requestedOn) {
		super();
		this.type = type;
		this.requestingUser = requestingUser;
		this.forUser = forUser;
		this.requestedOn = requestedOn;
		this.answer = AnswerType.PENDING;
	}

	/**
	 * Two Games will generate the same hashcode if they have exactly the same values for their type, started and user1
	 * 
	 */
	@Override
	public int hashCode() {
		// Google Guava provides great utilities for hashing
		return Objects.hashCode(type, requestingUser, requestedOn);
	}

	/**
	 * Two Games are considered equal if they have exactly the same values for their type, started and user1.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GameRequest) {
			GameRequest other = (GameRequest) obj;
			// Google Guava provides great utilities for equals too!
			return Objects.equal(type, other.type) && Objects.equal(requestingUser, other.requestingUser)
					&& Objects.equal(requestedOn, other.requestedOn);
		} else {
			return false;
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public String getRequestingUser() {
		return requestingUser;
	}

	public void setRequestingUser(String requestingUser) {
		this.requestingUser = requestingUser;
	}

	public String getForUser() {
		return forUser;
	}

	public void setForUser(String forUser) {
		this.forUser = forUser;
	}

	public String getRequestedOn() {
		return requestedOn;
	}

	public void setRequestedOn(String requestedOn) {
		this.requestedOn = requestedOn;
	}

	public AnswerType getAnswer() {
		return answer;
	}

	public void setAnswer(AnswerType answer) {
		this.answer = answer;
	}

	public long getGameId() {
		return gameId;
	}

	public void setGameId(long gameId) {
		this.gameId = gameId;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
