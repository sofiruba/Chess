package com.chess.domain.rule;

import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.Position;


public interface ICheckDetector {

    boolean isInCheck(Board board, Color color);

    /** Devuelve la posición del rey del color indicado si está en jaque, o null en caso contrario. */
    Position kingInCheckPosition(Board board, Color color);

    boolean isSquareAttacked(Board board, Position position, Color attackingColor);
}
