package com.chess.domain;

public interface ICheckDetector {

    boolean isInCheck(Board board, Color color);

    /** Returns the position of the king of the given color if it is in check, or null otherwise. */
    Position kingInCheckPosition(Board board, Color color);

    boolean isSquareAttacked(Board board, Position position, Color attackingColor);
}