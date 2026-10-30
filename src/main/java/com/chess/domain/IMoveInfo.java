package com.chess.domain;

import java.util.List;
import java.util.Optional;

/**
 * Read-only view of a move. Rules, draw counters and queries receive this type, so they can inspect the last
 * move but cannot execute or undo it; that power belongs to {@link ICommand}.
 */
public interface IMoveInfo {

    Position from();

    Position to();

    Piece movingPiece();

    Piece capturedPiece();

    /** Text shown to the player. It is not meant to rebuild a move, use {@link #moveCode()} for that. */
    String notation();

    /**
     * Code used to persist and replay a move: origin, destination and an optional promotion letter ("e1g1",
     * "e7e8q"). Castling is the king move and en passant is the pawn move, so the code is enough to rebuild them.
     */
    default String moveCode() {
        return Notation.square(from()) + Notation.square(to());
    }

    default Color movingColor() {
        return movingPiece().color();
    }

    default List<MovedPiece> movedPieces() {
        return List.of(new MovedPiece(movingPiece(), from()));
    }

    /**
     * The square a pawn jumped over with a double step, which is where an enemy pawn could capture it en passant.
     * Empty for any other move.
     */
    default Optional<Position> enPassantSquare() {
        if (!isPawnDoubleStep()) {
            return Optional.empty();
        }
        return Optional.of(new Position(to().x(), (from().y() + to().y()) / 2));
    }

    private boolean isPawnDoubleStep() {
        return movingPiece().type() == PieceType.PAWN && Math.abs(to().y() - from().y()) == 2;
    }

    record MovedPiece(Piece piece, Position from) {
    }
}