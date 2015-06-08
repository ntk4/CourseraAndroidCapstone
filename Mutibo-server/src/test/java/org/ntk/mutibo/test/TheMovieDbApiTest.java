/*
 *      Copyright (c) 2004-2014 Stuart Boston
 *
 *      This file is part of TheMovieDB API.
 *
 *      TheMovieDB API is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      any later version.
 *
 *      TheMovieDB API is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with TheMovieDB API.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.ntk.mutibo.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntk.mutibo.repository.Item;
import org.ntk.mutibo.repository.ItemSet;
import org.ntk.mutibo.repository.ItemType;
import org.ntk.mutibo.test.generator.DifficultyEvaluator;
import org.ntk.mutibo.test.generator.ExplanationFormatter;
import org.ntk.mutibo.test.generator.ItemComparator;
import org.ntk.mutibo.test.generator.ItemComparator.ComparisonResult;
import org.ntk.mutibo.test.generator.impl.CommonActorsComparator;
import org.ntk.mutibo.test.generator.impl.CommonActorsDifficultyEvaluator;
import org.ntk.mutibo.test.generator.impl.CommonActorsExplanationFormatter;
import org.ntk.mutibo.test.generator.impl.ReleaseYearComparator;
import org.ntk.mutibo.test.generator.impl.ReleaseYearDifficultyEvaluator;
import org.ntk.mutibo.test.generator.impl.ReleaseYearExplanationFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TestLogger;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.Genre;
import com.omertron.themoviedbapi.model.MovieDb;
import com.omertron.themoviedbapi.model.PersonCast;
import com.omertron.themoviedbapi.model.PersonCrew;
import com.omertron.themoviedbapi.model.TmdbConfiguration;
import com.omertron.themoviedbapi.results.TmdbResultsList;

/**
 * Test cases for TheMovieDbApi API
 * 
 * @author stuart.boston
 */
public class TheMovieDbApiTest extends SampleDataTest {

    private static final int MAX_ATTEMPTS_TO_FIND_SIMILAR_MOVIE = 50;
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(TheMovieDbApiTest.class);
    // API Key
    private static final String PROP_FIlENAME = "testing.properties";
    private static String API_KEY;
    private static int ACCOUNT_ID_APITESTS;
    private static String SESSION_ID_APITESTS;
    private static TheMovieDbApi tmdb;
    // Languages
    private static final String LANGUAGE_DEFAULT = "";
    private static final String LANGUAGE_ENGLISH = "en";

    // session and account id of test users named 'apitests'

    public TheMovieDbApiTest() throws MovieDbException {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestLogger.Configure();

        Properties props = new Properties();
        File f = new File(PROP_FIlENAME);
        if (f.exists()) {
            LOG.info("Loading properties from '{}'", PROP_FIlENAME);
            TestLogger.loadProperties(props, f);

            API_KEY = props.getProperty("API_Key");
            ACCOUNT_ID_APITESTS = NumberUtils.toInt(props.getProperty("Account_ID"), 0);
            SESSION_ID_APITESTS = props.getProperty("Session_ID");
        } else {
            LOG.info("Property file '{}' not found, creating dummy file.", PROP_FIlENAME);

            props.setProperty("API_Key", "ca009cb0786968989ccc131568cf14a4");
            props.setProperty("Account_ID", "ACCOUNT ID FOR SESSION TESTS");
            props.setProperty("Session_ID", "INSERT_YOUR_SESSION_ID_HERE");

            TestLogger.saveProperties(props, f, "Properties file for tests");
            fail("Failed to get key information from properties file '" + PROP_FIlENAME + "'");
        }

        tmdb = new TheMovieDbApi(API_KEY);
    }

    /**
     * Test of getConfiguration method, of class TheMovieDbApi.
     */
    @Test
    public void testConfiguration() {
        LOG.info("Test Configuration");

        TmdbConfiguration tmdbConfig = tmdb.getConfiguration();
        assertNotNull("Configuration failed", tmdbConfig);
        assertTrue("No base URL", StringUtils.isNotBlank(tmdbConfig.getBaseUrl()));
        assertTrue("No backdrop sizes", tmdbConfig.getBackdropSizes().size() > 0);
        assertTrue("No poster sizes", tmdbConfig.getPosterSizes().size() > 0);
        assertTrue("No profile sizes", tmdbConfig.getProfileSizes().size() > 0);
        LOG.info(tmdbConfig.toString());
    }

    /**
     * Test of getPopularMovieList method, of class TheMovieDbApi.
     * 
     * @throws MovieDbException
     */
    @Test
    public void generateItemsFromTopRatedMovies() throws MovieDbException {
        LOG.info("getPopularMovieList");
        TmdbResultsList<MovieDb> totalPageQuery = tmdb.getTopRatedMovies(LANGUAGE_DEFAULT, 0);

        assertTrue("No popular movies found", !totalPageQuery.getResults().isEmpty());

        for (int i = 1; i <= totalPageQuery.getTotalPages(); i++) {
            TmdbResultsList<MovieDb> result = tmdb.getTopRatedMovies(LANGUAGE_DEFAULT, i);
            for (MovieDb movie : result.getResults()) {
                Item item = convertMovieToItem(movie);
                service.addItem(item);
            }
        }

        // assertNotNull(service.getItemList("admin"));
    }

