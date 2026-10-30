package com.chess.domain.model;



import java.util.Objects;

/**
 * Identifica un tipo de pieza. Es un tipo abierto en lugar de un enum para poder definir una pieza nueva fuera de
 * esta clase. La igualdad es por identidad: cada tipo se crea una vez y se comparte la instancia.
 */
public final class PieceType {

    public static final PieceType KING = new PieceType("KING", "k");
    public static final PieceType QUEEN = new PieceType("QUEEN", "q");
    public static final PieceType ROOK = new PieceType("ROOK", "r");
    public static final PieceType BISHOP = new PieceType("BISHOP", "b");
    public static final PieceType KNIGHT = new PieceType("KNIGHT", "n");
    public static final PieceType PAWN = new PieceType("PAWN", "p");

    private final String name;
    private final String notationCode;

    /**
     * @param name nombre único y legible de la pieza
     * @param notationCode letra usada para escribir esta pieza en una promoción, como "q" en "e7e8q"
     */
    public PieceType(String name, String notationCode) {
        this.name = requireText(name, "name");
        this.notationCode = requireText(notationCode, "notationCode");
    }

    public String name() {
        return name;
    }

    public String notationCode() {
        return notationCode;
    }

    @Override
    public String toString() {
        return name;
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
