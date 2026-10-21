package com.chess.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.chess.domain.Color;
import com.chess.domain.Piece;
import com.chess.domain.PieceType;

/**
 * Unicode symbol of each piece, owned by the UI. A piece without a registered symbol is drawn with the letter of
 * its notation code (uppercase for white, lowercase for black), so a new piece is always visible.
 */
public final class PieceSymbols {

    private final Map<PieceType, String> whiteSymbols;
    private final Map<PieceType, String> blackSymbols;

    public PieceSymbols(Map<PieceType, String> whiteSymbols, Map<PieceType, String> blackSymbols) {
        this.whiteSymbols = Map.copyOf(Objects.requireNonNull(whiteSymbols, "whiteSymbols must not be null"));
        this.blackSymbols = Map.copyOf(Objects.requireNonNull(blackSymbols, "blackSymbols must not be null"));
    }

    public static PieceSymbols standard() {
        Map<PieceType, String> white = Map.of(
                PieceType.KING, "♔",
                PieceType.QUEEN, "♕",
                PieceType.ROOK, "♖",
                PieceType.BISHOP, "♗",
                PieceType.KNIGHT, "♘",
                PieceType.PAWN, "♙");
        Map<PieceType, String> black = Map.of(
                PieceType.KING, "♚",
                PieceType.QUEEN, "♛",
                PieceType.ROOK, "♜",
                PieceType.BISHOP, "♝",
                PieceType.KNIGHT, "♞",
                PieceType.PAWN, "♟");
        return new PieceSymbols(white, black);
    }

    /** Returns a copy that also knows the symbols of the given piece type. */
    public PieceSymbols with(PieceType type, String whiteSymbol, String blackSymbol) {
        Map<PieceType, String> white = new HashMap<>(whiteSymbols);
        Map<PieceType, String> black = new HashMap<>(blackSymbols);
        white.put(type, whiteSymbol);
        black.put(type, blackSymbol);
        return new PieceSymbols(white, black);
    }

    public String symbolFor(Piece piece) {
        if (piece == null) {
            return "";
        }
        Map<PieceType, String> symbols = piece.color() == Color.WHITE ? whiteSymbols : blackSymbols;
        String symbol = symbols.get(piece.type());
        return symbol != null ? symbol : fallbackSymbol(piece);
    }

    private String fallbackSymbol(Piece piece) {
        String code = piece.type().notationCode();
        return piece.color() == Color.WHITE ? code.toUpperCase() : code.toLowerCase();
    }
}
