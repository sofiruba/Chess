package com.chess.domain;

import java.util.Objects;

/**
 * What a rule needs to know about the current position. The last move is null before the first move.
 */
public record MoveRuleContext(
        Board board,
        ICheckDetector checkDetector,
        ICommand lastMove,
        CastlingRights castlingRights) {

    public MoveRuleContext {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(checkDetector, "checkDetector must not be null");
        Objects.requireNonNull(castlingRights, "castlingRights must not be null");
    }
}