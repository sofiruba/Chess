package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.play;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.model.GameResult;
import com.chess.domain.port.IGameService;

final class CheckmateTest {

    private static final String EMPTY = "........";

    @Test
    void scholarsMateEndsTheGameWithAWhiteWin() {
        IGameService game = TestGames.standard();

        play(game, "e2e4", "e7e5", "d1h5", "b8c6", "f1c4", "g8f6", "h5f7");

        assertTrue(game.isInCheck(Color.BLACK));
        assertTrue(game.isFinished());
        assertEquals(GameResult.WHITE_WINS, game.result());
    }

    @Test
    void backRankMateEndsTheGameWithABlackWin() {
        IGameService game = TestGames.fromDiagram(Color.BLACK,
                "r...k...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                ".....PPP",
                "......K.");
        assertFalse(game.isFinished());

        play(game, "a8a1");

        assertTrue(game.isInCheck(Color.WHITE));
        assertTrue(game.isFinished());
        assertEquals(GameResult.BLACK_WINS, game.result());
    }

    @Test
    void checkThatCanBeEscapedIsNotCheckmate() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k...r...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "....K...");

        assertTrue(game.isInCheck(Color.WHITE));
        assertFalse(game.isFinished());
        assertEquals(GameResult.IN_PROGRESS, game.result());
        assertTrue(game.legalMoves(at("e1")).contains(at("d1")));
    }

    @Test
    void checkThatCanBeBlockedIsNotCheckmate() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "....k...",
                EMPTY, EMPTY, EMPTY, EMPTY,
                "..R.....",
                ".....PPP",
                "r.....K.");

        assertTrue(game.isInCheck(Color.WHITE));
        assertFalse(game.isFinished());
        assertEquals(GameResult.IN_PROGRESS, game.result());
        assertTrue(game.legalMoves(at("c3")).contains(at("c1")));
    }

    @Test
    void checkThatCanBeAnsweredByCapturingTheAttackerIsNotCheckmate() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "....k...",
                EMPTY, EMPTY, EMPTY, EMPTY,
                "R.......",
                ".....PPP",
                "r.....K.");

        assertTrue(game.isInCheck(Color.WHITE));
        assertFalse(game.isFinished());
        assertEquals(GameResult.IN_PROGRESS, game.result());
        assertTrue(game.legalMoves(at("a3")).contains(at("a1")));
    }
}
