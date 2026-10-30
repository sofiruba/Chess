package com.chess.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.chess.domain.model.Color;
import com.chess.domain.model.Piece;
import com.chess.domain.model.PieceType;

/**
 * Símbolo Unicode de cada pieza, perteneciente a la interfaz. Una pieza sin símbolo registrado se dibuja con la letra
 * de su código de notación (mayúscula para blancas, minúscula para negras), por lo que una pieza nueva siempre es
 * visible.
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

    /** Devuelve una copia que también conoce los símbolos del tipo de pieza indicado. */
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
        return symbolFor(piece.type(), piece.color());
    }

    public String symbolFor(PieceType type, Color color) {
        Map<PieceType, String> symbols = color == Color.WHITE ? whiteSymbols : blackSymbols;
        String symbol = symbols.get(type);
        return symbol != null ? symbol : fallbackSymbol(type, color);
    }

    private String fallbackSymbol(PieceType type, Color color) {
        String code = type.notationCode();
        return color == Color.WHITE ? code.toUpperCase() : code.toLowerCase();
    }
}