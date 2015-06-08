package org.ntk.mutibo.api;

import java.util.Collection;

import org.ntk.mutibo.json.Game;
import org.ntk.mutibo.json.GameRequest;
import org.ntk.mutibo.json.Item;
import org.ntk.mutibo.json.ItemSet;
import org.ntk.mutibo.json.MutiboUser;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * 
 * 
 * This interface defines an API for a Mutibo service. The interface is used to provide a contract for client/server
 * interactions. The interface is annotated with Retrofit annotations so that clients can automatically convert the
 * interface into a client capable of sending the appropriate HTTP requests.
 * 
 * 
 * @author nick
 * 
 */
public interface MutiboApi {

	public static final String TITLE_PARAMETER = "title";

	public static final String DURATION_PARAMETER = "duration";

	public static final String TOKEN_PATH = "/oauth/token";

	/** The path where we expect the Mutibo to live */
	public static final String MUTIBO_SVC_PATH = "/mutibo";

	/** The path where we expect the Mutibo to handle items */
	public static final String MUTIBO_ITEM_PATH = MUTIBO_SVC_PATH + "/item";

	/** The path where we expect the Mutibo to handle movie sets */
	public static final String MUTIBO_ITEMSET_PATH = MUTIBO_SVC_PATH + "/itemset";

	public static final String MUTIBO_ITEMSET_PATH_WITH_ID = MUTIBO_ITEMSET_PATH + "/{id}";

	/** The path where we expect the Mutibo to handle games */
	public static final String MUTIBO_GAME_PATH = MUTIBO_SVC_PATH + "/game";

	public static final String MUTIBO_GAME_PATH_WITH_ID = MUTIBO_GAME_PATH + "/{id}";

	public static final String MUTIBO_GAME_PATH_WITH_DIFFICULTY = MUTIBO_GAME_PATH_WITH_ID + "/next/{difficulty}";

	public static final String MUTIBO_GAME_DEMO_PATH = MUTIBO_GAME_PATH + "/demo";

	public static final String MUTIBO_USER_PATH = MUTIBO_SVC_PATH + "/user";

	public static final String MUTIBO_USER_PATH_WITH_USERNAME = MUTIBO_USER_PATH + "/{username}";

	public static final String MUTIBO_GAMEREQUEST_PATH = MUTIBO_SVC_PATH + "/gamerequest";

	public static final String MUTIBO_GAMEREQUEST_PATH_WITH_ID = MUTIBO_GAMEREQUEST_PATH + "/{id}";

	public static final String MUTIBO_GAMEREQUEST_PATH_WITH_USER = MUTIBO_GAMEREQUEST_PATH + "/{user}";

	public static final String MUTIBO_GAMEREQUEST_JOIN_PATH = MUTIBO_GAMEREQUEST_PATH_WITH_ID + "/{user}/join";

	public static final String MUTIBO_GAMEREQUEST_PATH_REQUEST = MUTIBO_GAMEREQUEST_PATH_WITH_USER + "/request";

	public static final String MUTIBO_GAMEREQUEST_PENDING_PATH = MUTIBO_GAMEREQUEST_PATH_WITH_USER + "/pending";

	public static final String MUTIBO_GAMEREQUEST_ANSWER_PATH = MUTIBO_GAMEREQUEST_PATH_WITH_ID
			+ "/answer/{answerType}";

	@GET(MUTIBO_USER_PATH)
	public Collection<MutiboUser> getUsers();

	@GET(MUTIBO_USER_PATH_WITH_USERNAME)
	public MutiboUser getUser(@Path("username") String username);

	@POST(MUTIBO_USER_PATH)
	public MutiboUser registerUser(@Body MutiboUser user);

	@GET(MUTIBO_ITEMSET_PATH)
	public Collection<ItemSet> getItemSetList(@Query("user") String user);

	@GET(MUTIBO_ITEM_PATH)
	public Collection<Item> getItemList(@Query("user") String user);

	@POST(MUTIBO_ITEM_PATH)
	public Item addItem(@Body Item v);

	@GET(MUTIBO_ITEMSET_PATH_WITH_ID)
	public ItemSet getItemSet(@Path("id") long id);

	@POST(MUTIBO_ITEMSET_PATH)
	public ItemSet addItemSet(@Body ItemSet v);

	@POST(MUTIBO_ITEMSET_PATH_WITH_ID + "/like")
	public Void likeItemSet(@Path("id") long id);

	@POST(MUTIBO_ITEMSET_PATH_WITH_ID + "/dislike")
	public Void dislikeItemSet(@Path("id") long id);

	@GET(MUTIBO_ITEMSET_PATH_WITH_ID + "/like")
	public boolean hasUserLikedItemSet(@Path("id") long id);

	/* Game operations */
	@GET(MUTIBO_GAME_DEMO_PATH)
	public Collection<ItemSet> getDemo();

	@GET(MUTIBO_GAME_PATH_WITH_ID)
	public Game findGame(@Path("id") long id);

	@POST(MUTIBO_GAME_PATH + "/start/{type}")
	public Game startGame(@Path("type") Game.Type type, @Query("user1") String user1, @Query("user2") String user2);

	@POST(MUTIBO_GAMEREQUEST_JOIN_PATH)
	public Game joinGame(@Path("id") long id, @Path("user") String user);

	@GET(MUTIBO_GAME_PATH_WITH_DIFFICULTY)
	public ItemSet getNextItemSet(@Path("id") long id, @Path("difficulty") int difficulty);

	@POST(MUTIBO_GAME_PATH_WITH_ID + "/answer")
	public Void answerItemSet(@Path("id") long id, @Query("setid") long setId, @Query("answer") int answer);

	@POST(MUTIBO_GAME_PATH_WITH_ID + "/finish")
	public Void finishGame(@Path("id") long id);

	/* Game request operations */
	@POST(MUTIBO_GAMEREQUEST_PATH_REQUEST)
	public GameRequest requestGame(@Query("type") Game.Type type, @Path("user") String requestingUser,
			@Query("forUser") String forUser);

	@POST(MUTIBO_GAMEREQUEST_ANSWER_PATH)
	public Game answerGameRequest(@Path("id") long id, @Path("answerType") GameRequest.AnswerType answer);

	@GET(MUTIBO_GAMEREQUEST_PENDING_PATH)
	public Collection<GameRequest> getPendingGameRequests(@Path("user") String user);
}
