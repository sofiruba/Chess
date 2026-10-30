package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.play;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.port.IGameService;
import com.chess.domain.rule.IMoveCodeParser;
import com.chess.domain.rule.MoveCodeParser;
import com.chess.domain.rule.MoveRequest;
import com.chess.domain.model.Piece;
import com.chess.domain.model.PieceType;

final class MoveCodeTest {

    private static final String EMPTY = "........";
    private static final List<PieceType> PROMOTION_TYPES =
            List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT);

    private final IMoveCodeParser parser = new MoveCodeParser(PROMOTION_TYPES);

    @Test
    void parserReadsAPlainMove() {
        MoveRequest request = parser.parse("e2e4");

        assertEquals(at("e2"), request.from());
        assertEquals(at("e4"), request.to());
        assertNull(request.promotionType());
    }

    @Test
    void parserReadsThePromotionLetter() {
        MoveRequest request = parser.parse("e7e8n");

        assertEquals(at("e7"), request.from());
        assertEquals(at("e8"), request.to());
        assertEquals(PieceType.KNIGHT, request.promotionType());
    }

    @Test
    void parserRejectsTheDisplayNotationOfCastling() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("O-O"));
    }

    @Test
    void parserRejectsTheDisplayNotationOfEnPassant() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("e5d6e"));
    }

    @Test
    void parserRejectsAnUnknownPromotionLetter() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("e7e8x"));
    }

    @Test
    void parserRejectsMalformedText() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(""));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("e2"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("E2E4"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("e0e4"));
    }

    @Test
    void castlingIsStoredAsTheKingMoveAndReplaysAsCastling() {
        IGameService original = castlingGame();
        play(original, "e1g1");

        IGameService replayed = castlingGame();
        replayed.playMoveCode(original.moveCodes().get(0));

        assertEquals(List.of("e1g1"), original.moveCodes());
        assertEquals(List.of("O-O"), original.moveNotation());
        assertEquals(PieceType.ROOK, replayed.pieceAt(at("f1")).type());
        assertEquals(PieceType.KING, replayed.pieceAt(at("g1")).type());
    }

    @Test
    void enPassantIsStoredAsThePawnMoveAndReplaysAsACapture() {
        IGameService original = enPassantGame();
        play(original, "d7d5", "e5d6");

        IGameService replayed = enPassantGame();
        original.moveCodes().forEach(replayed::playMoveCode);

        assertEquals(List.of("d7d5", "e5d6"), original.moveCodes());
        assertNotEquals(original.moveCodes().get(1), original.moveNotation().get(1));
        assertNull(replayed.pieceAt(at("d5")));
        assertEquals(Color.WHITE, replayed.pieceAt(at("d6")).color());
    }

    @Test
    void promotionIsStoredWithItsLetterAndReplaysWithTheSamePiece() {
        IGameService original = promotionGame();
        play(original, "a7a8n");

        IGameService replayed = promotionGame();
        replayed.playMoveCode(original.moveCodes().get(0));

        Piece promoted = replayed.pieceAt(at("a8"));
        assertEquals(List.of("a7a8n"), original.moveCodes());
        assertEquals(PieceType.KNIGHT, promoted.type());
    }

    @Test
    void aPromotionWithoutLetterIsRejected() {
        IGameService game = promotionGame();

        assertThrows(IllegalArgumentException.class, () -> game.playMoveCode("a7a8"));
    }

    @Test
    void aLetterOnAMoveThatDoesNotPromoteIsRejected() {
        IGameService game = TestGames.standard();

        assertThrows(IllegalArgumentException.class, () -> game.playMoveCode("e2e4q"));
    }

    @Test
    void anIllegalMoveCodeIsRejectedAndLeavesTheGameUntouched() {
        IGameService game = TestGames.standard();

        assertThrows(IllegalArgumentException.class, () -> game.playMoveCode("e2e5"));

        assertEquals(List.of(), game.moveCodes());
    }

    @Test
    void aWholeGameReplaysToTheSameMoveCodesAndCanBeUndone() {
        IGameService original = TestGames.standard();
        play(original, "e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "g8f6");

        IGameService replayed = TestGames.standard();
        original.moveCodes().forEach(replayed::playMoveCode);
        replayed.undoLastMove();
        replayed.redoLastMove();

        assertEquals(original.moveCodes(), replayed.moveCodes());
        assertEquals(original.turn(), replayed.turn());
    }

    private IGameService castlingGame() {
        return TestGames.fromDiagram(Color.WHITE,
                "....k...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "R...K..R");
    }

    private IGameService enPassantGame() {
        return TestGames.fromDiagram(Color.BLACK,
                "....k...",
                "...p....",
                EMPTY,
                "....P...",
                EMPTY, EMPTY, EMPTY,
                "....K...");
    }

    private IGameService promotionGame() {
        return TestGames.fromDiagram(Color.WHITE,
                "....k...",
                "P.......",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "....K...");
    }
}