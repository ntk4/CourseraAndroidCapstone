package org.ntk.mutibo.repository;


public enum ItemSetDifficulty {
	VERY_EASY(100), EASY(150), NORMAL(200), HARD(250), EXTREME(300);

	private int level; // the value stored in the DB with the containing ItemSet

	private ItemSetDifficulty(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}
	
	public static ItemSetDifficulty getEasiest() {
		return VERY_EASY;
	}
	
	public static ItemSetDifficulty getHardest() {
		return EXTREME;
	}
}
