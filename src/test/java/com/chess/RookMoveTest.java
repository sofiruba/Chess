package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.squares;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.port.IGameService;

final class RookMoveTest {

    private static final String EMPTY = "........";

    @Test
    void rookOnAnOpenBoardReachesFourteenSquares() {
        IGameService game = openBoardWithRookOnD4();

        assertEquals(
                squares("d1", "d2", "d3", "d5", "d6", "d7", "d8", "a4", "b4", "c4", "e4", "f4", "g4", "h4"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }

    @Test
    void rookStopsBeforeItsOwnPieces() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY,
                "...P....",
                EMPTY,
                "...R.P..",
                EMPTY, EMPTY,
                ".......K");

        assertEquals(
                squares("d5", "d3", "d2", "d1", "c4", "b4", "a4", "e4"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }

    @Test
    void rookCapturesAnEnemyPieceAndStopsThere() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY,
                "...p....",
                EMPTY,
                "...R....",
                EMPTY, EMPTY,
                ".......K");

        assertEquals(
                squares("d5", "d6", "d3", "d2", "d1", "c4", "b4", "a4", "e4", "f4", "g4", "h4"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }

    @Test
    void rookCannotMoveDiagonally() {
        IGameService game = openBoardWithRookOnD4();

        for (String diagonal : new String[]{"e5", "e3", "c5", "c3"}) {
            assertFalse(game.legalMoves(at("d4")).contains(at(diagonal)), diagonal);
        }
    }

    private static IGameService openBoardWithRookOnD4() {
        return TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY, EMPTY, EMPTY,
                "...R....",
                EMPTY, EMPTY,
                ".......K");
    }
}
