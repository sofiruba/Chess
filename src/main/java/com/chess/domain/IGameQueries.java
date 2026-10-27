package com.chess.domain;

import java.util.List;

/**
 * Read-only view of a game. Whoever receives this cannot change the game.
 */
public interface IGameQueries {
    int boardSize();
    Piece pieceAt(Position position);
    Color turn();
    boolean isInCheck(Color color);
    Position kingInCheckPosition();
    List<Position> legalMoves(Position from);
    boolean requiresPromotion(Position from, Position to);
    boolean isFinished();
    GameResult result();
    List<Piece> capturedPieces();
    List<String> moveNotation();
    List<String> moveCodes();
}