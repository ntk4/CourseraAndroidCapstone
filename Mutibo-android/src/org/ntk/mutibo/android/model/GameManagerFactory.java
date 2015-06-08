package org.ntk.mutibo.android.model;

import org.ntk.mutibo.android.model.Playable.Type;
import org.ntk.mutibo.android.model.impl.DefaultGameManager;
import org.ntk.mutibo.android.model.impl.DemoGameManager;
import org.ntk.mutibo.api.MutiboApi;

public class GameManagerFactory {

	public static GameManager getManager(Playable.Type type, MutiboApi service, GameEventListener listener, String user) {
		if (type == Type.DEMO) {
			return new DemoGameManager(service, listener, user);

		} else {
			return new DefaultGameManager(service, listener, user);
		}
	}
}
