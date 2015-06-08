package org.ntk.mutibo.android.model;

public enum DifficultyScoreThresholds {
	VERY_EASY(3), EASY(8), NORMAL(12), HARD(20), EXTREME(100);

	private int score; // the value stored in the DB with the containing ItemSet

	private DifficultyScoreThresholds(int level) {
		this.score = level;
	}

	public int getScore() {
		return score;
	}
}
