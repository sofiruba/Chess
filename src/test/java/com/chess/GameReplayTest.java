package com.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.configuration.DefaultMoveCommandFactory;
import com.chess.configuration.StandardGameFactory;
import com.chess.domain.model.Color;
import com.chess.domain.game.GameReplayer;
import com.chess.domain.port.IGameReplayer;
import com.chess.domain.port.IGameService;

final class GameReplayTest {

    private final IGameReplayer replayer =
            new GameReplayer(new StandardGameFactory(new DefaultMoveCommandFactory()));

    @Test
    void noMoveCodesGiveANewGame() {
        IGameService game = replayer.replay(List.of());

        assertEquals(List.of(), game.moveCodes());
        assertEquals(Color.WHITE, game.turn());
    }

    @Test
    void theMovesAreReplayedInOrder() {
        IGameService game = replayer.replay(List.of("e2e4", "e7e5", "g1f3"));

        assertEquals(List.of("e2e4", "e7e5", "g1f3"), game.moveCodes());
        assertEquals(Color.BLACK, game.turn());
    }

    @Test
    void aReplayedGameCanBeUndoneAndRedone() {
        IGameService game = replayer.replay(List.of("e2e4", "e7e5"));

        game.undoLastMove();
        game.redoLastMove();

        assertEquals(List.of("e2e4", "e7e5"), game.moveCodes());
    }

    @Test
    void anIllegalMoveCodeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> replayer.replay(List.of("e2e5")));
    }

    @Test
    void oldDisplayNotationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> replayer.replay(List.of("e2e4", "O-O")));
    }

    @Test
    void nullMoveCodesAreRejected() {
        assertThrows(NullPointerException.class, () -> replayer.replay(null));
    }
}
