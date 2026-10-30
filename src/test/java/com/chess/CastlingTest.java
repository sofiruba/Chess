package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.play;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.model.PieceType;
import com.chess.domain.port.IGameService;

final class CastlingTest {

    private static final String EMPTY = "........";

    @Test
    void whiteCastlesShort() {
        IGameService game = whiteWithBothRooks();

        play(game, "e1g1");

        assertPiece(game, "g1", Color.WHITE, PieceType.KING);
        assertPiece(game, "f1", Color.WHITE, PieceType.ROOK);
        assertNull(game.pieceAt(at("e1")));
        assertNull(game.pieceAt(at("h1")));
    }

    @Test
    void whiteCastlesLong() {
        IGameService game = whiteWithBothRooks();

        play(game, "e1c1");

        assertPiece(game, "c1", Color.WHITE, PieceType.KING);
        assertPiece(game, "d1", Color.WHITE, PieceType.ROOK);
        assertNull(game.pieceAt(at("e1")));
        assertNull(game.pieceAt(at("a1")));
        assertNull(game.pieceAt(at("b1")));
    }

    @Test
    void blackCastlesShort() {
        IGameService game = blackWithBothRooks();

        play(game, "e8g8");

        assertPiece(game, "g8", Color.BLACK, PieceType.KING);
        assertPiece(game, "f8", Color.BLACK, PieceType.ROOK);
        assertNull(game.pieceAt(at("e8")));
        assertNull(game.pieceAt(at("h8")));
    }

    @Test
    void blackCastlesLong() {
        IGameService game = blackWithBothRooks();

        play(game, "e8c8");

        assertPiece(game, "c8", Color.BLACK, PieceType.KING);
        assertPiece(game, "d8", Color.BLACK, PieceType.ROOK);
        assertNull(game.pieceAt(at("e8")));
        assertNull(game.pieceAt(at("a8")));
        assertNull(game.pieceAt(at("b8")));
    }

    @Test
    void castlingIsRejectedWhileTheKingIsInCheck() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY, EMPTY,
                "....r...",
                EMPTY, EMPTY, EMPTY,
                "R...K..R");

        assertFalse(game.legalMoves(at("e1")).contains(at("g1")));
        assertFalse(game.legalMoves(at("e1")).contains(at("c1")));
    }

    @Test
    void castlingIsRejectedWhenTheKingPassesThroughAnAttackedSquare() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k....r..",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "R...K..R");

        assertFalse(game.legalMoves(at("e1")).contains(at("g1")));
    }

    @Test
    void castlingIsRejectedWhenTheDestinationSquareIsAttacked() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.....r.",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "R...K..R");

        assertFalse(game.legalMoves(at("e1")).contains(at("g1")));
    }

    @Test
    void shortCastlingIsRejectedWithAPieceBetweenKingAndRook() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "....k...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "R...K.NR");

        assertFalse(game.legalMoves(at("e1")).contains(at("g1")));
    }

    @Test
    void longCastlingIsRejectedWithAPieceBetweenKingAndRook() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "....k...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "R..NK..R");

        assertFalse(game.legalMoves(at("e1")).contains(at("c1")));
    }

    @Test
    void longCastlingIsRejectedWithAPieceOnB1() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "....k...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "RN..K..R");

        assertFalse(game.legalMoves(at("e1")).contains(at("c1")));
    }

    @Test
    void castlingIsRejectedAfterTheKingHasMoved() {
        IGameService game = whiteWithBothRooks();
        play(game, "e1e2", "e8e7", "e2e1", "e7e8");

        assertFalse(game.legalMoves(at("e1")).contains(at("g1")));
        assertFalse(game.legalMoves(at("e1")).contains(at("c1")));
    }

    @Test
    void shortCastlingIsRejectedAfterTheKingSideRookHasMoved() {
        IGameService game = whiteWithBothRooks();
        play(game, "h1h2", "e8e7", "h2h1", "e7e8");

        assertFalse(game.legalMoves(at("e1")).contains(at("g1")));
        assertTrue(game.legalMoves(at("e1")).contains(at("c1")));
    }

    @Test
    void longCastlingIsRejectedAfterTheQueenSideRookHasMoved() {
        IGameService game = whiteWithBothRooks();
        play(game, "a1a2", "e8e7", "a2a1", "e7e8");

        assertFalse(game.legalMoves(at("e1")).contains(at("c1")));
        assertTrue(game.legalMoves(at("e1")).contains(at("g1")));
    }

    @Test
    void longCastlingIsAllowedWhenOnlyB1IsAttacked() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "kr......",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "R...K..R");

        assertTrue(game.legalMoves(at("e1")).contains(at("c1")));
        play(game, "e1c1");
        assertPiece(game, "c1", Color.WHITE, PieceType.KING);
        assertPiece(game, "d1", Color.WHITE, PieceType.ROOK);
    }

    @Test
    void castlingIsRecordedAsTheKingMoveCode() {
        IGameService game = whiteWithBothRooks();

        play(game, "e1g1");

        assertEquals(List.of("e1g1"), game.moveCodes());
    }

    @Test
    void shortCastlingNotationIsOO() {
        IGameService game = whiteWithBothRooks();

        play(game, "e1g1");

        assertEquals(List.of("O-O"), game.moveNotation());
    }

    @Test
    void longCastlingNotationIsOOO() {
        IGameService game = whiteWithBothRooks();

        play(game, "e1c1");

        assertEquals(List.of("O-O-O"), game.moveNotation());
    }

    @Test
    void undoRestoresThePiecesAndTheCastlingRights() {
        IGameService game = whiteWithBothRooks();
        play(game, "e1g1");

        game.undoLastMove();

        assertPiece(game, "e1", Color.WHITE, PieceType.KING);
        assertPiece(game, "h1", Color.WHITE, PieceType.ROOK);
        assertNull(game.pieceAt(at("f1")));
        assertNull(game.pieceAt(at("g1")));
        assertTrue(game.legalMoves(at("e1")).contains(at("g1")));
        assertTrue(game.legalMoves(at("e1")).contains(at("c1")));
    }

    @Test
    void redoCastlesAgain() {
        IGameService game = whiteWithBothRooks();
        play(game, "e1g1");
        game.undoLastMove();

        game.redoLastMove();

        assertPiece(game, "g1", Color.WHITE, PieceType.KING);
        assertPiece(game, "f1", Color.WHITE, PieceType.ROOK);
        assertNull(game.pieceAt(at("e1")));
        assertNull(game.pieceAt(at("h1")));
        assertEquals(List.of("O-O"), game.moveNotation());
    }

    private static void assertPiece(IGameService game, String square, Color color, PieceType type) {
        assertEquals(color, game.pieceAt(at(square)).color(), square);
        assertEquals(type, game.pieceAt(at(square)).type(), square);
    }

    private static IGameService whiteWithBothRooks() {
        return TestGames.fromDiagram(Color.WHITE,
                "....k...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "R...K..R");
    }

    private static IGameService blackWithBothRooks() {
        return TestGames.fromDiagram(Color.BLACK,
                "r...k..r",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "....K...");
    }
}
