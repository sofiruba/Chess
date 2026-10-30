package com.chess;

import static com.chess.TestGames.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.domain.Color;
import com.chess.domain.GameResult;
import com.chess.domain.IGameState;
import com.chess.domain.Position;
import com.chess.domain.TurnStateFactory;

final class TurnStateContractTest extends GameStateContractTest {

    private final StubMoveGenerator generator = new StubMoveGenerator(List.of(at("a3"), at("a4")));
    private final TurnStateFactory states = new TurnStateFactory(generator);

    @Override
    IGameState state() {
        return states.forColor(Color.WHITE);
    }

    @Test
    void itIsInProgress() {
        assertFalse(state().isFinished());
        assertEquals(GameResult.IN_PROGRESS, state().result());
    }

    @Test
    void itExecutesTheMoveOfItsPlayerOnceAndPassesTheTurn() {
        RecordingCommand whiteMove = new RecordingCommand(Color.WHITE);

        IGameState next = state().handleMove(whiteMove);

        assertEquals(1, whiteMove.executions());
        assertEquals(Color.BLACK, next.activePlayerColor());
        assertFalse(next.isFinished());
    }

    @Test
    void itRejectsTheMoveOfTheOtherPlayerWithoutExecutingIt() {
        RecordingCommand blackMove = new RecordingCommand(Color.BLACK);

        assertThrows(IllegalArgumentException.class, () -> state().handleMove(blackMove));

        assertEquals(0, blackMove.executions());
    }

    @Test
    void itRejectsANullMove() {
        assertThrows(NullPointerException.class, () -> state().handleMove(null));
    }

    @Test
    void itAsksTheGeneratorForItsOwnPlayer() {
        List<Position> moves = states.forColor(Color.BLACK).legalMoves(null, at("e7"));

        assertEquals(List.of(at("a3"), at("a4")), moves);
        assertEquals(Color.BLACK, generator.askedPlayer());
        assertEquals(at("e7"), generator.askedFrom());
    }
}
