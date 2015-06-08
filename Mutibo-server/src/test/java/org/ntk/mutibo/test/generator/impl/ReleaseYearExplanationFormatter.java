package org.ntk.mutibo.test.generator.impl;

import java.util.List;

import org.ntk.mutibo.repository.Item;
import org.ntk.mutibo.test.generator.ExplanationFormatter;

public class ReleaseYearExplanationFormatter implements ExplanationFormatter {

    @Override
    public String formatExplanation(List<Item> items, int differentItemIndex) {
        Item differentItem = items.get(differentItemIndex);
        Item item = (differentItemIndex == 0 ? items.get(1) : items.get(0));
        return String.format("Movie '%s' was not released in %d like all others, but in %d", differentItem.getName(),
                item.getYear(), differentItem.getYear());
    }

}
