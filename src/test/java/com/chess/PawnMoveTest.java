package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.squares;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.port.IGameService;

final class PawnMoveTest {

    private static final String EMPTY = "........";

    @Test
    void whitePawnOutsideTheInitialRankAdvancesOnlyOneSquare() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY, EMPTY, EMPTY,
                "....P...",
                EMPTY,
                "K.......");

        assertEquals(squares("e4"), Set.copyOf(game.legalMoves(at("e3"))));
    }

    @Test
    void whitePawnMayAdvanceOneOrTwoSquaresFromTheInitialRank() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "....P...",
                "K.......");

        assertEquals(squares("e3", "e4"), Set.copyOf(game.legalMoves(at("e2"))));
    }

    @Test
    void blackPawnMayAdvanceOneOrTwoSquaresFromTheInitialRank() {
        IGameService game = TestGames.fromDiagram(Color.BLACK,
                ".......k",
                "....p...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "K.......");

        assertEquals(squares("e6", "e5"), Set.copyOf(game.legalMoves(at("e7"))));
    }

    @Test
    void blackPawnAdvancesDownTheBoard() {
        IGameService game = TestGames.fromDiagram(Color.BLACK,
                ".......k",
                EMPTY, EMPTY,
                "....p...",
                EMPTY, EMPTY, EMPTY,
                "K.......");

        assertEquals(squares("e4"), Set.copyOf(game.legalMoves(at("e5"))));
    }

    @Test
    void pawnBlockedByAnEnemyPieceInFrontCannotAdvanceOneOrTwoSquares() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY, EMPTY, EMPTY,
                "....n...",
                "....P...",
                "K.......");

        assertEquals(squares(), Set.copyOf(game.legalMoves(at("e2"))));
    }

    @Test
    void pawnBlockedByAnOwnPieceInFrontCannotAdvanceOneOrTwoSquares() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY, EMPTY, EMPTY,
                "....N...",
                "....P...",
                "K.......");

        assertEquals(squares(), Set.copyOf(game.legalMoves(at("e2"))));
    }

    @Test
    void doubleStepIsBlockedWhenOnlyTheTargetSquareIsOccupied() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY, EMPTY,
                "....n...",
                EMPTY,
                "....P...",
                "K.......");

        assertEquals(squares("e3"), Set.copyOf(game.legalMoves(at("e2"))));
    }

    @Test
    void pawnCapturesEnemyPiecesDiagonally() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY,
                "...n.n..",
                "....P...",
                EMPTY, EMPTY,
                "K.......");

        assertEquals(squares("e5", "d5", "f5"), Set.copyOf(game.legalMoves(at("e4"))));
    }

    @Test
    void blackPawnCapturesEnemyPiecesDiagonallyTowardsLowerRanks() {
        IGameService game = TestGames.fromDiagram(Color.BLACK,
                ".......k",
                EMPTY, EMPTY,
                "....p...",
                "...N.N..",
                EMPTY, EMPTY,
                "K.......");

        assertEquals(squares("e4", "d4", "f4"), Set.copyOf(game.legalMoves(at("e5"))));
    }

    @Test
    void pawnCannotCaptureOwnPieces() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY,
                "...N.N..",
                "....P...",
                EMPTY, EMPTY,
                "K.......");

        assertEquals(squares("e5"), Set.copyOf(game.legalMoves(at("e4"))));
    }

    @Test
    void pawnCannotCaptureStraightAhead() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY,
                "....n...",
                "....P...",
                EMPTY, EMPTY,
                "K.......");

        assertEquals(squares(), Set.copyOf(game.legalMoves(at("e4"))));
    }

    @Test
    void pawnOnTheAFileHasOnlyOneCaptureDiagonal() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                ".......k",
                EMPTY, EMPTY,
                ".n......",
                "P.......",
                EMPTY, EMPTY,
                "K.......");

        assertEquals(squares("a5", "b5"), Set.copyOf(game.legalMoves(at("a4"))));
    }

    @Test
    void pawnOnTheHFileHasOnlyOneCaptureDiagonal() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY, EMPTY,
                "......n.",
                ".......P",
                EMPTY, EMPTY,
                "K.......");

        assertEquals(squares("h5", "g5"), Set.copyOf(game.legalMoves(at("h4"))));
    }
}
