package org.ntk.mutibo.android;

import java.util.ArrayList;
import java.util.List;

import org.ntk.mutibo.android.adapter.NavDrawerListAdapter;
import org.ntk.mutibo.android.helpers.GameCircleHelper;
import org.ntk.mutibo.android.helpers.GameCircleHelper.ScorePublishListener;
import org.ntk.mutibo.android.helpers.WaitDialog;
import org.ntk.mutibo.android.helpers.googleplus.GooglePlusFragment;
import org.ntk.mutibo.android.helpers.googleplus.GooglePlusHelper;
import org.ntk.mutibo.android.model.DemoGame;
import org.ntk.mutibo.android.model.GameEventListener;
import org.ntk.mutibo.android.model.GameManager;
import org.ntk.mutibo.android.model.GameManagerFactory;
import org.ntk.mutibo.android.model.LoginInfo;
import org.ntk.mutibo.android.model.NavDrawerItem;
import org.ntk.mutibo.android.model.Playable;
import org.ntk.mutibo.android.model.Playable.Type;
import org.ntk.mutibo.android.model.TaskType;
import org.ntk.mutibo.android.model.helpers.AuthenticationChecker;
import org.ntk.mutibo.android.model.impl.DemoGameManager;
import org.ntk.mutibo.android.service.GameRequestService;
import org.ntk.mutibo.api.MutiboApi;
import org.ntk.mutibo.json.Game;
import org.ntk.mutibo.json.GameRequest;
import org.ntk.mutibo.json.ItemSet;
import org.ntk.mutibo.json.MutiboUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.amazon.ags.api.leaderboards.GetLeaderboardsResponse;
import com.amazon.ags.api.leaderboards.Leaderboard;
import com.amazon.ags.api.leaderboards.SubmitScoreResponse;

