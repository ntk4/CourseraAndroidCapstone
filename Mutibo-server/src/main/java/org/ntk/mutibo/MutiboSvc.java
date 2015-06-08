package org.ntk.mutibo;

import java.security.Principal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.ntk.mutibo.api.MutiboApi;
import org.ntk.mutibo.repository.Game;
import org.ntk.mutibo.repository.GameRepository;
import org.ntk.mutibo.repository.GameRequest;
import org.ntk.mutibo.repository.GameRequest.AnswerType;
import org.ntk.mutibo.repository.GameRequestRepository;
import org.ntk.mutibo.repository.Item;
import org.ntk.mutibo.repository.ItemRepository;
import org.ntk.mutibo.repository.ItemSet;
import org.ntk.mutibo.repository.ItemSetDifficulty;
import org.ntk.mutibo.repository.ItemSetRepository;
import org.ntk.mutibo.repository.LikedUser;
import org.ntk.mutibo.repository.LikedUserRepository;
import org.ntk.mutibo.repository.MutiboUser;
import org.ntk.mutibo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class MutiboSvc {

	@Autowired
	private ItemSetRepository itemSetRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private GameRepository gameRepository;

	@Autowired
	private LikedUserRepository likedUserRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private GameRequestRepository gameRequestRepository;

	@RequestMapping(value = MutiboApi.MUTIBO_USER_PATH, method = RequestMethod.GET)
	public @ResponseBody
	List<MutiboUser> getUsers(HttpServletResponse response) {
		Iterable<MutiboUser> userIterable = userRepository.findAll();
		List<MutiboUser> result = Lists.newArrayList();

		if (userIterable != null) {
			Iterator<MutiboUser> iterator = userIterable.iterator();
			while (iterator.hasNext()) {
				result.add(iterator.next());
			}
			return result;
		}
		response.setStatus(404);
		return null;
	}

	@RequestMapping(value = MutiboApi.MUTIBO_USER_PATH_WITH_USERNAME, method = RequestMethod.GET)
	public @ResponseBody
	MutiboUser getUser(@PathVariable("username") String username, HttpServletResponse response) {
		MutiboUser user = userRepository.findByUsername(username);
		if (user != null) {
			return user;
		}
		response.setStatus(404);
		return null;
	}

	@Transactional
	@RequestMapping(value = MutiboApi.MUTIBO_USER_PATH, method = RequestMethod.POST)
	public @ResponseBody
	MutiboUser registerUser(@RequestBody MutiboUser user) {
		MutiboUser existingUser = userRepository.findByUsername(user.getUsername());
		if (existingUser != null) {
			user.setId(existingUser.getId());
		}
		user = userRepository.save(user);
		return user;
	}

	@RequestMapping(value = MutiboApi.MUTIBO_ITEMSET_PATH_WITH_ID, method = RequestMethod.GET)
	public @ResponseBody
	ItemSet getItemSet(@PathVariable("id") Long id, HttpServletResponse response) {
		ItemSet itemset = itemSetRepository.findOne(id);
		if (itemset != null) {
			return itemset;
		}
		response.setStatus(404);
		return null;
	}

	@RequestMapping(value = MutiboApi.MUTIBO_ITEMSET_PATH, method = RequestMethod.GET)
	public @ResponseBody
	Collection<ItemSet> getItemSetList(String user) {
		return Lists.newArrayList(itemSetRepository.findAll());
	}

	@RequestMapping(value = MutiboApi.MUTIBO_ITEM_PATH, method = RequestMethod.GET)
	public @ResponseBody
	List<Item> getItemList(String user) {
		return Lists.newArrayList(itemRepository.findAll());
	}

	@Transactional
	@RequestMapping(value = MutiboApi.MUTIBO_ITEM_PATH, method = RequestMethod.POST)
	public @ResponseBody
	Item addItem(@RequestBody Item m) {

		if (m.getExternalId() > 0 && itemRepository.countByExternalId(m.getExternalId()) > 0)
			return itemRepository.findByExternalId(m.getExternalId());

		m = itemRepository.save(m);
		return m;
	}

	@Transactional
	@RequestMapping(value = MutiboApi.MUTIBO_ITEMSET_PATH, method = RequestMethod.POST)
	public @ResponseBody
	ItemSet addItemSet(@RequestBody ItemSet set) {
		set = itemSetRepository.save(set);
		return set;
	}

	@Transactional
	@RequestMapping(value = MutiboApi.MUTIBO_ITEMSET_PATH_WITH_ID + "/like", method = RequestMethod.POST)
	public ResponseEntity<Void> likeItemSet(@PathVariable("id") Long id, Principal principal) {

		HttpStatus status = likeUnlike(id, principal, true);

		itemSetRepository.activateLikedItemSets();

		return new ResponseEntity<Void>(status);
	}

	@Transactional
	@RequestMapping(value = MutiboApi.MUTIBO_ITEMSET_PATH_WITH_ID + "/dislike", method = RequestMethod.POST)
	public ResponseEntity<Void> dislikeItemSet(@PathVariable("id") Long id, Principal principal) {

		HttpStatus status = likeUnlike(id, principal, false);

		itemSetRepository.inactivateDislikedItemSets();

		return new ResponseEntity<Void>(status);
	}

	private HttpStatus likeUnlike(Long id, Principal principal, boolean like) {
		ItemSet itemSet = itemSetRepository.findOne(id);

		if (itemSet == null)
			return HttpStatus.NOT_FOUND;

		if (itemSet.getUsersLiked() != null) {

			LikedUser previouslyLikedSameUser = null;

			for (LikedUser user : itemSet.getUsersLiked()) {
				if (StringUtils.equals(user.getLocalUser(), principal.getName())) {

					previouslyLikedSameUser = user;

					if ((user.isLiked() && like) || (user.isDisliked() && !like))
						return HttpStatus.OK; // already (un)liked
					else
						break;
				}
			}

			// not yet liked
			if (like)
				itemSet.setLikes(itemSet.getLikes() + 1);
			else
				itemSet.setDislikes(itemSet.getDislikes() + 1);

			if (previouslyLikedSameUser == null) {
				previouslyLikedSameUser = new LikedUser(principal.getName(), itemSet.getId(), like, !like);

				itemSet.getUsersLiked().add(previouslyLikedSameUser);
			} else {
				itemSet.getUsersLiked().remove(previouslyLikedSameUser);
				itemSet.getUsersLiked().add(previouslyLikedSameUser);
			}
			previouslyLikedSameUser.setLiked(like);
			previouslyLikedSameUser.setDisliked(!like);
			itemSetRepository.save(itemSet);
			return HttpStatus.OK; // ok, (un)liked
		}

		return HttpStatus.NOT_FOUND;
	}

	@RequestMapping(value = MutiboApi.MUTIBO_ITEMSET_PATH_WITH_ID + "/like", method = RequestMethod.GET)
	public ResponseEntity<Boolean> hasUserLikedItemSet(@PathVariable("id") Long id, Principal principal) {

		ItemSet itemSet = itemSetRepository.findOne(id);
		if (itemSet != null) {
			List<LikedUser> likedUsers = likedUserRepository.findByLocalUserAndSetId(principal.getName(),
					itemSet.getId());
			if (likedUsers.size() > 0 && likedUsers.get(0).isLiked())
				return new ResponseEntity<Boolean>(true, HttpStatus.OK);
			else if (likedUsers.get(0).isDisliked())
				return new ResponseEntity<Boolean>(false, HttpStatus.OK);
		}

		return new ResponseEntity<Boolean>(false, HttpStatus.NOT_FOUND);
	}

	@RequestMapping(value = MutiboApi.MUTIBO_GAME_DEMO_PATH, method = RequestMethod.GET)
	public @ResponseBody
	List<ItemSet> getDemo() {
		List<ItemSet> itemSets = Lists.newArrayListWithCapacity(10);
		List<Long> itemSetIds = itemSetRepository.findByInactiveFalseAndDifficulty(ItemSetDifficulty.NORMAL.getLevel());

		for (int i = 0; i < 10; i++) {
			itemSets.add(itemSetRepository.findOne(itemSetIds.get((int) (new Random().nextInt(itemSetIds.size())))));
		}
		return itemSets;
	}

	@Transactional
	@RequestMapping(value = MutiboApi.MUTIBO_GAME_PATH_WITH_ID, method = RequestMethod.GET)
	public ResponseEntity<Game> findGame(@PathVariable("id") long id) {
		Game game = gameRepository.findOne(id);

		if (game != null) {
			return new ResponseEntity<Game>(game, HttpStatus.OK);
		} else {
			return new ResponseEntity<Game>(HttpStatus.NOT_FOUND);
		}
	}

	@Transactional
	@RequestMapping(value = MutiboApi.MUTIBO_GAME_PATH + "/start/{type}", method = RequestMethod.POST)
	public @ResponseBody
	Game startGame(@PathVariable("type") Game.Type type, @RequestParam String user1, @RequestParam String user2) {
		Game game = new Game(type, user1, user2, new Timestamp(new java.util.Date().getTime()));

		game = gameRepository.save(game);

		// for every new game load the first item sets
		for (int i = 0; i < Game.INITIAL_SETS_TO_LOAD; i++) {
			getNextItemSet(game.getId(), ItemSetDifficulty.VERY_EASY.getLevel());
			// if (itemSet != null && itemSet.hasBody()) {
			// game.addSet(itemSet.getBody());
			// }
		}
		// query again because the getNextItemSet has updated the list of GameSets
		game = gameRepository.findOne(game.getId());
		return game;
	}

	@Transactional
	@RequestMapping(value = MutiboApi.MUTIBO_GAME_PATH_WITH_DIFFICULTY, method = RequestMethod.GET)
	public ResponseEntity<ItemSet> getNextItemSet(@PathVariable("id") Long id,
			@PathVariable("difficulty") int difficulty) {

		List<Long> itemSetIds = itemSetRepository.findByInactiveFalseAndDifficulty(difficulty);

		if (!itemSetIds.isEmpty()) {

			// store the item set in the game
			Game game = gameRepository.findOne(id);
			boolean foundUniqueItemSet = false;
			ItemSet itemset = null;

			while (!foundUniqueItemSet) {
				// TODO: fix the random mechanism here
				itemset = itemSetRepository.findOne(itemSetIds.get((int) (new Random().nextInt(itemSetIds.size()))));

				foundUniqueItemSet = game.addSet(itemset);
			}
			game = gameRepository.save(game);

			return new ResponseEntity<ItemSet>(itemset, HttpStatus.OK);
		}

		return new ResponseEntity<ItemSet>(HttpStatus.NOT_FOUND);
	}

	@Transactional
	@RequestMapping(value = MutiboApi.MUTIBO_GAME_PATH_WITH_ID + "/answer", method = RequestMethod.POST)
	public ResponseEntity<Void> answerItemSet(@PathVariable("id") Long id, @RequestParam("setid") long setId,
			@RequestParam("answer") int answer) {
		ResponseEntity<Game> gameResponse = findGame(id);
		if (gameResponse.getStatusCode() == HttpStatus.NOT_FOUND)
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);

		ItemSet itemSet = itemSetRepository.findOne(setId);
		if (itemSet == null)
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);

		Game game = gameResponse.getBody();
		if (game == null)
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);

		game.answer(itemSet, answer);
		gameRepository.save(game);

		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@Transactional
	@RequestMapping(value = MutiboApi.MUTIBO_GAME_PATH_WITH_ID + "/finish", method = RequestMethod.POST)
	public ResponseEntity<Void> finishGame(@PathVariable("id") long id) {

		ResponseEntity<Game> gameResponse = findGame(id);
		if (gameResponse.getStatusCode() == HttpStatus.NOT_FOUND)
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);

		Game game = gameResponse.getBody();

		if (game != null) {
			game.setFinished(new Timestamp(new Date().getTime()));
			game = gameRepository.save(game);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} else {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
	}

	@Transactional
	@RequestMapping(value = MutiboApi.MUTIBO_GAMEREQUEST_PATH_REQUEST, method = RequestMethod.POST)
	public @ResponseBody
	GameRequest requestGame(@RequestParam("type") Game.Type type, @PathVariable("user") String requestingUser,
			@RequestParam("forUser") String forUser, HttpServletResponse response) {

		// get similar pending requests
		Collection<GameRequest> gameRequests = gameRequestRepository
				.findByTypeAndRequestingUserAndForUserAndAnswerEquals(type, requestingUser, forUser, AnswerType.PENDING);

		// if such a request already exists, reutrn it, don't create new one
		if (gameRequests != null && gameRequests.size() > 0) {
			return gameRequests.iterator().next();
		}

		GameRequest gameRequest = new GameRequest(type, requestingUser, forUser, new SimpleDateFormat(
				"yyyy/MM/dd hh:mm:ss").format(new Date()));

		// now start the game and update the gameRequest. The forUser will eventually join
		Game game = startGame(type, requestingUser, forUser);
		gameRequest.setGameId(game.getId());
		gameRequest = gameRequestRepository.save(gameRequest);

		if (gameRequest == null || gameRequest.getId() <= 0) {
			response.setStatus(500);
		}

		return gameRequest;
	}

	@Transactional
	@RequestMapping(value = MutiboApi.MUTIBO_GAMEREQUEST_JOIN_PATH, method = RequestMethod.POST)
	public @ResponseBody
	Game joinGame(@PathVariable("id") long id, @PathVariable("user") String user, HttpServletResponse response) {

		GameRequest request = gameRequestRepository.findOne(id);

		if (request == null) {
			response.setStatus(404); // request not found
			return null;
		}

		ResponseEntity<Game> responseEntity = findGame(request.getGameId());

		if (responseEntity != null && responseEntity.getBody() != null) {
			request.setAnswer(AnswerType.START_GAME);
			gameRequestRepository.save(request);
			return responseEntity.getBody();
		} else {
			response.setStatus(responseEntity.getStatusCode().value()); // probably 404 game not found
			return null;
		}
	}

	@Transactional
	@RequestMapping(value = MutiboApi.MUTIBO_GAMEREQUEST_ANSWER_PATH, method = RequestMethod.POST)
	public @ResponseBody
	Game answerGameRequest(@PathVariable("id") long id, @PathVariable("answerType") GameRequest.AnswerType answer,
			HttpServletResponse response) {

		GameRequest request = gameRequestRepository.findOne(id);

		if (request != null) {

			Game game = startGame(request.getType(), request.getRequestingUser(), request.getForUser());

			if (game != null) {
				return game;
			}
		}

		response.setStatus(404);
		return null;
	}

	@RequestMapping(value = MutiboApi.MUTIBO_GAMEREQUEST_PENDING_PATH, method = RequestMethod.GET)
	public @ResponseBody
	Collection<GameRequest> getPendingGameRequests(@PathVariable("user") String user) {

		return gameRequestRepository.findByForUserAndAnswerEquals(user, AnswerType.PENDING);
	}

}
