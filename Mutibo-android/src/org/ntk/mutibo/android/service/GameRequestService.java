package org.ntk.mutibo.android.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.ntk.mutibo.api.MutiboApi;
import org.ntk.mutibo.client.MutiboClientSvc;
import org.ntk.mutibo.json.GameRequest;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.google.common.collect.Lists;

/**
 * @class GameRequestService
 * 
 * @brief This Service generates unique IDs within a pool of Threads. When it is created, it creates a
 *        ThreadPoolExecutor using the newFixedThreadPool() method of the Executors class.
 * 
 *        This class implements the Synchronous Service layer of the Half-Sync/Half-Async pattern. It also implements a
 *        variant of the Factory Method pattern.
 */
public class GameRequestService extends Service {
	/**
	 * Used for debugging.
	 */
	private final String TAG = getClass().getName();

	/**
	 * A class constant that determines the maximum number of threads used to service download requests.
	 */
	private final int MAX_THREADS = 4;

	/**
	 * The ExecutorService implementation that references a ThreadPool.
	 */
	private ExecutorService mExecutor;

	/**
	 * A Messenger that encapsulates the RequestHandler used to handle request Messages sent from the activity
	 */
	private Messenger mReqMessenger = null;

	/**
	 * Hook method called when the Service is created.
	 */
	@Override
	public void onCreate() {
		// A Messenger that encapsulates the RequestHandler used to
		// handle request Messages sent from the activity
		mReqMessenger = new Messenger(new RequestHandler());

		// Create a FixedThreadPool Executor that's configured to use MAX_THREADS.
		mExecutor = Executors.newFixedThreadPool(MAX_THREADS);
	}

	/**
	 * Factory method to make the desired Intent.
	 */
	public static Intent makeIntent(Context context) {
		// Create the Intent that's associated to the
		// GameRequestService class.
		return new Intent(context, GameRequestService.class);
	}

	/**
	 * @class RequestHandler
	 * 
	 * @brief This class handles messages sent by the UniqueIDGeneratorActivity.
	 */
	@SuppressLint("HandlerLeak")
	private class RequestHandler extends Handler {
		/**
		 * Return a Message containing an ID that's unique system-wide.
		 */
		// private Message generateUniqueID() {
		// String uniqueID;
		//
		// // Protect critical section to ensure the IDs are unique.
		// synchronized (this) {
		// // This loop keeps generating a random UUID if it's
		// // not unique (i.e., is not currently found in the
		// // persistent collection of SharedPreferences). The
		// // likelihood of a non-unique UUID is low, but we're
		// // being extra paranoid for the sake of this example
		// // ;-)
		// // do {
		// // uniqueID = UUID.randomUUID().toString();
		// // } while (uniqueIDs.getInt(uniqueID, 0) == 1);
		//
		// // We found a unique ID, so add it as the "key" to the
		// // persistent collection of SharedPreferences, with a
		// // value of 1 to indicate this ID is already "used".
		// // SharedPreferences.Editor editor = uniqueIDs.edit();
		// // editor.putInt(uniqueID, 1);
		// // editor.commit();
		// }
		//
		// // Create a Message that's used to send the unique ID back
		// // to the UniqueIDGeneratorActivity.
		// Message reply = Message.obtain();
		// Bundle data = new Bundle();
		// data.putString("ID", "value");
		// reply.setData(data);
		// return reply;
		// }

		// Hook method called back when a request Message arrives from
		// the UniqueIDGeneratorActivity. The message it receives
		// contains the Messenger used to reply to the Activity.
		@Override
		public void handleMessage(Message request) {

			// Store the reply Messenger so it doesn't change out from
			// underneath us.
			final Messenger replyMessenger = request.replyTo;

			if (request.getData().getString("user") == null) {
				try {
					Log.e(TAG, "GameRequestService was called without login information, aborting...");
					replyMessenger.send(Message.obtain());
					return;
				} catch (RemoteException e) {
					Log.e(TAG, "GameRequestService was unable reply with empty message");
					return;
				}
			}

			final String username = request.getData().getString("user");
			final String server = request.getData().getString("server");
			final String pass = request.getData().getString("pass");
			Log.i(TAG, "handler info= " + username + "," + pass + "," + server);
			final MutiboApi svc = MutiboClientSvc.init(server, username, pass);
			Log.i(TAG, "Service resolved? " + (svc != null));
			// Put a runnable that generates a unique ID into the
			// thread pool for subsequent concurrent processing.
			try {
			mExecutor.execute(new Runnable() {
				public void run() {

					try {
						// Send the reply back to the
						// UniqueIDGeneratorActivity.
						// Log.d(TAG, "replyMessenger = " + replyMessenger.hashCode());
						// try { Thread.sleep (10000); } catch (InterruptedException e) {}
						Log.i(TAG, "before calling getPendingGameRequests");
						Collection<GameRequest> gameRequests = svc.getPendingGameRequests(username);
						Log.i(TAG, "after calling getPendingGameRequests. Result size = "
								+ (gameRequests != null ? gameRequests.size() : -1));
						Message reply = Message.obtain();
						Bundle data = new Bundle();
						data.putSerializable("gameRequests", new ArrayList<GameRequest>(gameRequests));
						reply.setData(data);

						replyMessenger.send(reply);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			});
			} catch(RejectedExecutionException e) {
				Log.e(TAG, "Service thread execution was rejected, error: " + e.getMessage());
			}
		}
	}

	/**
	 * Called when the service is destroyed, which is the last call the Service receives informing it to clean up any
	 * resources it holds.
	 */
	@Override
	public void onDestroy() {
		// Ensure that the threads used by the ThreadPoolExecutor
		// complete and are reclaimed by the system.

		mExecutor.shutdown();
	}

	/**
	 * Factory method that returns the underlying IBinder associated with the Request Messenger.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mReqMessenger.getBinder();
	}

	@SuppressWarnings("unchecked")
	public static List<GameRequest> unpackGameRequests(Message reply) {
		if (reply != null && reply.getData() != null && reply.getData().getSerializable("gameRequests") != null) {
			return (List<GameRequest>) reply.getData().getSerializable("gameRequests");
		} else {
			return Lists.newArrayList();
		}
	}
}
