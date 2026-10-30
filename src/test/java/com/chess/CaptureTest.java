package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.play;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.model.Piece;
import com.chess.domain.model.PieceType;
import com.chess.domain.port.IGameService;

final class CaptureTest {

    private static final String EMPTY = "........";

    @Test
    void captureRemovesTheEnemyPieceAndListsItAsCaptured() {
        IGameService game = rookFacingAnEnemyPawn("p");

        play(game, "d4d6");

        assertEquals(PieceType.ROOK, game.pieceAt(at("d6")).type());
        assertNull(game.pieceAt(at("d4")));
        List<Piece> captured = game.capturedPieces();
        assertEquals(1, captured.size());
        assertEquals(PieceType.PAWN, captured.get(0).type());
        assertEquals(Color.BLACK, captured.get(0).color());
    }

    @Test
    void movingWithoutCapturingLeavesCapturedPiecesEmpty() {
        IGameService game = rookFacingAnEnemyPawn("p");

        play(game, "d4d5");

        assertEquals(List.of(), game.capturedPieces());
    }

    @Test
    void capturingAnOwnPieceThrowsAndLeavesTheBoardUnchanged() {
        IGameService game = rookFacingAnEnemyPawn("P");

        assertThrows(IllegalArgumentException.class, () -> game.playMove(at("d4"), at("d6")));

        assertEquals(PieceType.ROOK, game.pieceAt(at("d4")).type());
        assertEquals(Color.WHITE, game.pieceAt(at("d6")).color());
        assertEquals(List.of(), game.capturedPieces());
    }

    @Test
    void undoRestoresTheCapturedPiece() {
        IGameService game = rookFacingAnEnemyPawn("p");
        play(game, "d4d6");

        game.undoLastMove();

        assertEquals(PieceType.ROOK, game.pieceAt(at("d4")).type());
        assertEquals(PieceType.PAWN, game.pieceAt(at("d6")).type());
        assertEquals(Color.BLACK, game.pieceAt(at("d6")).color());
        assertEquals(List.of(), game.capturedPieces());
    }

    @Test
    void redoCapturesThePieceAgain() {
        IGameService game = rookFacingAnEnemyPawn("p");
        play(game, "d4d6");
        game.undoLastMove();

        game.redoLastMove();

        assertEquals(PieceType.ROOK, game.pieceAt(at("d6")).type());
        assertNull(game.pieceAt(at("d4")));
        assertEquals(1, game.capturedPieces().size());
        assertEquals(PieceType.PAWN, game.capturedPieces().get(0).type());
    }

    /** White rook on d4 and the given piece on d6, with both kings far away. */
    private static IGameService rookFacingAnEnemyPawn(String pieceOnD6) {
        return TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY,
                "..." + pieceOnD6 + "....",
                EMPTY,
                "...R....",
                EMPTY, EMPTY,
                ".......K");
    }
}
