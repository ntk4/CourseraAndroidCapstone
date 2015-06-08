package org.ntk.mutibo.repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.ntk.mutibo.exception.EntryAlreadyExistsException;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * A simple object to represent a game of any type for one or two players
 */
@Entity
public class Game {

	public enum Type {
		SOLO, VENDETTA, GANG
	}

	public static final int MAX_LIVES = 3;

	public static final int SCORE_PER_CORRECT_ANSWER = 1;

	private static final boolean ALLOW_DUPLICATE_GAME_SETS = false; // true is like debug mode

	public static final int INITIAL_SETS_TO_LOAD = 4;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "type")
	private Type type;

	@Column(name = "user1")
	private String user1;

	@Column(name = "user2")
	private String user2;

	@Column(name = "started")
	private Timestamp started;

	@Column(name = "finished")
	private Timestamp finished;

	@OneToMany(cascade = CascadeType.ALL)
	private List<GameSet> gameSets;

	@Column(name = "score")
	private int score;

	@Column(name = "answers")
	private int answers;

	@Column(name = "lives")
	private int lives;

	public Game() {
	}

	public Game(Type type, String user1, String user2, Timestamp started) {
		super();
		this.type = type;
		this.user1 = user1;
		this.user2 = user2;
		// this.started = started;
		// this.finished = started;
		this.score = 0;
		this.gameSets = Lists.newArrayList();
		this.answers = 0;
		this.lives = MAX_LIVES;
	}

	public Game(long id, Type type, String user1, String user2, Timestamp started, Timestamp finished, int score) {
		this(type, user1, user2, started);
		this.id = id;
		this.finished = finished;
		this.score = score;
	}

	/**
	 * Two Games will generate the same hashcode if they have exactly the same values for their type, started and user1
	 * 
	 */
	@Override
	public int hashCode() {
		// Google Guava provides great utilities for hashing
		return Objects.hashCode(type, started, user1);
	}

	/**
	 * Two Games are considered equal if they have exactly the same values for their type, started and user1.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Game) {
			Game other = (Game) obj;
			// Google Guava provides great utilities for equals too!
			return Objects.equal(type, other.type) && Objects.equal(started, other.started)
					&& Objects.equal(user1, other.user1);
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

	public void setType(Type type) {
		this.type = type;
	}

	public String getUser1() {
		return user1;
	}

	public void setUser1(String user1) {
		this.user1 = user1;
	}

	public String getUser2() {
		return user2;
	}

	public void setUser2(String user2) {
		this.user2 = user2;
	}

	public Timestamp getStarted() {
		return started;
	}

	public void setStarted(Timestamp started) {
		this.started = started;
	}

	public Timestamp getFinished() {
		return finished;
	}

	public void setFinished(Timestamp finished) {
		this.finished = finished;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getAnswers() {
		return answers;
	}

	public void setAnswers(int answers) {
		this.answers = answers;
	}

	public int getLives() {
		return lives;
	}

	public void setLives(int lives) {
		this.lives = lives;
	}

	public List<GameSet> getGameSets() {
		return gameSets;
	}

	public void setGameSets(List<GameSet> gameSets) {
		this.gameSets = gameSets;
	}

	/**
	 * Updates the score with a correct answer. Important! a correct answer should not be given twice! fix the
	 * many-to-many relationship to store this info
	 * 
	 * @param itemSet
	 *            the itemSet to answer
	 */
	public void addCorrectAnswerToScore(ItemSet itemSet) {
		GameSet theGameSet = resolveGameSet(itemSet);

		if (theGameSet != null) {
			theGameSet.setCorrectAnswer(1); // answered at the first attempt (reserved for future alternative design
			setScore(getScore() + SCORE_PER_CORRECT_ANSWER);
		}
	}

	private GameSet resolveGameSet(ItemSet itemSet) {
		for (GameSet gameSet : gameSets) {
			if (gameSet.getSetId() == itemSet.getId()) {
				if (gameSet.getCorrectAnswer() > 0) {
					// already answered
					return null;
				} else {
					// found the game set
					return gameSet;
				}
			}
		}
		return null;
	}

	public void addIncorrectAnswer(ItemSet itemSet) {

		GameSet theGameSet = resolveGameSet(itemSet);
		if (theGameSet != null) {
			theGameSet.setCorrectAnswer(1); // answered at the first attempt (reserved for future alternative design
		}
	}

	public boolean addSet(ItemSet itemSet) throws EntryAlreadyExistsException {
		for (GameSet gameSet : gameSets) {
			if (!ALLOW_DUPLICATE_GAME_SETS && gameSet.getSetId() == itemSet.getId()) {
				return false;
				// throw new EntryAlreadyExistsException(ItemSet.class, itemSet.getId());
			}
		}
		GameSet gameSet = new GameSet(itemSet.getId(), this.getId(), gameSets.size() + 1, 0);
		gameSets.add(gameSet);
		return true;
	}

	public boolean canAnswer(ItemSet itemSet) {

		GameSet theGameSet = resolveGameSet(itemSet);
		if (theGameSet != null) {
			return theGameSet.getCorrectAnswer() == 0;
		}
		return false;
	}

	public void answer(ItemSet itemSet, int answer) {
		if (!canAnswer(itemSet))
			return;

		if (itemSet.isAnswerCorrect(answer)) {
			addCorrectAnswerToScore(itemSet);
			updateGameSet(itemSet, answer, true);
		} else {
			lives--;
			if (isGameOver())
				finished = new Timestamp(new Date().getTime());
			addIncorrectAnswer(itemSet);
			updateGameSet(itemSet, answer, false);
		}
		// TODO: set the answer in the corresponding game set
	}

	private void updateGameSet(ItemSet itemSet, int answer, boolean correct) {
		for (GameSet gameSet : getGameSets()) {
			if (gameSet.getSetId() == itemSet.getId()) {
				gameSet.setCorrectAnswer(correct ? 1 : 0);
				return;
			}
		}
	}

	public boolean isGameOver() {
		return lives <= 0;
	}

}
