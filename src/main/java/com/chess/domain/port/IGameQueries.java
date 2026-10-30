package com.chess.domain.port;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

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

    /** The pieces a pawn can become, in the order they should be offered. */
    List<PieceType> promotionOptions();
    boolean isFinished();
    GameResult result();
    List<Piece> capturedPieces();
    List<String> moveNotation();
    List<String> moveCodes();
}