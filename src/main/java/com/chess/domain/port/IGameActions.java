package com.chess.domain.port;

import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;


/**
 * Las formas de modificar una partida.
 */
public interface IGameActions {
    void playMove(Position from, Position to);
    void playMove(Position from, Position to, PieceType promotionType);
    void playMoveCode(String moveCode);
    void undoLastMove();
    void redoLastMove();
}
