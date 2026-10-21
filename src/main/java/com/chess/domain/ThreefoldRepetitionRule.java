package com.chess.domain;

/**
 * Draw when the position just reached has occurred three times. It is applied automatically, without a claim.
 */
public final class ThreefoldRepetitionRule implements IDrawRule {

    private static final int REQUIRED_OCCURRENCES = 3;

    @Override
    public boolean isDraw(Board board, DrawCounters counters) {
        return counters.timesCurrentPositionSeen() >= REQUIRED_OCCURRENCES;
    }
}