    private Item convertMovieToItem(MovieDb movie) throws MovieDbException {
        String director = "";

        List<String> actors = Lists.newArrayList();

        movie = tmdb.getMovieInfo(movie.getId(), LANGUAGE_ENGLISH,
                "alternative_titles,casts,images,keywords,releases,trailers,translations,similar_movies,reviews,lists");
        for (PersonCast p : movie.getCast()) {
            actors.add(p.getName());
        }

        for (PersonCrew p : movie.getCrew()) {
            if ("director".equalsIgnoreCase(p.getJob())) {
                director = p.getName();
                break;
            }
        }
        
        List<String> genres = convertGenres(movie.getGenres());
        int year = Integer.valueOf(movie.getReleaseDate().substring(0, 4));
        String posterPath = tmdb.createImageUrl(movie.getPosterPath(), "original").toString();

        Item item = new Item(movie.getTitle(), director, year, posterPath, movie.getId(), ItemType.MOVIE);
        item.setContributors(actors);
        item.setGenres(genres);
        return item;
    }

    private List<String> convertGenres(List<Genre> genres) {
        if (genres == null)
            return null;

        List<String> result = Lists.newArrayListWithCapacity(genres.size());
        for (Genre genre : genres) {
            result.add(genre.getName());
        }
        return result;
    }

    @Test
    public void testGetItems() throws MovieDbException {
        Collection<Item> items = service.getItemList("admin");
        assertNotNull(items);
        assertTrue(items.size() > 0);
    }

    @Test
    public void testGetItemSets() throws MovieDbException {
        Collection<ItemSet> items = service.getItemSetList("admin");
        assertNotNull(items);
        assertTrue(items.size() > 0);
    }

    @Test
    public void generateItemSetsFromMoviesOfTheSameYear() throws MovieDbException {
        generateItemSets(new ReleaseYearComparator(), new ReleaseYearDifficultyEvaluator(),
                new ReleaseYearExplanationFormatter());
    }

    @Test
    public void generateItemSetsFromMoviesWithCommonActors() throws MovieDbException {
        generateItemSets(new CommonActorsComparator(), new CommonActorsDifficultyEvaluator(),
                new CommonActorsExplanationFormatter());
    }

    /**
     * Generates ItemSets based on a particular comparison method (e.g. year, director etc.)
     * 
     * To generate new set of questions based on different criteria, only the following are required:
     * <ol>
     * <li>Create a comparator that will compare two items based on the desired attributes</li>
     * <li>Create an evaluator that will assess the difficulty of the resulting ItemSet</li>
     * <li>Create an explanation formatter that will generate an explanation text for the different item</li>
     * <li>Call this method generateItemSets</li>
     * </ol>
     * 
     * @param comparator
     *            the comparator to use and locate the similar and the different item
     * @param evaluator
     *            a difficulty evaluator that matches the given comparator
     */
    private void generateItemSets(ItemComparator comparator, DifficultyEvaluator evaluator,
            ExplanationFormatter explanationFormatter) {
        Collection<Item> itemsCollection = service.getItemList("admin");
        assertNotNull(itemsCollection);
        assertTrue(itemsCollection.size() > 0);

        List<Item> items = Lists.newArrayList(itemsCollection);
        ItemSet temp;
        Item[] otherItems = new Item[2];
        Item differentItem = null;

        boolean similarItemsFound = false, differentItemFound = false;
        int attemptsToFindSimilar = 0;
        Random rand = new Random();

        for (Item item : items) {
            temp = new ItemSet();
            attemptsToFindSimilar = 0;

            for (int i = 0; i < 2; i++) {

                similarItemsFound = false;
                differentItemFound = false;

                while (!similarItemsFound || !differentItemFound) {
                    if (attemptsToFindSimilar++ > MAX_ATTEMPTS_TO_FIND_SIMILAR_MOVIE)
                        break;

                    int randomIndex = rand.nextInt(items.size());

                    Item item2 = items.get(randomIndex);

                    if (item.getExternalId() == item2.getExternalId())
                        continue; // it happens to be the same item

                    ComparisonResult comparisonResult = comparator.compareItems(item, item2);

                    if (comparisonResult == ComparisonResult.SIMILAR) {
                        otherItems[i] = item2;

                        // don't let both others be the same movie
                        if (i == 0
                                || (otherItems[0] != null && otherItems[1] != null && otherItems[0].getExternalId() != otherItems[1]
                                        .getExternalId()))
                            similarItemsFound = true; // found both items of the same year as the template
                    } else if (comparisonResult == ComparisonResult.DIFFERENT) {
                        differentItem = item2; // found a different item
                        differentItemFound = true;
                    }
                }

            }

            if (attemptsToFindSimilar >= MAX_ATTEMPTS_TO_FIND_SIMILAR_MOVIE) // was aborted, don't create ItemSet
                continue;

            List<Item> itemSetItems = Lists.newArrayListWithCapacity(4);
            itemSetItems.add(item);
            itemSetItems.add(otherItems[0]);
            itemSetItems.add(otherItems[1]);
            int differentMovieId = rand.nextInt(4);
            itemSetItems.add(differentMovieId, differentItem);
            temp.setItems(itemSetItems);
            temp.setDifficulty(evaluator.evaluateDifficulty(itemSetItems, differentMovieId).getLevel());

            temp.setDifferentItemId(differentMovieId);
            temp.setInactive(false);

            temp.setExplanation(explanationFormatter.formatExplanation(itemSetItems, differentMovieId));
            service.addItemSet(temp);
        }
    }

}
