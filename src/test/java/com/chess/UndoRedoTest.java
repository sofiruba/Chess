package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.play;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.model.GameResult;
import com.chess.domain.model.PieceType;
import com.chess.domain.port.IGameService;

final class UndoRedoTest {

    private static final String EMPTY = "........";

    @Test
    void undoAndRedoACapture() {
        IGameService game = TestGames.standard();
        play(game, "e2e4", "d7d5", "e4d5");

        game.undoLastMove();

        assertEquals(Color.WHITE, game.pieceAt(at("e4")).color());
        assertEquals(Color.BLACK, game.pieceAt(at("d5")).color());
        assertEquals(Color.WHITE, game.turn());
        assertEquals(List.of(), game.capturedPieces());
        assertEquals(List.of("e2e4", "d7d5"), game.moveCodes());

        game.redoLastMove();

        assertEquals(Color.WHITE, game.pieceAt(at("d5")).color());
        assertNull(game.pieceAt(at("e4")));
        assertEquals(Color.BLACK, game.turn());
        assertEquals(1, game.capturedPieces().size());
        assertEquals(List.of("e2e4", "d7d5", "e4d5"), game.moveCodes());
    }

    @Test
    void undoAndRedoCastling() {
        IGameService game = TestGames.standard();
        play(game, "e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "g8f6", "e1g1");

        game.undoLastMove();

        assertEquals(PieceType.KING, game.pieceAt(at("e1")).type());
        assertEquals(PieceType.ROOK, game.pieceAt(at("h1")).type());
        assertNull(game.pieceAt(at("f1")));
        assertNull(game.pieceAt(at("g1")));
        assertEquals(Color.WHITE, game.turn());
        assertEquals(6, game.moveCodes().size());

        game.redoLastMove();

        assertEquals(PieceType.KING, game.pieceAt(at("g1")).type());
        assertEquals(PieceType.ROOK, game.pieceAt(at("f1")).type());
        assertEquals("O-O", game.moveNotation().get(6));
    }

    @Test
    void undoAndRedoAnEnPassantCapture() {
        IGameService game = TestGames.standard();
        play(game, "e2e4", "a7a6", "e4e5", "d7d5", "e5d6");

        game.undoLastMove();

        assertEquals(Color.WHITE, game.pieceAt(at("e5")).color());
        assertEquals(Color.BLACK, game.pieceAt(at("d5")).color());
        assertNull(game.pieceAt(at("d6")));
        assertEquals(List.of(), game.capturedPieces());

        game.redoLastMove();

        assertEquals(Color.WHITE, game.pieceAt(at("d6")).color());
        assertNull(game.pieceAt(at("d5")));
        assertEquals(1, game.capturedPieces().size());
        assertEquals("e5d6", game.moveCodes().get(4));
        assertEquals("e5d6e", game.moveNotation().get(4));
    }

    @Test
    void undoAndRedoAPromotion() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                "P.......",
                EMPTY, EMPTY, EMPTY, EMPTY,
                ".......P",
                ".......K");
        play(game, "a7a8q");

        game.undoLastMove();

        assertEquals(PieceType.PAWN, game.pieceAt(at("a7")).type());
        assertNull(game.pieceAt(at("a8")));
        assertEquals(List.of(), game.moveCodes());
        assertEquals(List.of(), game.moveNotation());

        game.redoLastMove();

        assertEquals(PieceType.QUEEN, game.pieceAt(at("a8")).type());
        assertEquals(List.of("a7a8q"), game.moveCodes());
        assertEquals(List.of("a7a8q"), game.moveNotation());
    }

    @Test
    void undoWithoutMovesThrowsIllegalStateException() {
        IGameService game = TestGames.standard();

        assertThrows(IllegalStateException.class, game::undoLastMove);
    }

    @Test
    void redoWithNothingToRedoThrowsIllegalStateException() {
        IGameService game = TestGames.standard();
        play(game, "e2e4");

        assertThrows(IllegalStateException.class, game::redoLastMove);
    }

    @Test
    void moveCodesAndNotationFollowUndoAndRedo() {
        IGameService game = TestGames.standard();
        play(game, "e2e4", "e7e5", "g1f3");

        game.undoLastMove();

        assertEquals(List.of("e2e4", "e7e5"), game.moveCodes());
        assertEquals(List.of("e2e4", "e7e5"), game.moveNotation());

        game.redoLastMove();

        assertEquals(List.of("e2e4", "e7e5", "g1f3"), game.moveCodes());
        assertEquals(List.of("e2e4", "e7e5", "g1f3"), game.moveNotation());
    }

    @Test
    void aNewMoveAfterUndoDiscardsTheMovesToRedo() {
        IGameService game = TestGames.standard();
        play(game, "e2e4");
        game.undoLastMove();

        play(game, "d2d4");

        assertThrows(IllegalStateException.class, game::redoLastMove);
        assertEquals(List.of("d2d4"), game.moveCodes());
    }

    @Test
    void undoingTheMoveThatEndedTheGameReopensIt() {
        IGameService game = TestGames.standard();
        play(game, "f2f3", "e7e5", "g2g4", "d8h4");
        assertTrue(game.isFinished());

        game.undoLastMove();

        assertFalse(game.isFinished());
        assertEquals(GameResult.IN_PROGRESS, game.result());
        assertEquals(Color.BLACK, game.turn());
        assertTrue(game.legalMoves(at("d8")).contains(at("h4")));
    }

    @Test
    void redoingTheMoveThatEndedTheGameFinishesItAgain() {
        IGameService game = TestGames.standard();
        play(game, "f2f3", "e7e5", "g2g4", "d8h4");
        game.undoLastMove();

        game.redoLastMove();

        assertTrue(game.isFinished());
        assertEquals(GameResult.BLACK_WINS, game.result());
    }
}
