package org.ntk.mutibo.test.generator;

import org.ntk.mutibo.repository.Item;

public interface ItemComparator {
    public enum ComparisonResult {
        UNKNOWN, SIMILAR, DIFFERENT;
    }
    
    ComparisonResult compareItems(Item item1, Item item2);
}
