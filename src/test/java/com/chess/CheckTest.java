package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.squares;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.model.PieceType;
import com.chess.domain.port.IGameService;

final class CheckTest {

    private static final String EMPTY = "........";

    @Test
    void rookGivesCheck() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k...r...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "....K...");

        assertOnlyWhiteIsInCheck(game);
    }

    @Test
    void bishopGivesCheck() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY, EMPTY,
                "b.......",
                EMPTY, EMPTY,
                "P.......",
                "....K...");

        assertOnlyWhiteIsInCheck(game);
    }

    @Test
    void knightGivesCheck() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY, EMPTY, EMPTY, EMPTY,
                ".....n..",
                "P.......",
                "....K...");

        assertOnlyWhiteIsInCheck(game);
    }

    @Test
    void pawnGivesCheck() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "...p....",
                "....K...");

        assertOnlyWhiteIsInCheck(game);
    }

    @Test
    void queenGivesCheck() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY, EMPTY, EMPTY,
                ".......q",
                EMPTY, EMPTY,
                "....K...");

        assertOnlyWhiteIsInCheck(game);
    }

    @Test
    void kingInCheckPositionIsTheSquareOfTheKingInCheck() {
        IGameService game = whiteKingChecked();

        assertEquals(at("e1"), game.kingInCheckPosition());
    }

    @Test
    void kingInCheckPositionIsTheKingOfTheSideToMove() {
        IGameService game = TestGames.fromDiagram(Color.BLACK,
                "k...R...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                ".......K");

        assertEquals(at("a8"), game.kingInCheckPosition());
    }

    @Test
    void kingInCheckPositionIsNullWithoutCheck() {
        IGameService game = TestGames.standard();

        assertNull(game.kingInCheckPosition());
    }

    @Test
    void kingInCheckMayEscapeFromTheAttackedLine() {
        IGameService game = whiteKingCheckedWithBlockerAndCapturer();

        assertEquals(squares("d1", "d2", "f1", "f2"), Set.copyOf(game.legalMoves(at("e1"))));
    }

    @Test
    void kingInCheckMayCaptureTheAttacker() {
        IGameService game = whiteKingCheckedWithBlockerAndCapturer();

        assertEquals(squares("e8"), Set.copyOf(game.legalMoves(at("a4"))));
    }

    @Test
    void kingInCheckMayBlockTheAttack() {
        IGameService game = whiteKingCheckedWithBlockerAndCapturer();

        assertEquals(squares("e4"), Set.copyOf(game.legalMoves(at("h4"))));
    }

    @Test
    void aMoveThatDoesNotResolveTheCheckIsRejected() {
        IGameService game = whiteKingCheckedWithBlockerAndCapturer();

        assertThrows(IllegalArgumentException.class, () -> game.playMove(at("h4"), at("h5")));

        assertEquals(PieceType.ROOK, game.pieceAt(at("h4")).type());
        assertEquals(Color.WHITE, game.turn());
    }

    @Test
    void kingMovingAlongTheLineOfTheAttackIsRejected() {
        IGameService game = whiteKingCheckedWithBlockerAndCapturer();

        assertThrows(IllegalArgumentException.class, () -> game.playMove(at("e1"), at("e2")));

        assertEquals(PieceType.KING, game.pieceAt(at("e1")).type());
        assertEquals(Color.WHITE, game.turn());
    }

    @Test
    void pinnedPieceMayMoveAlongTheLineOfThePin() {
        IGameService game = pinnedBishopOnC3();

        assertEquals(squares("b4", "a5", "d2"), Set.copyOf(game.legalMoves(at("c3"))));
    }

    @Test
    void pinnedPieceCannotLeaveTheLineOfThePin() {
        IGameService game = pinnedBishopOnC3();

        assertThrows(IllegalArgumentException.class, () -> game.playMove(at("c3"), at("d4")));

        assertEquals(PieceType.BISHOP, game.pieceAt(at("c3")).type());
        assertFalse(game.legalMoves(at("c3")).contains(at("b2")));
    }

    private static void assertOnlyWhiteIsInCheck(IGameService game) {
        assertTrue(game.isInCheck(Color.WHITE));
        assertFalse(game.isInCheck(Color.BLACK));
    }

    private static IGameService whiteKingChecked() {
        return TestGames.fromDiagram(Color.WHITE,
                "k...r...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "....K...");
    }

    /** Black rook on e8 checks the king on e1; the bishop on a4 can capture it and the rook on h4 can block. */
    private static IGameService whiteKingCheckedWithBlockerAndCapturer() {
        return TestGames.fromDiagram(Color.WHITE,
                "k...r...",
                EMPTY, EMPTY, EMPTY,
                "B......R",
                EMPTY, EMPTY,
                "....K...");
    }

    /** The white bishop on c3 shields the king on e1 from the black bishop on a5. */
    private static IGameService pinnedBishopOnC3() {
        return TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY, EMPTY,
                "b.......",
                EMPTY,
                "..B.....",
                ".......P",
                "....K...");
    }
}
