package com.chess.domain.port;

import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;


/**
 * Las formas de modificar una partida.
 */
public interface IGameActions {
    void playMove(Position from, Position to);
    void playMove(Position from, Position to, PieceType promotionType);
    /** Juega una jugada o un uso de poder a partir de su código ("e2e4", "e7e8q", "@e4e7"). */
    void playMoveCode(String moveCode);

    /**
     * Usa el poder de la pieza en {@code holder} sobre {@code target}. Ocupa el turno, como una jugada.
     *
     * @throws IllegalArgumentException si {@code target} no está en {@link IGameQueries#powerTargets(Position)}
     */
    void usePower(Position holder, Position target);
    void undoLastMove();
    void redoLastMove();
}
