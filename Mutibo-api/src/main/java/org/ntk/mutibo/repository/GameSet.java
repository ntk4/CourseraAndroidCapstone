package org.ntk.mutibo.repository;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.common.base.Objects;

/**
 * A simple object to represent a the intersection of games and itemsets
 */
@Entity
public class GameSet {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
    private long setId;
    
    private long gameId;
    
    private int questionOrder;
    
    private int correctAnswer;

	public GameSet() {
	}


	public GameSet(long setId, long gameId, int questionOrder, int correctAnswer) {
        super();
        this.setId = setId;
        this.gameId = gameId;
        this.questionOrder = questionOrder;
        this.correctAnswer = correctAnswer;
    }


    /**
	 * Two gameSets will generate the same hashcode if they have exactly the same values for their gameId, setId
	 * 
	 */
	@Override
	public int hashCode() {
		// Google Guava provides great utilities for hashing
		return Objects.hashCode(gameId, setId);
	}

	/**
	 * Two gameSets are considered equal if they have exactly the same values for their gameId, setId
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GameSet) {
			GameSet other = (GameSet) obj;
			// Google Guava provides great utilities for equals too!
			return gameId == other.gameId && setId == other.setId;
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


    public long getSetId() {
        return setId;
    }


    public void setSetId(long setId) {
        this.setId = setId;
    }


    public long getGameId() {
        return gameId;
    }


    public void setGameId(long gameId) {
        this.gameId = gameId;
    }


    public int getQuestionOrder() {
        return questionOrder;
    }


    public void setQuestionOrder(int questionOrder) {
        this.questionOrder = questionOrder;
    }


    public int getCorrectAnswer() {
        return correctAnswer;
    }


    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

}
