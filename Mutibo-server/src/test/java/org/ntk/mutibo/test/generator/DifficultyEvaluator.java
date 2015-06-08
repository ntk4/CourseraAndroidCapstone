package org.ntk.mutibo.test.generator;

import java.util.List;

import org.ntk.mutibo.repository.Item;
import org.ntk.mutibo.repository.ItemSetDifficulty;

public interface DifficultyEvaluator {
    ItemSetDifficulty evaluateDifficulty(List<Item> items, int differentItemIndex);
}
