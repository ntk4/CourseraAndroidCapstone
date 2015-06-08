package org.ntk.mutibo.json;

import java.sql.Timestamp;
import java.util.List;

import org.ntk.mutibo.android.model.Playable;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * A simple object to represent a game of any type for one or two players
 */
public class Game implements Playable {

//	private static final boolean ALLOW_DUPLICATE_GAME_SETS = true; // true is like debug mode

	public static final int MAX_LIVES = 3;

	public static final int INITIAL_SETS_TO_LOAD = 4;

	private long id;

	private Type type;

	private String user1;

	private String user2;

	private Timestamp started;
	private Timestamp finished;

	private List<GameSet> gameSets;
	
	private ItemSet firstQuestion;

	private int score;
	
	private int answers;
	
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

	public ItemSet getFirstQuestion() {
		return firstQuestion;
	}

	public void setFirstQuestion(ItemSet firstQuestion) {
		this.firstQuestion = firstQuestion;
	}

	public void addSet(ItemSet itemSet) {
		GameSet gameSet = new GameSet(itemSet.getId(), this.getId(), gameSets.size() + 1, 0);
		gameSets.add(gameSet);
	}

	@Override
	public void answer(ItemSet itemSet, int answer) {
		if (itemSet.isAnswerCorrect(answer)) {
			addCorrectAnswerToScore(itemSet);
		} else {
			lives--;
		}
	}

	private void addCorrectAnswerToScore(ItemSet itemSet) {
		setScore(getScore() + SCORE_PER_CORRECT_ANSWER);
	}
	
	public boolean isGameOver() {
		return lives <= 0;
	}
}
