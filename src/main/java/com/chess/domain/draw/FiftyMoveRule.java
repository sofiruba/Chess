package com.chess.domain.draw;

import com.chess.domain.model.Board;

/**
 * Tablas tras la cantidad indicada de jugadas de cada jugador (50 en ajedrez estándar) sin mover un peón ni capturar.
 * Se aplica automáticamente, sin necesidad de reclamación.
 */
public final class FiftyMoveRule implements IDrawRule {

    private static final int PLAYERS = 2;

    private final int halfMoveLimit;

    /**
     * @param moveLimit jugadas de cada jugador; el límite en medias jugadas es el doble de este valor
     */
    public FiftyMoveRule(int moveLimit) {
        if (moveLimit < 1) {
            throw new IllegalArgumentException("moveLimit must be positive: " + moveLimit);
        }
        this.halfMoveLimit = moveLimit * PLAYERS;
    }

    @Override
    public boolean isDraw(Board board, DrawCounters counters) {
        return counters.halfMoveClock() >= halfMoveLimit;
    }
}
