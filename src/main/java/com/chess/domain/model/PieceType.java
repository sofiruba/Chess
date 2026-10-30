package com.chess.domain.model;



import java.util.Objects;

/**
 * Identifies a kind of piece. It is an open type instead of an enum so a new piece can be defined outside this
 * class. Equality is by identity: create each type once and share the instance.
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
     * @param name unique readable name of the piece
     * @param notationCode letter used to write this piece in a promotion, such as "q" in "e7e8q"
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