public class MainActivity extends Activity implements GameEventListener, OnGesturePerformedListener,
		ScorePublishListener {

	private final String TAG = getClass().getName();

	private static final int NAV_DRAWER_POS_SIGNIN = 0;
	private static final int NAV_DRAWER_POS_QUESTION = 1;
	private static final int NAV_DRAWER_POS_VENDETTA = 2;
	private static final int NAV_DRAWER_POS_GANG = 3;
	private static final int NAV_DRAWER_POS_HOT_SCORES = 4;
	private static final int NAV_DRAWER_POS_OPTIONS = 5;
	private static final int NAV_DRAWER_POS_EXPLANATION = 10;
	private static final int NAV_DRAWER_POS_SIGNUP = 11;

	public static final int REQ_GAME_REQUEST = 0;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;

	private LoginFragment loginFragment = null;
	private QuestionFragment questionFragment = null;
	private ExplanationFragment explanationFragment = null;
	// private GangFragment gangFragment = null;
	// private HotScoresFragment hotScoresFragment = null; // Not used, showing GameCircle Leaderboards pop up instead
	private OptionsFragment optionsFragment = null;
	private PickUserFragment pickUserFragment = null;
	private SignupFragment signupFragment = null;
	private GooglePlusFragment googlePlusFragment = null;

	private LoginInfo login;
	private MutiboApi svc;

	private GameManager gameManager;

	private GestureLibrary mLibrary;

	/**
	 * Reference to the request Messenger that's implemented in the GameRequestService
	 */
	private Messenger mReqMessengerRef = null;

	private Messenger mReplyMessenger = new Messenger(new ReplyHandler(this));

	private NotificationManager mNotificationManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		initializeNavigationDrawer();

		if (savedInstanceState == null) {
			// on first time display view for first nav item
			displayView(0);
		}

		initializeOptionalFunctionality();

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		WaitDialog.init(this);
	}

	private void initializeOptionalFunctionality() {

		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);

		Boolean useGestures = p.getBoolean("pref_gestures", true);
		if (useGestures)
			initializeGestures();

		initGameCircle();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "calling bindService()");
		if (mReqMessengerRef == null)
			// Bind to the UniqueIDGeneratorService associated with this
			// Intent.
			bindService(GameRequestService.makeIntent(this), mSvcConn, Context.BIND_AUTO_CREATE);
	}

	/**
	 * Hook method called by Android when this Activity becomes invisible.
	 */
	@Override
	protected void onStop() {
		// Unbind from the Service.
		if (mSvcConn != null && mReqMessengerRef != null)
			try {
				unbindService(mSvcConn);
			} catch (Exception e) {
				Log.w(TAG, "Could not unbind service in the onStop() method. Was the service was already stopped?");
			}

		try {
			GameCircleHelper.instance().shutdown();
		} catch (Exception e) {
			Log.w(TAG, "Could not shutdown gamecircle in the onStop() method. Was it already disconnected?");
		}

		super.onStop();
	}
	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		GooglePlusHelper.instance().handleActivityResult(requestCode, resultCode, data);
	}

	/**
	 * This ServiceConnection is used to receive a Messenger reference after binding to the UniqueIDGeneratorService
	 * using bindService().
	 */
	private ServiceConnection mSvcConn = new ServiceConnection() {
		/**
		 * Called after the UniqueIDGeneratorService is connected to convey the result returned from onBind().
		 */
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "ComponentName:" + className);

			// Create a new Messenger that encapsulates the
			// returned IBinder object and store it for later use
			// in mReqMessengerRef.
			mReqMessengerRef = new Messenger(binder);
		}

		/**
		 * Called if the Service crashes and is no longer available. The ServiceConnection will remain bound, but the
		 * Service will not respond to any requests.
		 */
		public void onServiceDisconnected(ComponentName className) {
			mReqMessengerRef = null;
		}
	};

	private void initializeGestures() {
		mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!mLibrary.load()) {
			finish();
		}

		GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gesturesOverlay);
		gestures.addOnGesturePerformedListener(this);

		gestures.cancelClearAnimation();
		gestures.clear(true);
		gestures.setFadeEnabled(true);

		gestures.setWillNotDraw(true);
		gestures.setAlpha(1);
	}

	private void initializeNavigationDrawer() {
		navDrawerItems = new ArrayList<NavDrawerItem>();

		for (int i = 0; i < 6; i++) {
			// adding nav drawer items to array
			navDrawerItems.add(new NavDrawerItem(navMenuTitles[i], navMenuIcons.getResourceId(i, -1)));
		}

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for accessibility
				R.string.app_name // nav drawer close - description for accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && gameManager != null && gameManager.hasActiveGame()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.on_back_button_title);
			builder.setMessage(R.string.on_back_button_message);
			builder.setPositiveButton(R.string.yes, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			builder.setNegativeButton(R.string.no, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			builder.show();
			return true;
		}
		finish();
		return true;
	}

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			warnAbortActiveGame(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {

		if (position == NAV_DRAWER_POS_HOT_SCORES) {
			GameCircleHelper.instance().showLeaderboardsOverlay();
			return;
		}
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case NAV_DRAWER_POS_SIGNIN:
			if (loginFragment == null)
				loginFragment = new LoginFragment();
			fragment = loginFragment;
			break;
		case NAV_DRAWER_POS_QUESTION:
			if (questionFragment == null)
				questionFragment = new QuestionFragment();
			fragment = questionFragment;
			break;
		case NAV_DRAWER_POS_VENDETTA:
			if (pickUserFragment == null)
				pickUserFragment = new PickUserFragment();
			pickUserFragment.setGameType(Type.VENDETTA);
			fragment = pickUserFragment;
			break;
		case NAV_DRAWER_POS_EXPLANATION:
			// if (explanationFragment == null)
			explanationFragment = new ExplanationFragment();
			fragment = explanationFragment;
			break;
		case NAV_DRAWER_POS_GANG:
			if (pickUserFragment == null)
				pickUserFragment = new PickUserFragment();
			pickUserFragment.setGameType(Type.GANG);
			fragment = pickUserFragment;
			break;
		case NAV_DRAWER_POS_OPTIONS:
			if (optionsFragment == null)
				optionsFragment = new OptionsFragment();
			fragment = optionsFragment;
			break;
		case NAV_DRAWER_POS_SIGNUP:
			if (signupFragment == null)
				signupFragment = new SignupFragment();
			fragment = signupFragment;
		default:
			break;
		}

		switchToFragment(position, fragment);
	}

	private void switchToFragment(int position, Fragment fragment) {
		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			if (gameManager != null && gameManager.hasActiveGame())
				setTitle(gameManager.getActiveGame().getType().toString());
			else if (navMenuTitles.length > position)
				setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ntk.mutibo.android.GameEventListener#handleErrorFromTask(org.ntk.mutibo.android.model.FragmentTask)
	 */
	@Override
	public void onErrorFromTask(TaskType taskWithErrorResult) {
		String message = "Error";

		switch (taskWithErrorResult) {
		case ANSWER_ITEMSET:
			message = "Error answering set";
			break;
		case FINISH_GAME:
			message = "Error finishing game";
			break;
		case NEXT_ITEMSET:
			message = "Error loading next set";
			break;
		case START_GAME:
			message = "Error starting new game";
			break;
		case RETRIEVE_DEMO:
			message = "Error retrieving demo game";
			break;
		case LOGIN:
			message = "Login failed, check your Internet connection, credentials or that the server is running";
			break;
		case START_DEMO_GAME:
			message = "Error starting demo game";
			break;
		case REGISTER_USER:
			message = "Error registering user";
			break;
		case DISLIKE_ITEMSET:
			message = "Error disliking item set";
			break;
		case LIKE_ITEMSET:
			message = "Error liking item set";
			break;
		case LOAD_USERS:
			message = "Error loading users list";
			break;
		default:
			break;
		}
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ntk.mutibo.android.GameEventListener#handleLoginSuccess(org.ntk.mutibo.api.MutiboApi)
	 */
	@Override
	public void onLoginSuccess(LoginInfo result) {
		if (result != null) {
			setLogin(result);
			this.svc = result.getService();
			Toast.makeText(this, "Logged in! Now please choose a game type", Toast.LENGTH_SHORT).show();
			mDrawerLayout.openDrawer(Gravity.LEFT);

			askServiceForGameRequests();
		} else {
			Toast.makeText(
					this,
					"Connection to the server has failed, please check your Internet connection "
							+ "and that the server is running", Toast.LENGTH_SHORT).show();
		}
	}

	public MutiboApi getService() {
		return svc;
	}

	public LoginInfo getLogin() {
		return login;
	}

	public void setLogin(LoginInfo login) {
		this.login = login;
	}

	public GameManager getGameManager() {
		return gameManager;
	}

	public void setGameManager(GameManager gameManager) {
		this.gameManager = gameManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ntk.mutibo.android.GameEventListener#handleGameStarted(org.ntk.mutibo.json.Game)
	 */
	@Override
	public void onGameStarted(Playable result, ItemSet firstQuestion) {
		displayStartGameUI(result, firstQuestion);
	}

	private void displayStartGameUI(Playable result, ItemSet firstQuestion) {
		displayView(NAV_DRAWER_POS_QUESTION);
		questionFragment.setGame(result);
		questionFragment.loadItemSet(firstQuestion);
		questionFragment.displayItemSet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ntk.mutibo.android.GameEventListener#handleGameFinished(org.ntk.mutibo.json.Game)
	 */
	@Override
	public void onGameFinished(Playable result) {
		Toast.makeText(this, "Game finished! final score: " + result.getScore(), Toast.LENGTH_SHORT).show();
		// mDrawerLayout.openDrawer(Gravity.LEFT);
		if (questionFragment != null)
			questionFragment.cleanUp();

		if (login != null) // logged in, therefore not a demo
			GameCircleHelper.instance().publishScore(login.getUsername(), result.getScore());
	}

	@Override
	public void onNextItemset(ItemSet result) {
		displayView(NAV_DRAWER_POS_QUESTION);

		if (questionFragment != null) {

			questionFragment.setGame(gameManager.getActiveGame());
			questionFragment.loadItemSet(result);
			questionFragment.displayItemSet();
		}
	}

	@Override
	public void onAnswerItemSet(ItemSet result, int answer, Drawable answerBitmap) {
		displayView(NAV_DRAWER_POS_EXPLANATION);
		explanationFragment.setAnswerBitmap(answerBitmap);
		explanationFragment.loadItemSet(gameManager.getActiveGame(), result, answer);
	}

	@Override
	public void onDemoGame(Playable game) {
		displayView(NAV_DRAWER_POS_QUESTION);
		questionFragment.startDemo((DemoGame) game);
	}

	@Override
	public void onItemSetLiked(ItemSet result) {
		Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
		gameManager.loadNextItem(gameManager.getActiveGame());
	}

	@Override
	public void onItemSetDisliked(ItemSet result) {
		Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
		gameManager.loadNextItem(gameManager.getActiveGame());
	}

	@Override
	public void onGesturePerformed(GestureOverlayView view, Gesture gesture) {
		// when a gesture is still on screen and we try another, we get a null here
		if (gesture == null)
			return;

		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);

		if (predictions.size() > 0 && predictions.get(0).score > 3.0) {
			String result = predictions.get(0).name;

			if ("solo".equalsIgnoreCase(result)) {
				warnAbortActiveGame(NAV_DRAWER_POS_QUESTION);
			} else if ("vendetta".equalsIgnoreCase(result)) {
				warnAbortActiveGame(NAV_DRAWER_POS_VENDETTA);
			} else if ("gang".equalsIgnoreCase(result)) {
				warnAbortActiveGame(NAV_DRAWER_POS_GANG);
			}
		}
	}

	@Override
	public void onUsersLoaded(List<MutiboUser> result) {
		if (pickUserFragment != null) {

			// remove the currently logged in user
			MutiboUser currentUser = null;
			for (MutiboUser user : result) {
				if (login.getUsername().equalsIgnoreCase(user.getUsername())) {
					currentUser = user;
				}
			}
			if (currentUser != null)
				result.remove(currentUser);

			pickUserFragment.initializeUserList(result);
		}
	}

	@Override
	public void onSignUp(String server) {
		displayView(NAV_DRAWER_POS_SIGNUP);
		signupFragment.setServer(server);
	}

	@Override
	public void onUserSelected(Type gameType, MutiboUser user) {
		gameManager.requestGame(gameType, login.getUsername(), user.getUsername());
	}

	private void warnAbortActiveGame(final int position) {

		if (gameManager != null && gameManager.hasActiveGame()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle(R.string.on_back_button_title);
			builder.setMessage(R.string.on_abort_game_message);
			builder.setPositiveButton(R.string.yes, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					gameManager.finishGame(gameManager.getActiveGame());
					// gameManager.startNewGame(type, login.getUsername(), null);
					// display view for selected nav drawer item
					navigationOptionClicked(position);
				}
			});
			builder.setNegativeButton(R.string.no, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mDrawerLayout.closeDrawer(mDrawerList);
				}
			});
			builder.show();
		} else {

			navigationOptionClicked(position);
		}

	}

	private void navigationOptionClicked(final int position) {
		if (position == NAV_DRAWER_POS_QUESTION) {
			if (!AuthenticationChecker.checkValidSession(login)) {
				redirectToLoginPage();
				return;
			}

			getGameManager(Type.SOLO);
			MutiboUser otherUser = new MutiboUser();
			otherUser.setUsername("");
			gameManager.startNewGame(Type.SOLO, login.getUsername(), otherUser);
		} else if (position == NAV_DRAWER_POS_VENDETTA) {
			if (!AuthenticationChecker.checkValidSession(login)) {
				redirectToLoginPage();
				return;
			}

			getGameManager(Type.VENDETTA);
			gameManager.getUserList();
			if (pickUserFragment == null) {
				pickUserFragment = new PickUserFragment();
			}
			pickUserFragment.setGameType(Type.VENDETTA);
			switchToFragment(position, pickUserFragment);
		} else if (position == NAV_DRAWER_POS_GANG) {
			if (!AuthenticationChecker.checkValidSession(login)) {
				redirectToLoginPage();
				return;
			}
			getGameManager(Type.GANG);
			gameManager.getUserList();
			if (pickUserFragment == null)
				pickUserFragment = new PickUserFragment();
			pickUserFragment.setGameType(Type.GANG);
			switchToFragment(position, pickUserFragment);
		} else {
			displayView(position);
		}
	}

	private void redirectToLoginPage() {
		displayView(NAV_DRAWER_POS_SIGNIN);
	}

	/**
	 * Called by Android when the user presses the "Generate Unique ID" button to request a new unique ID from the
	 * UniqueIDGeneratorService.
	 */
	public void askServiceForGameRequests() {
		// Create a request Message that indicates the Service should
		// send the reply back to ReplyHandler encapsulated by the
		// Messenger.
		Message request = Message.obtain();
		Bundle data = new Bundle();
		data.putString("server", login.getServerAddress());
		data.putString("user", login.getUsername());
		data.putString("pass", login.getPassword());
		request.setData(data);
		request.replyTo = mReplyMessenger;
		// Log.d(TAG, "mReplyMessenger = " + mReplyMessenger.hashCode());
		try {
			if (mReqMessengerRef != null) {
				Log.d(TAG, "sending message");
				mReqMessengerRef.send(request);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUserRegistered(MutiboUser result) {
		Toast.makeText(this, String.format("User %s was registered", result.getUsername()), Toast.LENGTH_SHORT).show();
		displayView(NAV_DRAWER_POS_SIGNIN);
	}

	@Override
	public void onResume() {
		super.onResume();

		initGameCircle();
	}

	private void initGameCircle() {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
		Boolean useGamecircle = p.getBoolean("pref_gameCircle", true);
		if (useGamecircle)
			GameCircleHelper.instance().init(this, this);
	}

	@Override
	public void onPause() {
		super.onPause();
		GameCircleHelper.instance().release();
	}

	@Override
	public void onScorePublishSuccess(SubmitScoreResponse result) {
		Toast.makeText(
				this,
				String.format("Score for user %s was synchronized with Amazon GameCircle!",
						(result.getUserData() != null && result.getUserData().length > 0 ? result.getUserData()[0]
								: login.getUsername())), Toast.LENGTH_LONG).show();
	}

	@Override
	public void onScorePublishError(SubmitScoreResponse result) {
		Toast.makeText(
				this,
				String.format("Warning: score for user %s could not be synchronized with Amazon GameCircle! "
						+ "Please make sure you have logged in with your amazon account", (result.getUserData() != null
						&& result.getUserData().length > 0 ? result.getUserData()[0] : login.getUsername())),
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onGameRequested(GameRequest result) {
		Toast.makeText(
				this,
				String.format("The game has been requested! User %s will be informed soon, please wait",
						result.getForUser()), Toast.LENGTH_LONG).show();
		WaitDialog.show();
	}

	@Override
	public void onLeaderboardsAcquired(List<Leaderboard> leaderboards) {
		// if (hotScoresFragment != null)
		// hotScoresFragment.showLeaderboards(leaderboards); // not used
	}

	@Override
	public void onLeaderboardsError(GetLeaderboardsResponse result) {
		Toast.makeText(
				this,
				String.format("Warning: Leaderboards were not retrieved from Amazon GameCircle! "
						+ "Please make sure you have logged in with your amazon account"), Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Bundle options = intent.getExtras();
		// String requestingUser = options.getString("requestingUser");
		String forUser = options.getString("forUser");
		long requestId = options.getLong("requestId");
		Playable.Type gameType = Type.VENDETTA; // as default
		if (options.getString("gameType") != null)
			gameType = Playable.Type.valueOf(options.getString("gameType"));

		if (login != null && login.getUsername() != null && login.getUsername().equalsIgnoreCase(forUser)) {
			GameManager gameManager = getGameManager(gameType);
			gameManager.joinGame(requestId, login.getUsername());
		}
	}

	private GameManager getGameManager(Playable.Type gameType) {
		if (gameManager == null || gameManager.getClass().getName().equals(DemoGameManager.class.getName())) {
			gameManager = GameManagerFactory.getManager(gameType, svc, MainActivity.this, login.getUsername());
		}
		return gameManager;
	}

	public NotificationManager getNotificationManager() {
		return mNotificationManager;
	}

	@Override
	public void onGameJoined(Game result) {
		if (result != null) {
			displayStartGameUI(result, result.getFirstQuestion());
		} else
			Toast.makeText(this, String.format("Attempted to join a non-existing game!"), Toast.LENGTH_LONG).show();

	}

}
