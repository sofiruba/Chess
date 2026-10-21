package com.chess.domain;

import java.util.List;

public interface ICommand {

    void execute();
    void undo();
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

    record MovedPiece(Piece piece, Position from) {
    }
}