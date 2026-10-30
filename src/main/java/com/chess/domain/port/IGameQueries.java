package com.chess.domain.port;

import com.chess.domain.model.*;


import java.util.List;

/**
 * Vista de solo lectura de una partida. Quien la recibe no puede modificarla.
 */
public interface IGameQueries {
    int boardSize();
    Piece pieceAt(Position position);
    Color turn();
    boolean isInCheck(Color color);
    Position kingInCheckPosition();
    List<Position> legalMoves(Position from);
    boolean requiresPromotion(Position from, Position to);

    /** Las piezas en las que puede convertirse un peón, en el orden en que se deben ofrecer. */
    List<PieceType> promotionOptions();
    boolean isFinished();
    GameResult result();
    List<Piece> capturedPieces();
    List<String> moveNotation();
    List<String> moveCodes();
}
