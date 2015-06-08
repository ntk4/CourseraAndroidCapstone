package org.ntk.mutibo.test.generator.impl;

import java.util.List;

import org.ntk.mutibo.repository.Item;
import org.ntk.mutibo.repository.ItemSetDifficulty;
import org.ntk.mutibo.test.generator.DifficultyEvaluator;

import com.google.common.collect.Lists;

public class CommonActorsDifficultyEvaluator implements DifficultyEvaluator {

    @Override
    public ItemSetDifficulty evaluateDifficulty(List<Item> items, int differentItemIndex) {

        int index = 0;
        // take the next item every time ignoring the different one, we need the similar entries only
        Item item1 = null;
        if (index == differentItemIndex) {

            item1 = items.get(++index);
            index++; // need to increment again, the current index was just used!
        } else
            item1 = items.get(index++);

        Item item2 = null;
        if (index == differentItemIndex) {

            item2 = items.get(++index);
            index++;
        } else
            item2 = items.get(index++);

        Item item3 = null;
        if (index == differentItemIndex) {

            item3 = items.get(++index);
            index++;
        } else
            item3 = items.get(index++);

        // Item item2 = index == differentItemIndex ? items.get(index += 2) : items.get(index++);
        // Item item3 = index == differentItemIndex ? items.get(index += 2) : items.get(index++);

        List<String> commonActors = Lists.newArrayList(item1.getContributors());
        commonActors.retainAll(item2.getContributors());

        int commons1_2 = commonActors.size();

        commonActors.retainAll(item3.getContributors());
        int commons_all = commonActors.size();

        commonActors.clear();
        commonActors.addAll(item1.getContributors());
        commonActors.retainAll(item3.getContributors());

        int commons1_3 = commonActors.size();

        commonActors.clear();
        commonActors.addAll(item2.getContributors());
        commonActors.retainAll(item3.getContributors());

        int commons2_3 = commonActors.size();

        if (commons_all >= 4 || commons1_2 > 4 || commons1_3 > 4 || commons2_3 > 4) {
            return ItemSetDifficulty.VERY_EASY;
        } else if (commons_all >= 3 || commons1_2 > 3 || commons1_3 > 3 || commons2_3 > 3) {
            return ItemSetDifficulty.EASY;
        } else if (commons_all >= 2 || commons1_2 > 2 || commons1_3 > 2 || commons2_3 > 2) {
            return ItemSetDifficulty.NORMAL;
        } else if (commons_all >= 1 || commons1_2 > 1 || commons1_3 > 1 || commons2_3 > 1) {
            return ItemSetDifficulty.HARD;
        } else {
            return ItemSetDifficulty.EXTREME;
        }

    }

}
