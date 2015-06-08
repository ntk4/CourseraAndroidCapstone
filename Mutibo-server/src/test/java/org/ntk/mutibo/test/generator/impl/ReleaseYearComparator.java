package org.ntk.mutibo.test.generator.impl;

import org.ntk.mutibo.repository.Item;
import org.ntk.mutibo.test.generator.ItemComparator;
import org.ntk.mutibo.test.generator.ItemComparator.ComparisonResult;

public class ReleaseYearComparator implements ItemComparator {

    @Override
    public ComparisonResult compareItems(Item item1, Item item2) {
        if (item1 == null || item2 == null || item1.getYear() < 1900 || item1.getYear() < 1900)
            return ComparisonResult.UNKNOWN;
        
        if (item1.getYear() == item2.getYear())
            return ComparisonResult.SIMILAR;
        else return ComparisonResult.DIFFERENT;
    }

}
