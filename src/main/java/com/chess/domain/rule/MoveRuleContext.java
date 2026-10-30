package com.chess.domain.rule;

import java.util.Objects;
import java.util.Optional;

import com.chess.domain.command.IMoveInfo;
import com.chess.domain.model.Board;

/**
 * Lo que una regla necesita conocer sobre la posición actual. La última jugada es de solo lectura y está vacía antes
 * de la primera jugada.
 */
public record MoveRuleContext(
        Board board,
        ICheckDetector checkDetector,
        Optional<IMoveInfo> lastMove,
        CastlingRights castlingRights) {

    public MoveRuleContext(Board board, ICheckDetector checkDetector, Optional<IMoveInfo> lastMove,
                           CastlingRights castlingRights) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(checkDetector, "checkDetector must not be null");
        Objects.requireNonNull(lastMove, "lastMove must not be null, use Optional.empty()");
        Objects.requireNonNull(castlingRights, "castlingRights must not be null");
        this.board = board;
        this.checkDetector = checkDetector;
        this.lastMove = lastMove;
        this.castlingRights = castlingRights;
    }
}
