package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.squares;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.port.IGameService;

/**
 * A bare king against a king, or against a lone knight, is a draw, so these positions carry the extra material needed
 * to keep the game going.
 */
final class KingMoveTest {

    private static final String EMPTY = "........";

    @Test
    void kingMovesOneSquareInAllEightDirections() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY, EMPTY,
                "...K....",
                EMPTY,
                "P.......",
                EMPTY);

        assertEquals(
                squares("c3", "c4", "c5", "d3", "d5", "e3", "e4", "e5"),
                Set.copyOf(game.legalMoves(at("d4"))));
    }

    @Test
    void kingCannotMoveToAnAttackedSquare() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY,
                "r.......",
                "....K...",
                EMPTY, EMPTY, EMPTY);

        assertEquals(
                squares("d3", "e3", "f3", "d4", "f4"),
                Set.copyOf(game.legalMoves(at("e4"))));
    }

    @Test
    void kingCannotCaptureAProtectedPiece() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY,
                "...p....",
                "....n...",
                "....K...",
                EMPTY, EMPTY, EMPTY);

        assertFalse(game.legalMoves(at("e4")).contains(at("e5")));
    }

    @Test
    void kingCanCaptureAnUnprotectedEnemyPiece() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY,
                "....n...",
                "....K...",
                EMPTY,
                "P.......",
                EMPTY);

        assertTrue(game.legalMoves(at("e4")).contains(at("e5")));
    }
}
