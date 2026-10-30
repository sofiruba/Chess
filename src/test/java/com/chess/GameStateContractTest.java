package com.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.GameResult;
import com.chess.domain.state.IGameState;

/**
 * Lo que garantiza cada {@link IGameState}, sin importar la implementación que mantenga la partida (Liskov). Cada
 * implementación tiene una subclase que proporciona el estado a comprobar.
 */
abstract class GameStateContractTest {

    abstract IGameState state();

    @Test
    void itAlwaysHasAnActivePlayer() {
        assertNotNull(state().activePlayerColor());
    }

    @Test
    void itIsFinishedExactlyWhenTheResultIsNotInProgress() {
        IGameState state = state();

        assertEquals(state.result() != GameResult.IN_PROGRESS, state.isFinished());
    }

    @Test
    void itNeverReturnsNullLegalMoves() {
        assertNotNull(state().legalMoves(null, null));
    }
}
