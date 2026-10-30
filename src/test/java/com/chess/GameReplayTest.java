package com.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.chess.configuration.DefaultMoveCommandFactory;
import com.chess.configuration.StandardGameFactory;
import com.chess.domain.model.Color;
import com.chess.domain.port.IGameFactory;
import com.chess.domain.port.IGameService;

final class GameReplayTest {

    private final IGameFactory gameFactory =
            new StandardGameFactory(new DefaultMoveCommandFactory());

    @Test
    void noMoveCodesGiveANewGame() {
        IGameService game = gameFactory.create(List.of());

        assertEquals(List.of(), game.moveCodes());
        assertEquals(Color.WHITE, game.turn());
    }

    @Test
    void theMovesAreReplayedInOrder() {
        IGameService game = gameFactory.create(List.of("e2e4", "e7e5", "g1f3"));

        assertEquals(List.of("e2e4", "e7e5", "g1f3"), game.moveCodes());
        assertEquals(Color.BLACK, game.turn());
    }

    @Test
    void aReplayedGameCanBeUndoneAndRedone() {
        IGameService game = gameFactory.create(List.of("e2e4", "e7e5"));

        game.undoLastMove();
        game.redoLastMove();

        assertEquals(List.of("e2e4", "e7e5"), game.moveCodes());
    }

    @Test
    void anIllegalMoveCodeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> gameFactory.create(List.of("e2e5")));
    }

    @Test
    void oldDisplayNotationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> gameFactory.create(List.of("e2e4", "O-O")));
    }

    @Test
    void nullMoveCodesAreRejected() {
        assertThrows(NullPointerException.class, () -> gameFactory.create(null));
    }
        @Test
    void anIllegalMoveCodeReportsItsMoveNumber() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> gameFactory.create(List.of("e2e4", "e7e5", "e2e5", "g8f6")));

        assertTrue(error.getMessage().contains("Move 3"), error.getMessage());
        assertTrue(error.getMessage().contains("e2e5"), error.getMessage());
    }

    @Test
    void aMalformedMoveCodeReportsItsMoveNumber() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> gameFactory.create(List.of("e2e4", "banana", "g1f3")));

        assertTrue(error.getMessage().contains("Move 2"), error.getMessage());
    }

    @Test
    void replaysCastlingEnPassantAndPromotionInOneGame() {
        List<String> moveCodes = List.of(
                "e2e4",
                "a7a6",
                "e4e5",
                "d7d5",
                "e5d6",
                "c7c6",
                "g1f3",
                "b7b6",
                "f1e2",
                "f7f6",
                "e1g1",
                "c8b7",
                "a2a3",
                "d8c8",
                "d6d7",
                "e8f7",
                "d7d8q");

        IGameService game = gameFactory.create(moveCodes);

        assertEquals(moveCodes, game.moveCodes());
        assertEquals(Color.WHITE, game.pieceAt(TestGames.at("g1")).color());
        assertEquals("QUEEN", game.pieceAt(TestGames.at("d8")).type().name());
    }
}
