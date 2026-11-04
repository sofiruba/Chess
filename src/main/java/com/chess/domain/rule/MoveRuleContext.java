package com.chess.domain.rule;

import java.util.Objects;
import java.util.Optional;

import com.chess.domain.command.IMoveInfo;
import com.chess.domain.model.Board;

/**
 * Lo que una regla necesita conocer sobre la posición actual. La última jugada es de solo lectura y está vacía
 * antes de la primera jugada. {@code pieceStatus} dice qué piezas no pueden actuar o no pueden ser capturadas.
 */
public record MoveRuleContext(
        Board board,
        ICheckDetector checkDetector,
        Optional<IMoveInfo> lastMove,
        CastlingRights castlingRights,
        IPieceStatus pieceStatus) {

    public MoveRuleContext {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(checkDetector, "checkDetector must not be null");
        Objects.requireNonNull(lastMove, "lastMove must not be null, use Optional.empty()");
        Objects.requireNonNull(castlingRights, "castlingRights must not be null");
        Objects.requireNonNull(pieceStatus, "pieceStatus must not be null, use IPieceStatus.NONE");
    }

    /** Un contexto sin restricciones sobre las piezas, como en el ajedrez clásico. */
    public MoveRuleContext(Board board, ICheckDetector checkDetector, Optional<IMoveInfo> lastMove,
                           CastlingRights castlingRights) {
        this(board, checkDetector, lastMove, castlingRights, IPieceStatus.NONE);
    }
}
