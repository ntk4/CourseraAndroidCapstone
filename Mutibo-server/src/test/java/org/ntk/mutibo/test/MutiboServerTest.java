package org.ntk.mutibo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.ntk.mutibo.api.MutiboApi;
import org.ntk.mutibo.client.SecuredRestBuilder;
import org.ntk.mutibo.client.UnsafeHttpsClient;
import org.ntk.mutibo.repository.Game;
import org.ntk.mutibo.repository.GameRequest;
import org.ntk.mutibo.repository.ItemSet;
import org.ntk.mutibo.repository.ItemSetDifficulty;
import org.ntk.mutibo.repository.MutiboUser;

import retrofit.RetrofitError;
import retrofit.client.ApacheClient;

public class MutiboServerTest {

    private final String TEST_URL = "https://localhost:8443";

    private final String USERNAME1 = "admin";
    private final String USERNAME2 = "user0";
    private final String PASSWORD = "pass";
    private final String CLIENT_ID = "mobile";

    private MutiboApi svc = new SecuredRestBuilder()
            .setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient())).setEndpoint(TEST_URL)
            .setLoginEndpoint(TEST_URL + MutiboApi.TOKEN_PATH)
            // .setLogLevel(LogLevel.FULL)
            .setUsername(USERNAME1).setPassword(PASSWORD).setClientId(CLIENT_ID).build().create(MutiboApi.class);

    private MutiboApi readWriteSvcUser2 = new SecuredRestBuilder()
            .setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient())).setEndpoint(TEST_URL)
            .setLoginEndpoint(TEST_URL + MutiboApi.TOKEN_PATH)
            // .setLogLevel(LogLevel.FULL)
            .setUsername(USERNAME2).setPassword(PASSWORD).setClientId(CLIENT_ID).build().create(MutiboApi.class);

    @Test
    @Ignore
    public void testGetAdminUser() {
        MutiboUser user1Stored = svc.getUser("admin");

        assertNotNull(user1Stored);
        assertEquals("admin", user1Stored.getUsername());
        assertEquals("pass", user1Stored.getPassword());
    }

    @Test
    public void testCreateUser() {
        MutiboUser user1 = new MutiboUser(0, "John Smith", "jsmith", "pass");
        MutiboUser user1Stored = svc.registerUser(user1);

        assertNotNull(user1Stored);
        assertEquals("John Smith", user1Stored.getName());
        assertEquals("jsmith", user1Stored.getUsername());
        assertEquals("pass", user1Stored.getPassword());
        assertTrue(user1Stored.getId() > 0);

        MutiboUser user2Stored = svc.getUser("jsmith");

        assertNotNull(user1Stored);

        assertEquals(user1Stored.getName(), user2Stored.getName());
        assertEquals(user1Stored.getUsername(), user2Stored.getUsername());
        assertEquals(user1Stored.getPassword(), user2Stored.getPassword());
        assertEquals(user1Stored.getId(), user2Stored.getId());
    }
    


    @Test
    public void testGetAllUsers() {
        MutiboUser user1 = new MutiboUser(0, "John Smith", "jsmith", "pass");
        svc.registerUser(user1);
        user1 = new MutiboUser(0, "Nick T", "tnick", "pass");
        svc.registerUser(user1);
        Collection<MutiboUser> users = svc.getUsers();

        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertEquals(2,users.size());
    }

    @Test
    public void testGetNonExistingUser() {
        // we use a complete try-catch block just to make sure he error code is 404
        try {
            svc.getUser("doesnotexist");
            fail();
        } catch (RetrofitError e) {
            if (e.getResponse().getStatus() != 404)
                fail();
        }
    }

    @Test
    public void testGetAllMovieSetsForAdmin() throws Exception {
        Collection<ItemSet> stored = svc.getItemSetList("admin");
        assertFalse(stored == null);
        assertFalse(stored.isEmpty());
    }

    @Test
    public void testDemoGame() throws Exception {
        Collection<ItemSet> stored = svc.getDemo();
        assertFalse(stored == null);
        assertFalse(stored.isEmpty());
        assertEquals(10, stored.size());
    }

    @Test
    public void testStartSoloGame() throws Exception {
        Game game = svc.startGame(Game.Type.SOLO, "admin", "");
        assertNotNull(game);
        assertEquals(Game.Type.SOLO, game.getType());
    }

    @Test
    public void testGetItemSet() throws Exception {
        ItemSet itemSet = svc.getItemSet(1);
        assertNotNull(itemSet);
        assertEquals(1, itemSet.getId());
        assertNotNull(itemSet.getItems());
        assertFalse(itemSet.getItems().isEmpty());
    }

    @Test
    public void testNextItemSet() throws Exception {
        Game game = svc.startGame(Game.Type.SOLO, "admin", "");
        assertNotNull(game);
        assertEquals(Game.Type.SOLO, game.getType());

        ItemSet itemset = svc.getNextItemSet(game.getId(), ItemSetDifficulty.NORMAL.getLevel());
        assertNotNull(itemset);
    }

    @Test
    public void testAnswerItemSetCorrectly() throws Exception {
        Game game = svc.startGame(Game.Type.SOLO, "admin", "");
        assertNotNull(game);

        // answer correctly
        ItemSet itemSet = svc.getNextItemSet(game.getId(), ItemSetDifficulty.NORMAL.getLevel());
        svc.answerItemSet(game.getId(), itemSet.getId(), itemSet.getDifferentItemId());
        game = svc.findGame(game.getId());
        assertEquals(Game.SCORE_PER_CORRECT_ANSWER, game.getScore());

    }

    @Test
    public void testAnswerItemSetIncorrectly() throws Exception {
        Collection<ItemSet> itemSets = svc.getDemo();
        assertNotNull(itemSets);
        assertNotEquals(0, itemSets.size());

        ItemSet itemSet = (ItemSet) itemSets.toArray()[0];
        Game game = svc.startGame(Game.Type.SOLO, "admin", "");
        assertNotNull(game);

        // answer wrong
        svc.answerItemSet(game.getId(), itemSet.getId(), itemSet.getDifferentItemId() != 1 ? 1 : 2);
        game = svc.findGame(game.getId());
        assertEquals(0, game.getScore());
    }

    @Test
    public void testAnswerItemSetTwice() throws Exception {
        Collection<ItemSet> itemSets = svc.getDemo();
        assertNotNull(itemSets);
        assertNotEquals(0, itemSets.size());

        ItemSet itemSet = (ItemSet) itemSets.toArray()[0];
        Game game = svc.startGame(Game.Type.SOLO, "admin", "");
        assertNotNull(game);

        // answer wrong
        svc.answerItemSet(game.getId(), itemSet.getId(), itemSet.getDifferentItemId() != 1 ? 1 : 2);
        game = svc.findGame(game.getId());
        assertEquals(0, game.getScore());

        // try to answer the same but correctly
        svc.answerItemSet(game.getId(), itemSet.getId(), itemSet.getDifferentItemId());
        game = svc.findGame(game.getId());
        assertEquals(0, game.getScore()); // should not have modified the score
    }

    @Test
    public void testFinishGame() throws Exception {
        Game game = svc.startGame(Game.Type.SOLO, "admin", "");
        assertNotNull(game);
        svc.finishGame(game.getId());
    }

    @Test
    public void testFindGame() throws Exception {
        Game game = svc.startGame(Game.Type.SOLO, "admin", "");
        assertNotNull(game);
        assertNotEquals(0, game.getId());

        Game game2 = svc.findGame(game.getId());
        assertNotNull(game);
        assertEquals(game.getId(), game2.getId());
    }

    @Test
    public void testLikeItemSet() throws Exception {
        Game game = svc.startGame(Game.Type.SOLO, "admin", "");
        assertNotNull(game);
        assertNotEquals(0, game.getId());

        ItemSet itemSet = svc.getNextItemSet(game.getId(), ItemSetDifficulty.NORMAL.getLevel());
        assertNotNull(itemSet);

        svc.dislikeItemSet(itemSet.getId());
        assertFalse(svc.hasUserLikedItemSet(itemSet.getId()));

        svc.likeItemSet(itemSet.getId());
        assertTrue(svc.hasUserLikedItemSet(itemSet.getId()));

        svc.dislikeItemSet(itemSet.getId());
        assertFalse(svc.hasUserLikedItemSet(itemSet.getId()));
    }
    
    @Test
    public void testPendingGameRequests() throws Exception {
        Collection<GameRequest> requests = svc.getPendingGameRequests("admin");
        assertNotNull(requests);
    }
}
