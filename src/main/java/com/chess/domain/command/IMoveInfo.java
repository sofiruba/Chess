package com.chess.domain.command;

import com.chess.domain.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Vista de solo lectura de una jugada. Las reglas, los contadores de tablas y las consultas reciben este tipo, por
 * lo que pueden inspeccionar la última jugada, pero no ejecutarla ni deshacerla; esa facultad pertenece a {@link ICommand}.
 */
public interface IMoveInfo {

    Position from();

    Position to();

    Piece movingPiece();

    Piece capturedPiece();

    /** Texto mostrado al jugador. No sirve para reconstruir una jugada; para eso se usa {@link #moveCode()}. */
    String notation();

    /**
     * Código usado para persistir y reproducir una jugada: origen, destino y una letra de promoción opcional
     * ("e1g1", "e7e8q"). El enroque es el movimiento del rey y la captura al paso es el del peón, por lo que el
     * código basta para reconstruirlos.
     */
    default String moveCode() {
        return Notation.square(from()) + Notation.square(to());
    }

    default Color movingColor() {
        return movingPiece().color();
    }

    default List<MovedPiece> movedPieces() {
        return List.of(new MovedPiece(movingPiece(), from()));
    }

    default Optional<Position> enPassantSquare() {
        return enPassantSquare(PieceRoles.standard());
    }

    default Optional<Position> enPassantSquare(PieceRoles roles) {
        if (!isPawnDoubleStep(roles)) {
            return Optional.empty();
        }
        return Optional.of(new Position(to().x(), (from().y() + to().y()) / 2));
    }

    private boolean isPawnDoubleStep(PieceRoles roles) {
        return movingPiece().type() == roles.pawn() && Math.abs(to().y() - from().y()) == 2;
    }

    record MovedPiece(Piece piece, Position from) {
    }
}
