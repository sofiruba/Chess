package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.play;
import static com.chess.TestGames.squares;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.model.PieceType;
import com.chess.domain.port.IGameService;

final class EnPassantTest {

    private static final String EMPTY = "........";

    @Test
    void enPassantIsAvailableRightAfterTheDoubleStep() {
        IGameService game = whitePawnOnE5FacingBlackPawnOnD7();

        play(game, "d7d5");

        assertEquals(squares("e6", "d6"), Set.copyOf(game.legalMoves(at("e5"))));
    }

    @Test
    void enPassantIsLostWhenAnotherMoveIsPlayedInBetween() {
        IGameService game = whitePawnOnE5FacingBlackPawnOnD7();

        play(game, "d7d5", "a1a2", "a8a7");

        assertEquals(squares("e6"), Set.copyOf(game.legalMoves(at("e5"))));
    }

    @Test
    void enPassantRemovesTheCapturedPawn() {
        IGameService game = whitePawnOnE5FacingBlackPawnOnD7();

        play(game, "d7d5", "e5d6");

        assertEquals(PieceType.PAWN, game.pieceAt(at("d6")).type());
        assertEquals(Color.WHITE, game.pieceAt(at("d6")).color());
        assertNull(game.pieceAt(at("d5")));
        assertNull(game.pieceAt(at("e5")));
        assertEquals(1, game.capturedPieces().size());
        assertEquals(Color.BLACK, game.capturedPieces().get(0).color());
    }

    @Test
    void enPassantWorksForBlack() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY, EMPTY, EMPTY,
                "...p....",
                EMPTY,
                "....P...",
                "K.......");

        play(game, "e2e4");
        assertEquals(squares("d3", "e3"), Set.copyOf(game.legalMoves(at("d4"))));
        play(game, "d4e3");

        assertEquals(PieceType.PAWN, game.pieceAt(at("e3")).type());
        assertEquals(Color.BLACK, game.pieceAt(at("e3")).color());
        assertNull(game.pieceAt(at("e4")));
        assertEquals(Color.WHITE, game.capturedPieces().get(0).color());
    }

    @Test
    void enPassantOnlyFollowsAPawnDoubleStep() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY, EMPTY, EMPTY,
                ".p......",
                EMPTY,
                "R......K");

        play(game, "a1a3");

        assertEquals(squares("b2"), Set.copyOf(game.legalMoves(at("b3"))));
    }

    @Test
    void enPassantIsIllegalWhenItWouldExposeTheOwnKingAlongTheRank() {
        IGameService game = TestGames.fromDiagram(Color.BLACK,
                ".......k",
                "....p...",
                EMPTY,
                "K..P...r",
                EMPTY, EMPTY, EMPTY, EMPTY);

        play(game, "e7e5");

        assertEquals(squares("d6"), Set.copyOf(game.legalMoves(at("d5"))));
        assertThrows(IllegalArgumentException.class, () -> game.playMove(at("d5"), at("e6")));
        assertEquals(PieceType.PAWN, game.pieceAt(at("e5")).type());
    }

    @Test
    void undoRestoresBothPawnsAndTheCaptureOption() {
        IGameService game = whitePawnOnE5FacingBlackPawnOnD7();
        play(game, "d7d5", "e5d6");

        game.undoLastMove();

        assertEquals(Color.WHITE, game.pieceAt(at("e5")).color());
        assertEquals(Color.BLACK, game.pieceAt(at("d5")).color());
        assertNull(game.pieceAt(at("d6")));
        assertEquals(0, game.capturedPieces().size());
        assertEquals(squares("e6", "d6"), Set.copyOf(game.legalMoves(at("e5"))));
    }

    @Test
    void redoCapturesEnPassantAgain() {
        IGameService game = whitePawnOnE5FacingBlackPawnOnD7();
        play(game, "d7d5", "e5d6");
        game.undoLastMove();

        game.redoLastMove();

        assertEquals(Color.WHITE, game.pieceAt(at("d6")).color());
        assertNull(game.pieceAt(at("d5")));
        assertNull(game.pieceAt(at("e5")));
        assertEquals(1, game.capturedPieces().size());
    }

    private static IGameService whitePawnOnE5FacingBlackPawnOnD7() {
        return TestGames.fromDiagram(Color.BLACK,
                "k.......",
                "...p....",
                EMPTY,
                "....P...",
                EMPTY, EMPTY, EMPTY,
                "K.......");
    }
}
