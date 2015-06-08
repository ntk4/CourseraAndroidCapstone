package org.ntk.mutibo.test.generator.impl;

import java.util.List;

import org.ntk.mutibo.repository.Item;
import org.ntk.mutibo.test.generator.ExplanationFormatter;

import com.google.common.collect.Lists;

public class CommonActorsExplanationFormatter implements ExplanationFormatter {

    @Override
    public String formatExplanation(List<Item> items, int differentItemIndex) {
        int index = 0;
        // take the next item every time ignoring the different one, we need the similar entries only
        Item item1 = items.get(index == differentItemIndex ? ++index : index++), item2 = items
                .get(index == differentItemIndex ? ++index : index++), item3 = items
                .get(index == differentItemIndex ? ++index : index++);

        List<String> commonActors = Lists.newArrayList(item1.getContributors());
        commonActors.retainAll(item2.getContributors());
        commonActors.retainAll(item3.getContributors());

        return String.format("All movies have at least one common actor(e.g. %s), besides movie '%s'", commonActors
                .isEmpty() ? "[Name not available]" : commonActors.get(0), items.get(differentItemIndex).getName());
    }

}
