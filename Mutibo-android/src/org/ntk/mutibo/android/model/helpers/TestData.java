package org.ntk.mutibo.android.model.helpers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.ntk.mutibo.json.Game;
import org.ntk.mutibo.json.GameSet;
import org.ntk.mutibo.json.Item;
import org.ntk.mutibo.json.ItemSet;
import org.ntk.mutibo.json.LikedUser;

import com.google.common.collect.Lists;

/**
 * This is a utility class to aid in the construction of Video objects with random names, urls, and durations. The class
 * also provides a facility to convert objects into JSON using Jackson, which is the format that the VideoSvc controller
 * is going to expect data in for integration testing.
 * 
 * @author jules
 * 
 */
public class TestData {

    /**
     * Construct and return Item objects
     * 
     * @return
     */
    public static List<Item> initialMovies() {

        List<Item> movies = Lists.newArrayList();

        for (int i = 0; i < 12; i++) {
            Item movie = new Item("Movie-" + i, "Director-" + i, Calendar.getInstance().get(Calendar.YEAR) - i, "");
            movies.add(movie);
        }
        return movies;
    }

    /**
     * Construct and return ItemSet objects
     * 
     * @return
     */
    public static List<ItemSet> initialSets() {

        List<Item> movies = initialMovies();
        List<ItemSet> movieSets = Lists.newArrayList();

        for (int i = 0; i < 3; i++) {
            ItemSet movieSet = new ItemSet(i + 1, "MovieSet-" + i, Lists.newArrayList(movies.get(i * 3),
                    movies.get(i * 3 + 1), movies.get(i * 3 + 2), movies.get(i * 3 + 3)), 1, 0, 0,
                    new ArrayList<LikedUser>());
            movieSets.add(movieSet);
        }
        return movieSets;
    }

    public static Game demoGame() {
        Game demo = new Game(Game.Type.SOLO, "admin", "", new Timestamp(new java.util.Date().getTime()));

        List<ItemSet> movieSets = initialSets();
        List<GameSet> gameSets = Lists.newArrayListWithCapacity(movieSets.size());
        int questionIndex = 0;
        for (ItemSet itemSet : movieSets) {
            gameSets.add(new GameSet(itemSet.getId(), demo.getId(), questionIndex++, (short) 0));
        }
        demo.setGameSets(gameSets);

        return demo;
    }
}
