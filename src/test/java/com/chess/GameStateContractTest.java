package com.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.chess.domain.GameResult;
import com.chess.domain.IGameState;

/**
 * What every {@link IGameState} promises, whichever implementation the game holds (Liskov). Each implementation has
 * a subclass that supplies the state to check.
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
