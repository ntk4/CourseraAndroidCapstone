package org.ntk.mutibo.test.generator;

import java.util.List;

import org.ntk.mutibo.repository.Item;

public interface ExplanationFormatter {

    String formatExplanation(List<Item> items, int differentItemIndex);
}
