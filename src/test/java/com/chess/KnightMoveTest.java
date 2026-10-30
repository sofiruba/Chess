package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.squares;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.port.IGameService;

/**
 * A lone knight against a bare king is a draw, so every position here has one extra pawn to keep the game going.
 */
final class KnightMoveTest {

    private static final String EMPTY = "........";

    @Test
    void knightInTheCenterHasEightDestinations() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY, EMPTY,
                "...N....",
                EMPTY,
                "P.......",
                ".......K");

        assertEquals(
                squares("e6", "f5", "f3", "e2", "c2", "b3", "b5", "c6"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }

    @Test
    void knightInACornerHasTwoDestinations() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY,
                "....P...",
                EMPTY, EMPTY, EMPTY,
                "N......K");

        assertEquals(squares("b3", "c2"), Set.copyOf(game.legalMoves(at("a1"))));
    }

    @Test
    void knightJumpsOverSurroundingPieces() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY,
                "..PPP...",
                "..PNP...",
                "..PPP...",
                EMPTY,
                ".......K");

        assertEquals(
                squares("e6", "f5", "f3", "e2", "c2", "b3", "b5", "c6"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }

    @Test
    void knightCannotCaptureOwnPiece() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY,
                "....P...",
                ".....P..",
                "...N....",
                EMPTY,
                "P.......",
                ".......K");

        assertEquals(
                squares("f3", "e2", "c2", "b3", "b5", "c6"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }

    @Test
    void knightCapturesAnEnemyPiece() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY,
                "....p...",
                EMPTY,
                "...N....",
                EMPTY,
                "P.......",
                ".......K");

        assertEquals(
                squares("e6", "f5", "f3", "e2", "c2", "b3", "b5", "c6"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }
}
