package org.ntk.mutibo.android.model;

import org.ntk.mutibo.json.ItemSet;

/**
 * Marker interface for game classes
 * @author Nick
 *
 */
public interface Playable {

	public static final int SCORE_PER_CORRECT_ANSWER = 1;
	
	public enum Type {
		SOLO, VENDETTA, GANG, DEMO
	}

	void addSet(ItemSet itemSet);
	
	void answer(ItemSet itemSet, int answer);
	
	int getScore();
	
	Type getType();
	
	boolean isGameOver();

	int getLives();
}
