package com.chess.ui;

import java.util.List;
import java.util.Objects;

/**
 * Remembers the move codes of the game that is already in the database, either because it was just saved or
 * because it was loaded from there. It is screen state only: nothing here is stored. It has no JavaFX so the
 * rule "is this game already saved?" can be tested on its own.
 */
public final class SavedGameTracker {

    /** Null until a game is saved or loaded. */
    private List<String> savedMoveCodes;

    public boolean isSaved(List<String> moveCodes) {
        Objects.requireNonNull(moveCodes, "moveCodes must not be null");
        return savedMoveCodes != null && savedMoveCodes.equals(moveCodes);
    }

    public void markSaved(List<String> moveCodes) {
        Objects.requireNonNull(moveCodes, "moveCodes must not be null");
        savedMoveCodes = List.copyOf(moveCodes);
    }
}