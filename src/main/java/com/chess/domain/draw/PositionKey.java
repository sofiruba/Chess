package com.chess.domain.draw;

import java.util.Objects;

import com.chess.domain.command.IMoveInfo;
import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.Piece;
import com.chess.domain.model.PieceRoles;
import com.chess.domain.model.Position;
import com.chess.domain.rule.CastlingRights;

/**
 * Lo que hace que dos posiciones sean "iguales" para la regla de repetición: ubicación de las piezas, lado al que
 * le toca mover, derechos de enroque y casilla al paso. Supuesto: la casilla al paso cuenta cuando la última jugada
 * fue un avance doble de peón, aunque ningún peón enemigo esté en posición de capturar.
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
     * @param lastMove la jugada que llevó a esta posición, o null si es la posición inicial
     */
    public static PositionKey of(Board board, Color turn, CastlingRights castlingRights, IMoveInfo lastMove) {
        Objects.requireNonNull(board, "board must not be null");
        return new PositionKey(
                placementOf(board),
                turn,
                castlingRights,
                enPassantSquareAfter(lastMove, castlingRights.roles()));
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

    private static Position enPassantSquareAfter(IMoveInfo lastMove, PieceRoles roles) {
        if (lastMove == null) {
            return null;
        }
        return lastMove.enPassantSquare(roles).orElse(null);
    }
}
