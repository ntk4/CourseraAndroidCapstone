package org.ntk.mutibo.android.model;

import java.util.List;

import org.ntk.mutibo.android.model.Playable.Type;
import org.ntk.mutibo.json.Game;
import org.ntk.mutibo.json.GameRequest;
import org.ntk.mutibo.json.ItemSet;
import org.ntk.mutibo.json.MutiboUser;

import android.graphics.drawable.Drawable;

/**
 * Listener interface for all game events that involve server communication. Typically will be implemented by the UI
 * thread, particularly the main activity in order to update the UI components upon a successful (or not) game
 * operation.
 * 
 * @author Nick
 * 
 */
public interface GameEventListener {

	void onErrorFromTask(TaskType taskWithErrorResult);

	void onDemoGame(Playable game);

	void onLoginSuccess(LoginInfo result);

	void onGameStarted(Playable game, ItemSet firstQuestion);

	void onNextItemset(ItemSet itemSet);

	void onAnswerItemSet(ItemSet itemSet, int answer, Drawable answerBitmap);

	void onGameFinished(Playable game);

	void onItemSetLiked(ItemSet result);

	void onItemSetDisliked(ItemSet result);
	
	void onUsersLoaded(List<MutiboUser> result);
	
	void onUserSelected(Type gameType, MutiboUser user);

	void onSignUp(String server);

	void onUserRegistered(MutiboUser result);

	void onGameRequested(GameRequest result);

	void onGameJoined(Game result);

}