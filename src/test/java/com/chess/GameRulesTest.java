package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.play;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.model.GameResult;
import com.chess.domain.port.IGameService;
import com.chess.domain.model.PieceType;

final class GameRulesTest {

    private static final String EMPTY = "........";

    @Test
    void turnsAlternateAndUndoRedoRestoreThem() {
        IGameService game = TestGames.standard();

        play(game, "e2e4");
        assertEquals(Color.BLACK, game.turn());
        game.undoLastMove();
        assertEquals(Color.WHITE, game.turn());
        game.redoLastMove();
        assertEquals(Color.BLACK, game.turn());
    }

    @Test
    void movingAPinnedPieceOffTheLineIsIllegal() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k...r...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "....R...",
                "....K...");

        assertFalse(game.legalMoves(at("e2")).contains(at("d2")));
        assertTrue(game.legalMoves(at("e2")).contains(at("e5")));
    }

    @Test
    void detectsFoolsMate() {
        IGameService game = TestGames.standard();

        play(game, "f2f3", "e7e5", "g2g4", "d8h4");

        assertTrue(game.isFinished());
        assertEquals(GameResult.BLACK_WINS, game.result());
    }

    @Test
    void enPassantIsAvailableAfterADoubleStepAndSurvivesUndoRedo() {
        IGameService game = TestGames.standard();
        play(game, "e2e4", "a7a6", "e4e5", "d7d5");
        assertTrue(game.legalMoves(at("e5")).contains(at("d6")));

        play(game, "e5d6");
        assertNull(game.pieceAt(at("d5")));
        game.undoLastMove();
        assertTrue(game.legalMoves(at("e5")).contains(at("d6")));
        game.redoLastMove();

        assertNull(game.pieceAt(at("d5")));
    }

    @Test
    void promotionUsesTheChosenPiece() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "....k...",
                "P.......",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "....K...");
        assertTrue(game.requiresPromotion(at("a7"), at("a8")));

        play(game, "a7a8r");

        assertEquals(PieceType.ROOK, game.pieceAt(at("a8")).type());
        assertEquals(java.util.List.of("a7a8r"), game.moveNotation());
    }

    @Test
    void canCastleShortAfterClearingTheKingSide() {
        IGameService game = TestGames.standard();
        play(game, "e2e3", "a7a6", "g1f3", "a6a5", "f1e2", "a5a4");
        assertTrue(game.legalMoves(at("e1")).contains(at("g1")));

        play(game, "e1g1");

        assertEquals(PieceType.KING, game.pieceAt(at("g1")).type());
        assertEquals(PieceType.ROOK, game.pieceAt(at("f1")).type());
    }

    @Test
    void movingTheRookAwayAndBackDoesNotRestoreCastling() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "....k...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "....K..R");

        play(game, "h1h2", "e8e7", "h2h1", "e7e8");

        assertFalse(game.legalMoves(at("e1")).contains(at("g1")));
    }
}
