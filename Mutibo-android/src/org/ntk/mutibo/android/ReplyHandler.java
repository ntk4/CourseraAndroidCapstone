package org.ntk.mutibo.android;

import java.util.List;

import org.ntk.mutibo.android.service.GameRequestService;
import org.ntk.mutibo.json.GameRequest;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

/**
 * @class ReplyHandler
 * 
 * @brief Receives the reply from the UniqueIDGeneratorService, which contains the unique ID.
 */
@SuppressLint("HandlerLeak")
public class ReplyHandler extends Handler {

	/**
	 * 
	 */
	private final MainActivity mainActivity;

	/**
	 * @param mainActivity
	 */
	ReplyHandler(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	/**
	 * Callback to handle the reply from the UniqueIDGeneratorService.
	 */
	@Override
	public void handleMessage(Message reply) {
		List<GameRequest> gameRequests = GameRequestService.unpackGameRequests(reply);

		for (GameRequest request : gameRequests) {

			Context ctx = this.mainActivity;
			Resources res = ctx.getResources();
			Notification.Builder builder = new Notification.Builder(ctx);

			Intent notificationIntent = new Intent(ctx, MainActivity.class);
			notificationIntent.putExtra("requestingUser", request.getRequestingUser());
			notificationIntent.putExtra("forUser", request.getForUser());
			notificationIntent.putExtra("requestId", request.getId());
			notificationIntent.putExtra("gameType", request.getType().toString());

			PendingIntent contentIntent = PendingIntent.getActivity(ctx, MainActivity.REQ_GAME_REQUEST,
					notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			builder.setContentIntent(contentIntent)
					.setSmallIcon(R.drawable.ic_launcher)
					.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
					// .setTicker(res.getString(R.string.your_ticker)).setWhen(System.currentTimeMillis())
					.setAutoCancel(true)
					.setContentText(
							String.format(res.getString(R.string.game_request_notification_text),
									request.getRequestingUser()))
					.setContentTitle(
							String.format(res.getString(R.string.game_request_notification_title), request.getType()
									.toString()));
			Notification n = builder.build();

			this.mainActivity.getNotificationManager().notify((int) request.getId(), n);
		}
	}
}