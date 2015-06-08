package org.ntk.mutibo.android.helpers;

import java.util.EnumSet;
import java.util.List;

import android.app.Activity;

import com.amazon.ags.api.AGResponseCallback;
import com.amazon.ags.api.AGResponseHandle;
import com.amazon.ags.api.AmazonGamesCallback;
import com.amazon.ags.api.AmazonGamesClient;
import com.amazon.ags.api.AmazonGamesFeature;
import com.amazon.ags.api.AmazonGamesStatus;
import com.amazon.ags.api.leaderboards.GetLeaderboardsResponse;
import com.amazon.ags.api.leaderboards.Leaderboard;
import com.amazon.ags.api.leaderboards.LeaderboardsClient;
import com.amazon.ags.api.leaderboards.SubmitScoreResponse;
import com.amazon.ags.api.overlay.PopUpLocation;

/**
 * Helper class for all Amazon GameCircle operations
 * 
 * @author Nick
 * 
 */
public class GameCircleHelper {

	public static final String LEADERBOARD_ID = "Mutibo_leaderboard";

	// reference to the agsClient
	private AmazonGamesClient agsClient;
	private AmazonGamesCallback callback = null;
	private EnumSet<AmazonGamesFeature> myGameFeatures = null;

	private ScorePublishListener listener; // currently only one supported

	private static GameCircleHelper theInstance; // singleton instance

	/**
	 * Listener for the events of connection or score publishing on Amazon GameCircle
	 * 
	 * @author Nick
	 * 
	 */
	public interface ScorePublishListener {
		void onScorePublishSuccess(SubmitScoreResponse result);

		void onScorePublishError(SubmitScoreResponse result);

		void onLeaderboardsAcquired(List<Leaderboard> result);

		void onLeaderboardsError(GetLeaderboardsResponse result);
	}

	// make it inaccessible to the outside world
	private GameCircleHelper() {

	}

	public static GameCircleHelper instance() {
		if (theInstance == null) {
			theInstance = new GameCircleHelper();
		}
		return theInstance;
	}

	public void init(Activity activity, ScorePublishListener theListener) {

		this.listener = theListener;

		// list of features your game uses (in this example, achievements and leaderboards)
		myGameFeatures = EnumSet.of(AmazonGamesFeature.Leaderboards);

		callback = new AmazonGamesCallback() {
			@Override
			public void onServiceNotReady(AmazonGamesStatus status) {
				// unable to use service
			}

			@Override
			public void onServiceReady(AmazonGamesClient amazonGamesClient) {
				agsClient = amazonGamesClient;

				agsClient.setPopUpLocation(PopUpLocation.TOP_CENTER);
			}
		};

		AmazonGamesClient.initialize(activity, callback, myGameFeatures);
	}

	public void publishScore(String user, long score) {
		if (agsClient == null) {
			// listener.onScorePublishError(new SubmitScoreResponseImpl(0, ErrorCode.SERVICE_NOT_READY));
			return;
		}
		// Replace YOUR_LEADERBOARD_ID with an actual leaderboard ID from your game.
		LeaderboardsClient lbClient = agsClient.getLeaderboardsClient();
		AGResponseHandle<SubmitScoreResponse> handle = lbClient.submitScore(LEADERBOARD_ID, score, user);

		// Optional callback to receive notification of success/failure.
		handle.setCallback(new AGResponseCallback<SubmitScoreResponse>() {

			@Override
			public void onComplete(SubmitScoreResponse result) {
				if (result.isError()) {
					// Add optional error handling here. Not strictly required
					// since retries and on-device request caching are automatic.
					if (listener != null)
						listener.onScorePublishError(result);
				} else {
					if (listener != null)
						listener.onScorePublishSuccess(result);
					// Continue game flow.
				}
			}
		});
	}

	public void getLeaderboards() {
		if (agsClient == null) {
			return; // TODO: log something
		}
		LeaderboardsClient lbClient = agsClient.getLeaderboardsClient();
		if (lbClient != null)
			lbClient.getLeaderboards().setCallback(new AGResponseCallback<GetLeaderboardsResponse>() {

				@Override
				public void onComplete(GetLeaderboardsResponse result) {
					if (result.isError()) {
						// Handle getLeaderboards error

						if (listener != null)
							listener.onLeaderboardsError(result);
					} else {
						List<Leaderboard> leaderboards = result.getLeaderboards();

						if (listener != null)
							listener.onLeaderboardsAcquired(leaderboards);
					}
				}
			});
	}

	public void showLeaderboardsOverlay() {
		if (agsClient != null) {
			LeaderboardsClient lbClient = agsClient.getLeaderboardsClient();
			if (lbClient != null)
				lbClient.showLeaderboardsOverlay();
		}
	}

	public void release() {
		if (agsClient != null) {
			AmazonGamesClient.release();
		}
	}

	public void shutdown() {
		if (agsClient != null) {
			AmazonGamesClient.shutdown();
		}
	}
}
