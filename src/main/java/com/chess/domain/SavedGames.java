package com.chess.domain;

import java.util.List;
import java.util.Objects;

/**
 * The result of reading the game history: the games that could be read, newest first, and how many stored rows
 * had to be left out because they were damaged.
 */
public record SavedGames(List<GameRecord> records, int unreadableRows) {
    public SavedGames {
        Objects.requireNonNull(records, "records must not be null");
        if (unreadableRows < 0) {
            throw new IllegalArgumentException("unreadableRows must not be negative: " + unreadableRows);
        }
        records = List.copyOf(records);
    }
}