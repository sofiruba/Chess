package com.chess.domain.rule;

import com.chess.domain.model.Piece;

/**
 * Restricciones temporales sobre piezas concretas que el generador de jugadas tiene que respetar. Las reglas solo
 * conocen esta interfaz; quién congela o protege una pieza (por ejemplo, los poderes de las cajas misteriosas) queda
 * fuera de este paquete, por lo que las reglas no dependen de esa mecánica.
 */
public interface IPieceStatus {

    /** Ninguna pieza tiene restricciones: el ajedrez clásico. */
    IPieceStatus NONE = new IPieceStatus() {
        @Override
        public boolean canAct(Piece piece) {
            return true;
        }

        @Override
        public boolean canBeCaptured(Piece piece) {
            return true;
        }
    };

    /** Es falso cuando la pieza no puede moverse ni usar poderes en este turno (por ejemplo, porque está congelada). */
    boolean canAct(Piece piece);

    /** Es falso cuando la pieza no puede ser capturada ni eliminada (por ejemplo, porque está protegida). */
    boolean canBeCaptured(Piece piece);
}
