package org.ntk.mutibo.android.model;

import org.ntk.mutibo.android.model.Playable.Type;
import org.ntk.mutibo.json.ItemSet;
import org.ntk.mutibo.json.MutiboUser;

import android.graphics.drawable.Drawable;

public interface GameManager {

	boolean hasActiveGame();
	
	Playable getActiveGame();

	void startNewGame(Playable.Type type, String userStarting, MutiboUser otherUser);

	void loadNextItem(Playable game);

	void answerItemSet(Playable game, ItemSet itemset, int answer, Drawable answerBitmap);

	void finishGame(Playable game);

	void likeItemSet(ItemSet itemSet);

	void dislikeItemSet(ItemSet itemSet);
	
	void getUserList();

	void requestGame(Type gameType, String fromUser, String forUser);

	void joinGame(long gameId, String username);

}