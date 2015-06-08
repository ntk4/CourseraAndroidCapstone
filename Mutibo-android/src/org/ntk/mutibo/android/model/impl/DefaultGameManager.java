package org.ntk.mutibo.android.model.impl;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.Callable;

import org.ntk.mutibo.android.model.DifficultyScoreThresholds;
import org.ntk.mutibo.android.model.GameEventListener;
import org.ntk.mutibo.android.model.GameManager;
import org.ntk.mutibo.android.model.Playable;
import org.ntk.mutibo.android.model.Playable.Type;
import org.ntk.mutibo.android.model.TaskType;
import org.ntk.mutibo.api.MutiboApi;
import org.ntk.mutibo.async.CallableTask;
import org.ntk.mutibo.async.TaskCallback;
import org.ntk.mutibo.json.Game;
import org.ntk.mutibo.json.GameRequest;
import org.ntk.mutibo.json.GameSet;
import org.ntk.mutibo.json.ItemSet;
import org.ntk.mutibo.json.ItemSetDifficulty;
import org.ntk.mutibo.json.MutiboUser;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.common.collect.Lists;

/**
 * Central point of reference for game operations. Handles all connection with the server with the help of
 * CallableTasks. Abstracts all game functionality so that the connection to the server can be moved easily to a
 * different context (e.g. service) if needed.
 * 
 * Realizes also the game mechanics such as difficulty levels.
 * 
 * @author Nick
 * 
 */
public class DefaultGameManager implements GameManager {
	
	private String user;
	private Game activeGame;
	private ItemSetDifficulty activeGameDifficulty;
	private MutiboApi theService;
	private GameEventListener listener;
	

	private ArrayDeque<ItemSet> buffer;

