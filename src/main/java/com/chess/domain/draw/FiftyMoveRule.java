package com.chess.domain.draw;

import com.chess.domain.model.*;

/**
 * Draw after the given number of moves by each player (50 in standard chess) without a pawn move or a capture.
 * It is applied automatically, without a claim.
 */
public final class FiftyMoveRule implements IDrawRule {

    private static final int PLAYERS = 2;

    private final int halfMoveLimit;

    /**
     * @param moveLimit moves by each player, so the limit in half-moves is twice this value
     */
    public FiftyMoveRule(int moveLimit) {
        if (moveLimit < 1) {
            throw new IllegalArgumentException("moveLimit must be positive: " + moveLimit);
        }
        this.halfMoveLimit = moveLimit * PLAYERS;
    }

    @Override
    public boolean isDraw(Board board, DrawCounters counters) {
        return counters.halfMoveClock() >= halfMoveLimit;
    }
}
