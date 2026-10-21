package com.chess.domain;

import java.util.List;

public interface IGameService {
    void playMove(Position from, Position to);
    void playMove(Position from, Position to, PieceType promotionType);
    boolean requiresPromotion(Position from, Position to);
    int boardSize();
    Position kingInCheckPosition();
    void undoLastMove();
    void redoLastMove();
    List<Position> legalMoves(Position from);
    Piece pieceAt(Position position);
    Color turn();
    boolean isInCheck(Color color);
    boolean isFinished();
    GameResult result();
    List<Piece> capturedPieces();
    List<String> moveNotation();
    List<String> moveCodes();
    void playMoveCode(String moveCode);
}