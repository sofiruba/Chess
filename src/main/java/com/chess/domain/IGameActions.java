package com.chess.domain;

/**
 * The ways to change a game.
 */
public interface IGameActions {
    void playMove(Position from, Position to);
    void playMove(Position from, Position to, PieceType promotionType);
    void playMoveCode(String moveCode);
    void undoLastMove();
    void redoLastMove();
}