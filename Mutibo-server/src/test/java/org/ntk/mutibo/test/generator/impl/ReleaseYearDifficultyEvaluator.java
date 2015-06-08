package org.ntk.mutibo.test.generator.impl;

import java.util.List;

import org.ntk.mutibo.repository.Item;
import org.ntk.mutibo.repository.ItemSetDifficulty;
import org.ntk.mutibo.test.generator.DifficultyEvaluator;

public class ReleaseYearDifficultyEvaluator implements DifficultyEvaluator {

    @Override
    public ItemSetDifficulty evaluateDifficulty(List<Item> items, int differentItemIndex) {
        Item item = (differentItemIndex == 0 ? items.get(1) : items.get(0));
        Item differentItem = items.get(differentItemIndex);

        int yearsDifference = Math.abs(item.getYear() - differentItem.getYear());
        if (yearsDifference > 40) {
            return ItemSetDifficulty.VERY_EASY;
        } else if (yearsDifference > 15) {
            return ItemSetDifficulty.EASY;
        } else if (yearsDifference > 5) {
            return ItemSetDifficulty.NORMAL;
        } else if (yearsDifference > 2) {
            return ItemSetDifficulty.HARD;
        } else {
            return ItemSetDifficulty.EXTREME;
        }

    }

}
