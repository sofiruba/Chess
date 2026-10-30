package com.chess.domain.port;

import com.chess.domain.model.*;
import com.chess.domain.port.*;


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