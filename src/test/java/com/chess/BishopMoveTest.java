package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.squares;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.port.IGameService;

final class BishopMoveTest {

    private static final String EMPTY = "........";

    @Test
    void bishopMovesAlongAllFourDiagonals() {
        IGameService game = openBoardWithBishopOnD4();

        assertEquals(
                squares("a1", "b2", "c3", "e5", "f6", "g7", "h8", "a7", "b6", "c5", "e3", "f2", "g1"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }

    @Test
    void bishopStopsBeforeItsOwnPiece() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY,
                ".....P..",
                EMPTY,
                "...B....",
                EMPTY, EMPTY,
                ".......K");

        assertEquals(
                squares("a1", "b2", "c3", "e5", "a7", "b6", "c5", "e3", "f2", "g1"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }

    @Test
    void bishopCapturesAnEnemyPieceAndStopsThere() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY,
                ".....p..",
                EMPTY,
                "...B....",
                EMPTY, EMPTY,
                ".......K");

        assertEquals(
                squares("a1", "b2", "c3", "e5", "f6", "a7", "b6", "c5", "e3", "f2", "g1"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }

    @Test
    void bishopCannotMoveInStraightLines() {
        IGameService game = openBoardWithBishopOnD4();

        for (String straight : new String[]{"d5", "d3", "c4", "e4"}) {
            assertFalse(game.legalMoves(at("d4")).contains(at(straight)), straight);
        }
    }

    /** A lone bishop against a bare king is a draw, so a white pawn on a2 keeps the game going. */
    private static IGameService openBoardWithBishopOnD4() {
        return TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY, EMPTY, EMPTY,
                "...B....",
                EMPTY,
                "P.......",
                ".......K");
    }
}
