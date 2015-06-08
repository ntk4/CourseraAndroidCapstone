package org.ntk.mutibo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.ntk.mutibo.api.MutiboApi;
import org.ntk.mutibo.client.SecuredRestBuilder;
import org.ntk.mutibo.repository.Game;
import org.ntk.mutibo.repository.Item;
import org.ntk.mutibo.repository.ItemSet;
import org.ntk.mutibo.test.helpers.EasyHttpClient;
import org.ntk.mutibo.test.helpers.TestData;

import retrofit.RestAdapter.LogLevel;
import retrofit.client.ApacheClient;

import com.google.common.collect.Lists;

/**
 * Creates the intial data in the DB. To be ran only once.
 */
public class SampleDataTest {

    protected final String TEST_URL = "https://localhost:8443";

    protected final String USERNAME1 = "admin";
    protected final String USERNAME2 = "user0";
    protected final String PASSWORD = "pass";
    protected final String CLIENT_ID = "mobile";

	// private MutiboApi readWriteSvcUser1 = new SecuredRestBuilder()
	// .setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient())).setEndpoint(TEST_URL)
	// .setLoginEndpoint(TEST_URL + MutiboApi.TOKEN_PATH)
	// // .setLogLevel(LogLevel.FULL)
	// .setUsername(USERNAME1).setPassword(PASSWORD).setClientId(CLIENT_ID).build().create(MutiboApi.class);

	protected MutiboApi service = new SecuredRestBuilder().setLoginEndpoint(TEST_URL + MutiboApi.TOKEN_PATH)
			.setUsername(USERNAME1).setPassword(PASSWORD).setClientId(CLIENT_ID)
			.setClient(new ApacheClient(new EasyHttpClient())).setEndpoint(TEST_URL).setLogLevel(LogLevel.FULL).build()
			.create(MutiboApi.class);

	// private MutiboApi readWriteSvcUser2 = new SecuredRestBuilder()
	// .setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient())).setEndpoint(TEST_URL)
	// .setLoginEndpoint(TEST_URL + MutiboApi.TOKEN_PATH)
	// // .setLogLevel(LogLevel.FULL)
	// .setUsername(USERNAME2).setPassword(PASSWORD).setClientId(CLIENT_ID).build().create(MutiboApi.class);

	// private MovieSet set = TestData.initialSets();

	@Test
	public void testCreateItems() throws Exception {
		Collection<Item> storedMovies = service.getItemList(USERNAME1);
		if (storedMovies == null || storedMovies.isEmpty()) {
			Collection<Item> initialData = TestData.initialMovies();
			assertFalse(initialData == null);
			assertFalse(initialData.isEmpty());

			for (Item movie : initialData) {
				// TODO: if exists, don't add it
				service.addItem(movie);
			}
		}
	}

	@Test
	public void testCreateItemSets() throws Exception {
		Collection<ItemSet> storedMovieSets = service.getItemSetList(USERNAME1);
		if (storedMovieSets == null || storedMovieSets.isEmpty()) {

			List<ItemSet> initialData = TestData.initialSets();
			assertFalse(initialData == null);
			assertFalse(initialData.isEmpty());

			for (ItemSet movieSet : initialData) {
				// TODO: if exists, don't add it
				movieSet.setItems(mapMovies(movieSet.getItems(), service.getItemList(USERNAME1)));
				service.addItemSet(movieSet);
			}
		}
	}

	public List<Item> mapMovies(List<Item> transientMovies, Collection<Item> moviesFromDB) {
		List<Item> result = Lists.newArrayListWithCapacity(transientMovies.size());

		for (Item transientMovie : transientMovies) {
			for (Item movieFromDB : moviesFromDB) {
				if (movieFromDB.equals(transientMovie)) {
					result.add(movieFromDB);
					break;
				}
			}
		}
		return result;

	}

	@Test
	public void testLoadDemoGame() {
		Game demo = TestData.demoGame();
		assertNotNull(demo);
		assertEquals(Game.Type.SOLO, demo.getType());
		assertNotNull(demo.getGameSets());
		assertEquals(3, demo.getGameSets().size());

	}
}
