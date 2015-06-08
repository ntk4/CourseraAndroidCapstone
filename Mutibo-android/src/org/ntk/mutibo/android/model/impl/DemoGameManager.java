package org.ntk.mutibo.android.model.impl;

import org.ntk.mutibo.android.model.DemoGame;
import org.ntk.mutibo.android.model.GameEventListener;
import org.ntk.mutibo.android.model.GameManager;
import org.ntk.mutibo.android.model.Playable;
import org.ntk.mutibo.android.model.Playable.Type;
import org.ntk.mutibo.android.model.TaskType;
import org.ntk.mutibo.android.model.helpers.TestData;
import org.ntk.mutibo.api.MutiboApi;
import org.ntk.mutibo.json.ItemSet;
import org.ntk.mutibo.json.MutiboUser;

import android.graphics.drawable.Drawable;

/**
 * Central point of reference for game operations. Handles all connection with the server with the help of
 * CallableTasks. Abstracts all game functionality so that the connection to the server can be moved easily to a
 * different context (e.g. service)
 * 
 * @author Nick
 * 
 */
public class DemoGameManager implements GameManager {

	private DemoGame activeGame;
	private GameEventListener listener;

	/**
	 * singleton method
	 * 
	 * @param user
	 * 
	 * @return
	 */
	public DemoGameManager(MutiboApi service, GameEventListener listener, String user) {
		if (listener == null)
			throw new RuntimeException("The game event listener has not been initialized");

		this.listener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ntk.mutibo.android.model.GameManagerIntf#hasActiveGame()
	 */
	@Override
	public boolean hasActiveGame() {
		return activeGame != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ntk.mutibo.android.model.GameManagerIntf#startNewGame(org.ntk.mutibo.json.Game.Type, java.lang.String)
	 */
	@Override
	public void startNewGame(final Playable.Type type, String userStarting, final MutiboUser otherUser) {
		if (activeGame != null) {
			finishGame(activeGame); // TODO: check for side effects on the UI
		}
		activeGame = new DemoGame(TestData.initialSets());
		listener.onDemoGame(activeGame);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ntk.mutibo.android.model.GameManagerIntf#loadNextItem(org.ntk.mutibo.json.Game)
	 */
	@Override
	public void loadNextItem(final Playable game) {
		if (activeGame.hasItemSets()) {
			ItemSet next = activeGame.peekNextItemSet();
			listener.onNextItemset(next);
		} else {
			listener.onGameFinished(game);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ntk.mutibo.android.model.GameManagerIntf#answerItemSet(org.ntk.mutibo.json.Game,
	 * org.ntk.mutibo.json.ItemSet, int)
	 */
	@Override
	public void answerItemSet(final Playable game, final ItemSet itemset, final int answer, final Drawable answerBitmap) {
		if (activeGame.hasItemSets()) {
			ItemSet next = activeGame.peekNextItemSet();
			if (next != null && next.equals(itemset)) {
				activeGame.answer(itemset, answer);
				listener.onAnswerItemSet(itemset, answer, answerBitmap);
				activeGame.pollNextItemSet(); // remove the item
			} else {
				listener.onErrorFromTask(TaskType.ANSWER_ITEMSET);
			}
		} else {
			listener.onGameFinished(game);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ntk.mutibo.android.model.GameManagerIntf#finishGame(org.ntk.mutibo.json.Game)
	 */
	@Override
	public void finishGame(final Playable game) {
		this.activeGame = null;
		listener.onGameFinished(game);
		// no reason to throw error yet
		// listener.onErrorFromTask(TaskType.FINISH_GAME);
	}

	@Override
	public Playable getActiveGame() {
		return activeGame;
	}

	@Override
	public void likeItemSet(ItemSet itemSet) {
		// not implemented in demo mode
	}

	@Override
	public void dislikeItemSet(ItemSet itemSet) {
		// not implemented in demo mode
	}

	@Override
	public void getUserList() {
		// not implemented in demo mode
	}

	@Override
	public void requestGame(Type gameType, String fromUser, String forUser) {
		// not implemented in demo mode
	}

	@Override
	public void joinGame(long gameId, String username) {
		// not implemented in demo mode
	}
}
