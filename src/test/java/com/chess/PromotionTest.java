package com.chess;

import static com.chess.TestGames.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.model.PieceType;
import com.chess.domain.port.IGameService;

final class PromotionTest {

    private static final String EMPTY = "........";

    @Test
    void whitePawnPromotesToAQueen() {
        assertWhitePromotes(PieceType.QUEEN);
    }

    @Test
    void whitePawnPromotesToARook() {
        assertWhitePromotes(PieceType.ROOK);
    }

    @Test
    void whitePawnPromotesToABishop() {
        assertWhitePromotes(PieceType.BISHOP);
    }

    @Test
    void whitePawnPromotesToAKnight() {
        assertWhitePromotes(PieceType.KNIGHT);
    }

    @Test
    void blackPawnPromotesToAQueen() {
        assertBlackPromotes(PieceType.QUEEN);
    }

    @Test
    void blackPawnPromotesToARook() {
        assertBlackPromotes(PieceType.ROOK);
    }

    @Test
    void blackPawnPromotesToABishop() {
        assertBlackPromotes(PieceType.BISHOP);
    }

    @Test
    void blackPawnPromotesToAKnight() {
        assertBlackPromotes(PieceType.KNIGHT);
    }

    @Test
    void promotionCanCaptureOnTheLastRank() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "r......k",
                ".P......",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                ".......K");
        assertTrue(game.legalMoves(at("b7")).contains(at("a8")));

        game.playMove(at("b7"), at("a8"), PieceType.QUEEN);

        assertEquals(PieceType.QUEEN, game.pieceAt(at("a8")).type());
        assertEquals(Color.WHITE, game.pieceAt(at("a8")).color());
        assertNull(game.pieceAt(at("b7")));
        assertEquals(1, game.capturedPieces().size());
        assertEquals(PieceType.ROOK, game.capturedPieces().get(0).type());
    }

    @Test
    void promotionCanGiveCheck() {
        IGameService game = whitePawnOnA7();

        game.playMove(at("a7"), at("a8"), PieceType.QUEEN);

        assertTrue(game.isInCheck(Color.BLACK));
    }

    @Test
    void promotionWithoutAChosenPieceUsesTheFirstOption() {
        IGameService game = whitePawnOnA7();

        game.playMove(at("a7"), at("a8"));

        assertEquals(game.promotionOptions().get(0), game.pieceAt(at("a8")).type());
    }

    @Test
    void aPieceOutsideThePromotionOptionsIsRejected() {
        IGameService game = whitePawnOnA7();

        assertThrows(IllegalArgumentException.class, () -> game.playMove(at("a7"), at("a8"), PieceType.KING));
        assertThrows(IllegalArgumentException.class, () -> game.playMove(at("a7"), at("a8"), PieceType.PAWN));

        assertEquals(PieceType.PAWN, game.pieceAt(at("a7")).type());
        assertNull(game.pieceAt(at("a8")));
        assertEquals(Color.WHITE, game.turn());
    }

    @Test
    void undoRestoresThePawn() {
        IGameService game = whitePawnOnA7();
        game.playMove(at("a7"), at("a8"), PieceType.QUEEN);

        game.undoLastMove();

        assertEquals(PieceType.PAWN, game.pieceAt(at("a7")).type());
        assertEquals(Color.WHITE, game.pieceAt(at("a7")).color());
        assertNull(game.pieceAt(at("a8")));
    }

    @Test
    void undoRestoresThePawnAndTheCapturedPiece() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "r......k",
                ".P......",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                ".......K");
        game.playMove(at("b7"), at("a8"), PieceType.QUEEN);

        game.undoLastMove();

        assertEquals(PieceType.PAWN, game.pieceAt(at("b7")).type());
        assertEquals(PieceType.ROOK, game.pieceAt(at("a8")).type());
        assertEquals(Color.BLACK, game.pieceAt(at("a8")).color());
        assertEquals(0, game.capturedPieces().size());
    }

    private static void assertWhitePromotes(PieceType type) {
        IGameService game = whitePawnOnA7();

        game.playMove(at("a7"), at("a8"), type);

        assertEquals(type, game.pieceAt(at("a8")).type());
        assertEquals(Color.WHITE, game.pieceAt(at("a8")).color());
        assertNull(game.pieceAt(at("a7")));
    }

    private static void assertBlackPromotes(PieceType type) {
        IGameService game = TestGames.fromDiagram(Color.BLACK,
                ".......k",
                ".......p",
                EMPTY, EMPTY, EMPTY, EMPTY,
                "p.......",
                ".......K");

        game.playMove(at("a2"), at("a1"), type);

        assertEquals(type, game.pieceAt(at("a1")).type());
        assertEquals(Color.BLACK, game.pieceAt(at("a1")).color());
        assertNull(game.pieceAt(at("a2")));
    }

    /** The extra white pawn on h2 keeps the game going even when the pawn promotes to a minor piece. */
    private static IGameService whitePawnOnA7() {
        return TestGames.fromDiagram(Color.WHITE,
                ".......k",
                "P.......",
                EMPTY, EMPTY, EMPTY, EMPTY,
                ".......P",
                ".......K");
    }
}
