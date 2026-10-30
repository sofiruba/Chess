package com.chess.domain.model;

import java.util.Objects;

/**
 * Las piezas a las que las reglas del núcleo les atribuyen un papel (rey, torre, peón, alfil y caballo). La reina no
 * tiene rol porque ninguna regla depende de ella: solo la conocen la configuración y la interfaz.
 */
public record PieceRoles(
        PieceType king,
        PieceType rook,
        PieceType pawn,
        PieceType bishop,
        PieceType knight) {

    public PieceRoles {
        Objects.requireNonNull(king, "king must not be null");
        Objects.requireNonNull(rook, "rook must not be null");
        Objects.requireNonNull(pawn, "pawn must not be null");
        Objects.requireNonNull(bishop, "bishop must not be null");
        Objects.requireNonNull(knight, "knight must not be null");
    }

    public static PieceRoles standard() {
        return new PieceRoles(
                PieceType.KING,
                PieceType.ROOK,
                PieceType.PAWN,
                PieceType.BISHOP,
                PieceType.KNIGHT);
    }
}