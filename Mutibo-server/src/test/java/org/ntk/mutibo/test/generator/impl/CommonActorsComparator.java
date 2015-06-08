package org.ntk.mutibo.test.generator.impl;

import java.util.List;

import org.ntk.mutibo.repository.Item;
import org.ntk.mutibo.test.generator.ItemComparator;

import com.google.common.collect.Lists;

/**
 * Compares two items by searching for at least one common contributor
 * 
 * @author Nick
 * 
 */
public class CommonActorsComparator implements ItemComparator {

    @Override
    public ComparisonResult compareItems(Item item1, Item item2) {

        if (item1.getContributors() == null || item1.getContributors().isEmpty() || item2.getContributors() == null
                || item2.getContributors().isEmpty())
            return ComparisonResult.UNKNOWN;

        List<String> commonActors = Lists.newArrayList(item1.getContributors());
        commonActors.retainAll(item2.getContributors());

        if (commonActors.size() > 0)
            return ComparisonResult.SIMILAR;
        else
            return ComparisonResult.DIFFERENT;
    }

}
