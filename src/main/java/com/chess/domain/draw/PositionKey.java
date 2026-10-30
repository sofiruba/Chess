package com.chess.domain.draw;

import com.chess.domain.model.*;

import com.chess.domain.command.*;

import com.chess.domain.rule.*;


import java.util.Objects;

/**
 * What makes two positions "the same" for the repetition rule: piece placement, side to move, castling rights and
 * the en passant square. Assumption: the en passant square counts whenever the last move was a pawn double step,
 * even if no enemy pawn is in a position to capture.
 */
public record PositionKey(String placement, Color turn, CastlingRights castlingRights, Position enPassantSquare) {

    private static final String EMPTY_SQUARE = "-";
    private static final char SEPARATOR = ',';

    public PositionKey {
        Objects.requireNonNull(placement, "placement must not be null");
        Objects.requireNonNull(turn, "turn must not be null");
        Objects.requireNonNull(castlingRights, "castlingRights must not be null");
    }

    /**
     * @param lastMove the move that led to this position, or null if it is the starting position
     */
    public static PositionKey of(Board board, Color turn, CastlingRights castlingRights, IMoveInfo lastMove) {
        Objects.requireNonNull(board, "board must not be null");
        return new PositionKey(placementOf(board), turn, castlingRights, enPassantSquareAfter(lastMove));
    }

    public static PositionKey initial(Board board, Color turn, CastlingRights castlingRights) {
        Objects.requireNonNull(board, "board must not be null");
        return new PositionKey(placementOf(board), turn, castlingRights, null);
    }

    private static String placementOf(Board board) {
        StringBuilder placement = new StringBuilder();
        for (Position position : board.positions()) {
            placement.append(symbolAt(board, position));
            placement.append(SEPARATOR);
        }
        return placement.toString();
    }

    private static String symbolAt(Board board, Position position) {
        Piece piece = board.pieceAt(position);
        if (piece == null) {
            return EMPTY_SQUARE;
        }
        return piece.color().name().charAt(0) + piece.type().name();
    }

    private static Position enPassantSquareAfter(IMoveInfo lastMove) {
        if (lastMove == null) {
            return null;
        }
        return lastMove.enPassantSquare().orElse(null);
    }
}