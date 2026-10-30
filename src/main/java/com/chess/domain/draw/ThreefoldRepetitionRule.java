package com.chess.domain.draw;

import com.chess.domain.model.Board;


/**
 * Tablas cuando la posición recién alcanzada ocurrió tres veces. Se aplica automáticamente, sin reclamación.
 */
public final class ThreefoldRepetitionRule implements IDrawRule {

    private static final int REQUIRED_OCCURRENCES = 3;

    @Override
    public boolean isDraw(Board board, DrawCounters counters) {
        return counters.timesCurrentPositionSeen() >= REQUIRED_OCCURRENCES;
    }
}
