package org.ntk.mutibo.android.model;

import java.util.ArrayDeque;
import java.util.List;

import org.ntk.mutibo.json.ItemSet;

import com.google.common.base.Objects;

/**
 * Represents a demo game and is a client (android) only object
 */
public class DemoGame implements Playable {

	private ArrayDeque<ItemSet> itemSets;

	private int score;

	public DemoGame() {
	}

	public DemoGame(List<ItemSet> itemSets) {
		super();
		this.itemSets = new ArrayDeque<ItemSet>(itemSets);
	}

	/**
	 * Two Games will generate the same hashcode if they have exactly the same values for their type, started and user1
	 * 
	 */
	@Override
	public int hashCode() {
		// Google Guava provides great utilities for hashing
		return Objects.hashCode(itemSets);
	}

	/**
	 * Two Games are considered equal if they have exactly the same values for their type, started and user1.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DemoGame) {
			DemoGame other = (DemoGame) obj;
			// Google Guava provides great utilities for equals too!
			return Objects.equal(itemSets, other.itemSets);
		} else {
			return false;
		}
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public ArrayDeque<ItemSet> getItemSets() {
		return itemSets;
	}

	@Override
	public void addSet(ItemSet itemSet) {
		if (itemSets != null)
			itemSets.add(itemSet);
	}

	public boolean hasItemSets() {
		return itemSets != null && itemSets.size() > 0;
	}

	public ItemSet pollNextItemSet() {
		return itemSets.poll();
	}

	public ItemSet peekNextItemSet() {
		return itemSets.peek();
	}
	

	@Override
	public void answer(ItemSet itemSet, int answer) {
		if (itemSet.isAnswerCorrect(answer)) {
			addCorrectAnswerToScore(itemSet);
		}
	}

	private void addCorrectAnswerToScore(ItemSet itemSet) {
		setScore(getScore() + SCORE_PER_CORRECT_ANSWER);
	}

	@Override
	public Type getType() {
		return Playable.Type.DEMO;
	}

	@Override
	public boolean isGameOver() {
		return false;
	}

	@Override
	public int getLives() {
		return 3;
	}

}