	/**
	 * singleton method
	 * 
	 * @return
	 */
	public DefaultGameManager(MutiboApi service, GameEventListener listener, String user) {
		if (service == null)
			throw new RuntimeException("The server connection has not been initialized");

		if (listener == null)
			throw new RuntimeException("The game event listener has not been initialized");

		this.listener = listener;
		this.user = user;
		this.theService = service;

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
		CallableTask.invoke(new Callable<Game>() {

			@Override
			public Game call() throws Exception {
				activeGame = theService.startGame(type, user, otherUser.getUsername());
				ItemSet firstItemset = theService.getItemSet(activeGame.getGameSets().get(0).getSetId());
				activeGame.setFirstQuestion(firstItemset);
				activeGameDifficulty = ItemSetDifficulty.getEasiest();
				buffer = new ArrayDeque<ItemSet>();
				return activeGame;
			}
		}, new TaskCallback<Game>() {

			@Override
			public void success(final Game result) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						listener.onGameStarted(result, activeGame.getFirstQuestion());
					}
				});
			}

			@Override
			public void error(Exception e) {
				Log.e(DefaultGameManager.class.getName(), "Error starting new game", e);

				listener.onErrorFromTask(TaskType.START_GAME);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ntk.mutibo.android.model.GameManagerIntf#loadNextItem(org.ntk.mutibo.json.Game)
	 */
	@Override
	public void loadNextItem(final Playable game) {
		CallableTask.invoke(new Callable<ItemSet>() {

			@Override
			public ItemSet call() throws Exception {

				return internalLoadNextItem(game);
			}
		}, new TaskCallback<ItemSet>() {

			@Override
			public void success(final ItemSet result) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						listener.onNextItemset(result);
					}
				});
			}

			@Override
			public void error(Exception e) {
				Log.e(DefaultGameManager.class.getName(), "Error loading next set", e);

				listener.onErrorFromTask(TaskType.NEXT_ITEMSET);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ntk.mutibo.android.model.GameManagerIntf#answerItemSet(org.ntk.mutibo.json.Game,
	 * org.ntk.mutibo.json.ItemSet, int)
	 */
	@Override
	public void answerItemSet(final Playable game, final ItemSet itemset, final int answer, final Drawable answerBitmap) {
		CallableTask.invoke(new Callable<ItemSet>() {

			@Override
			public ItemSet call() throws Exception {
				theService.answerItemSet(activeGame.getId(), itemset.getId(), answer);
				return itemset;
			}
		}, new TaskCallback<ItemSet>() {

			@Override
			public void success(final ItemSet result) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						game.answer(itemset, answer);
						listener.onAnswerItemSet(result, answer, answerBitmap);
					}
				});
			}

			@Override
			public void error(Exception e) {

				Log.e(DefaultGameManager.class.getName(), "Error answering set", e);

				listener.onErrorFromTask(TaskType.ANSWER_ITEMSET);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ntk.mutibo.android.model.GameManagerIntf#finishGame(org.ntk.mutibo.json.Game)
	 */
	@Override
	public void finishGame(final Playable game) {
		CallableTask.invoke(new Callable<Game>() {

			@Override
			public Game call() throws Exception {
				theService.finishGame(activeGame.getId());
				return activeGame;
			}
		}, new TaskCallback<Game>() {

			@Override
			public void success(final Game result) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						DefaultGameManager.this.activeGame = null;
						listener.onGameFinished(result);
					}
				});
			}

			@Override
			public void error(Exception e) {
				Log.e(DefaultGameManager.class.getName(), "Error finishing game", e);

				listener.onErrorFromTask(TaskType.FINISH_GAME);
			}
		});
	}

	@Override
	public Playable getActiveGame() {
		return activeGame;
	}

	@Override
	public void likeItemSet(final ItemSet itemSet) {
		CallableTask.invoke(new Callable<ItemSet>() {

			@Override
			public ItemSet call() throws Exception {
				theService.likeItemSet(itemSet.getId());
				return itemSet;
			}
		}, new TaskCallback<ItemSet>() {

			@Override
			public void success(final ItemSet result) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						listener.onItemSetLiked(result);
					}
				});
			}

			@Override
			public void error(Exception e) {
				Log.e(DefaultGameManager.class.getName(), "Error liking itemset", e);

				listener.onErrorFromTask(TaskType.LIKE_ITEMSET);
			}
		});
	}

	@Override
	public void dislikeItemSet(final ItemSet itemSet) {
		CallableTask.invoke(new Callable<ItemSet>() {

			@Override
			public ItemSet call() throws Exception {
				theService.dislikeItemSet(itemSet.getId());
				return itemSet;
			}
		}, new TaskCallback<ItemSet>() {

			@Override
			public void success(final ItemSet result) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						listener.onItemSetLiked(result);
					}
				});
			}

			@Override
			public void error(Exception e) {
				Log.e(DefaultGameManager.class.getName(), "Error disliking itemset", e);

				listener.onErrorFromTask(TaskType.DISLIKE_ITEMSET);
			}
		});
	}

	private void internalBufferOneItemSet(final Playable game, final ItemSetDifficulty difficulty) {
		CallableTask.invoke(new Callable<ItemSet>() {

			@Override
			public ItemSet call() throws Exception {
				return theService.getNextItemSet(activeGame.getId(), difficulty.getLevel());
			}
		}, new TaskCallback<ItemSet>() {

			@Override
			public void success(final ItemSet result) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						buffer.add(result);
					}
				});
			}

			@Override
			public void error(Exception e) {
				Log.e(DefaultGameManager.class.getName(), "Error buffering ItemSet", e);
			}
		});
	}

	@Override
	public void getUserList() {
		CallableTask.invoke(new Callable<List<MutiboUser>>() {

			@Override
			public List<MutiboUser> call() throws Exception {
				return Lists.newArrayList(theService.getUsers());
			}
		}, new TaskCallback<List<MutiboUser>>() {

			@Override
			public void success(final List<MutiboUser> result) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						listener.onUsersLoaded(result);
					}
				});
			}

			@Override
			public void error(Exception e) {
				Log.e(DefaultGameManager.class.getName(), "Error loading users", e);

				listener.onErrorFromTask(TaskType.LOAD_USERS);
			}
		});
	}

	@Override
	public void requestGame(final Type type, final String fromUser, final String forUser) {
		CallableTask.invoke(new Callable<GameRequest>() {

			@Override
			public GameRequest call() throws Exception {
				return theService.requestGame(type, fromUser, forUser);
			}
		}, new TaskCallback<GameRequest>() {

			@Override
			public void success(final GameRequest result) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						listener.onGameRequested(result);
					}
				});
			}

			@Override
			public void error(Exception e) {
				Log.e(DefaultGameManager.class.getName(), "Error requesting game", e);

				listener.onErrorFromTask(TaskType.REQUEST_GAME);
			}
		});
	}

	@Override
	public void joinGame(final long gameId, final String username) {
		CallableTask.invoke(new Callable<Game>() {

			@Override
			public Game call() throws Exception {
				Game game = theService.joinGame(gameId, username);
				if (game != null) {
					activeGame = game;
					activeGameDifficulty = ItemSetDifficulty.getEasiest();

					buffer = new ArrayDeque<ItemSet>();
					// resolve first questions
					for (GameSet gameSet : game.getGameSets()) {
						ItemSet itemSet = theService.getItemSet(gameSet.getSetId());
						buffer.add(itemSet);
						game.setFirstQuestion(buffer.peek());
					}
				}
				return game;
			}
		}, new TaskCallback<Game>() {

			@Override
			public void success(final Game result) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						listener.onGameJoined(result);
					}
				});
			}

			@Override
			public void error(Exception e) {
				Log.e(DefaultGameManager.class.getName(), "Error requesting game", e);

				listener.onErrorFromTask(TaskType.REQUEST_GAME);
			}
		});
	}

	/**
	 * Evaluates the difficulty level that the next question should have Depends only on the score, we could easily
	 * invent a more sophisticated algorithm
	 * 
	 * @param game
	 *            The game to be evaluated
	 * @return the difficulty
	 */
	protected ItemSetDifficulty evaluateDifficulty(Game game) {
		if (game.getScore() < DifficultyScoreThresholds.VERY_EASY.getScore())
			return ItemSetDifficulty.VERY_EASY;
		else if (game.getScore() < DifficultyScoreThresholds.EASY.getScore())
			return ItemSetDifficulty.EASY;
		else if (game.getScore() < DifficultyScoreThresholds.NORMAL.getScore())
			return ItemSetDifficulty.NORMAL;
		else if (game.getScore() < DifficultyScoreThresholds.HARD.getScore())
			return ItemSetDifficulty.HARD;
		else
			return ItemSetDifficulty.EXTREME;
	}

	private ItemSet internalLoadNextItem(final Playable game) {
		activeGameDifficulty = evaluateDifficulty(activeGame);
		if (buffer.isEmpty()) {

			ItemSet itemSet = theService.getNextItemSet(activeGame.getId(), activeGameDifficulty.getLevel());
			game.addSet(itemSet);
			internalBufferOneItemSet(game, activeGameDifficulty); // load asynchronously the next item
			return itemSet;
		} else {
			ItemSet bufferedItem = buffer.poll();
			internalBufferOneItemSet(game, activeGameDifficulty); // load asynchronously the next item
			return bufferedItem;
		}
	}

}
