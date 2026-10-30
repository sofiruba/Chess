package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.squares;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.port.IGameService;

final class QueenMoveTest {

    private static final String EMPTY = "........";

    @Test
    void queenCombinesRookAndBishopMovesOnAnOpenBoard() {
        IGameService game = openBoardWithQueenOnD4();

        assertEquals(
                squares(
                        "d1", "d2", "d3", "d5", "d6", "d7", "d8", "a4", "b4", "c4", "e4", "f4", "g4", "h4",
                        "a1", "b2", "c3", "e5", "f6", "g7", "h8", "a7", "b6", "c5", "e3", "f2", "g1"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }

    @Test
    void queenStopsBeforeItsOwnPieces() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY,
                "...P.P..",
                EMPTY,
                "...Q....",
                EMPTY, EMPTY,
                ".......K");

        assertEquals(
                squares(
                        "d5", "d3", "d2", "d1", "a4", "b4", "c4", "e4", "f4", "g4", "h4",
                        "a1", "b2", "c3", "e5", "a7", "b6", "c5", "e3", "f2", "g1"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }

    @Test
    void queenCapturesEnemyPiecesAndStopsThere() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY,
                "...p.p..",
                EMPTY,
                "...Q....",
                EMPTY, EMPTY,
                ".......K");

        assertEquals(
                squares(
                        "d5", "d6", "d3", "d2", "d1", "a4", "b4", "c4", "e4", "f4", "g4", "h4",
                        "a1", "b2", "c3", "e5", "f6", "a7", "b6", "c5", "e3", "f2", "g1"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }

    @Test
    void queenCannotMoveLikeAKnight() {
        IGameService game = openBoardWithQueenOnD4();

        for (String knightJump : new String[]{"e6", "f5", "f3", "e2", "c2", "b3", "b5", "c6"}) {
            assertFalse(game.legalMoves(at("d4")).contains(at(knightJump)), knightJump);
        }
    }

    private static IGameService openBoardWithQueenOnD4() {
        return TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY, EMPTY, EMPTY,
                "...Q....",
                EMPTY, EMPTY,
                ".......K");
    }
}
