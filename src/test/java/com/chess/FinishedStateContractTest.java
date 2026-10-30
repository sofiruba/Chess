package com.chess;

import static com.chess.TestGames.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.state.FinishedState;
import com.chess.domain.model.GameResult;
import com.chess.domain.state.IGameState;
import com.chess.domain.state.TurnStateFactory;

final class FinishedStateContractTest extends GameStateContractTest {

    private final TurnStateFactory states = new TurnStateFactory(new StubMoveGenerator(List.of(at("a3"))));

    @Override
    IGameState state() {
        return states.checkmate(Color.WHITE);
    }

    @Test
    void itIsFinishedWithItsResult() {
        assertTrue(state().isFinished());
        assertEquals(GameResult.BLACK_WINS, states.checkmate(Color.WHITE).result());
        assertEquals(GameResult.WHITE_WINS, states.checkmate(Color.BLACK).result());
        assertEquals(GameResult.DRAW, states.stalemate(Color.WHITE).result());
        assertEquals(GameResult.DRAW, states.draw(Color.BLACK).result());
    }

    @Test
    void itKeepsThePlayerWhoWouldHaveMoved() {
        assertEquals(Color.WHITE, states.checkmate(Color.WHITE).activePlayerColor());
        assertEquals(Color.BLACK, states.stalemate(Color.BLACK).activePlayerColor());
    }

    @Test
    void itRejectsAnyMoveWithoutExecutingItAndSaysWhyTheGameEnded() {
        RecordingCommand whiteMove = new RecordingCommand(Color.WHITE);
        RecordingCommand blackMove = new RecordingCommand(Color.BLACK);

        IllegalStateException checkmate =
                assertThrows(IllegalStateException.class, () -> states.checkmate(Color.WHITE).handleMove(whiteMove));
        IllegalStateException stalemate =
                assertThrows(IllegalStateException.class, () -> states.stalemate(Color.BLACK).handleMove(blackMove));
        IllegalStateException draw =
                assertThrows(IllegalStateException.class, () -> states.draw(Color.WHITE).handleMove(blackMove));

        assertEquals(0, whiteMove.executions());
        assertEquals(0, blackMove.executions());
        assertTrue(checkmate.getMessage().contains("checkmate"));
        assertTrue(stalemate.getMessage().contains("stalemate"));
        assertTrue(draw.getMessage().contains("draw"));
    }

    @Test
    void itHasNoLegalMoves() {
        assertTrue(state().legalMoves(null, at("a2")).isEmpty());
    }

    @Test
    void itCannotBeBuiltAsInProgress() {
        assertThrows(IllegalArgumentException.class,
                () -> new FinishedState(Color.WHITE, GameResult.IN_PROGRESS, "none"));
    }
}
