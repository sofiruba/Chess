package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.play;
import static com.chess.TestGames.squares;
import static com.chess.TestGames.withBoxes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.model.PieceType;
import com.chess.domain.port.IGameService;

/** Lo que hace cada poder y sus límites. */
final class PowersTest {

    @Nested
    final class IceFlower {

        private IGameService game() {
            return withBoxes("c5=i", Color.WHITE,
                    "....k...",
                    ".......r",
                    "........",
                    "........",
                    "........",
                    ".N......",
                    "........",
                    "....K...");
        }

        @Test
        void itTargetsAnyEnemyPieceButTheKing() {
            IGameService game = game();
            play(game, "b3c5", "e8d8");

            assertEquals(squares("h7"), Set.copyOf(game.powerTargets(at("c5"))));
        }

        @Test
        void theFrozenPieceCannotMoveDuringItsOwnersNextTurn() {
            IGameService game = game();
            play(game, "b3c5", "e8d8");

            game.usePower(at("c5"), at("h7"));

            assertTrue(game.isFrozen(at("h7")));
            assertTrue(game.legalMoves(at("h7")).isEmpty());
            assertFalse(game.legalMoves(at("d8")).isEmpty());
        }

        @Test
        void itThawsWhenThatTurnIsOver() {
            IGameService game = game();
            play(game, "b3c5", "e8d8");
            game.usePower(at("c5"), at("h7"));

            play(game, "d8e8", "c5b3");

            assertFalse(game.isFrozen(at("h7")));
            assertFalse(game.legalMoves(at("h7")).isEmpty());
        }

        @Test
        void aFrozenPieceCanStillBeCaptured() {
            IGameService game = withBoxes("b2=i", Color.WHITE,
                    "....k...",
                    ".......r",
                    "........",
                    "........",
                    "........",
                    "........",
                    ".B......",
                    "....K..R");
            game.usePower(at("b2"), at("h7"));
            play(game, "e8d8");

            assertTrue(game.legalMoves(at("h1")).contains(at("h7")));
        }
    }

    @Nested
    final class Feather {

        @Test
        void itFliesToAnyEmptySquare() {
            IGameService game = withBoxes("d4=f", Color.WHITE,
                    "....k...",
                    ".......p",
                    "........",
                    "........",
                    "...N....",
                    "........",
                    "........",
                    "....K...");

            game.usePower(at("d4"), at("h5"));

            assertEquals(PieceType.KNIGHT, game.pieceAt(at("h5")).type());
            assertNull(game.pieceAt(at("d4")));
            assertEquals(List.of("Pluma d4-h5"), game.moveNotation());
        }

        @Test
        void itCannotLandOnAnOccupiedSquare() {
            IGameService game = withBoxes("d4=f", Color.WHITE,
                    "....k...",
                    ".......p",
                    "........",
                    "........",
                    "...N....",
                    "........",
                    "........",
                    "....K...");

            assertFalse(game.powerTargets(at("d4")).contains(at("h7")));
            assertFalse(game.powerTargets(at("d4")).contains(at("e1")));
        }

        @Test
        void aPawnCannotFlyToTheFirstOrLastRank() {
            IGameService game = withBoxes("a5=f", Color.WHITE,
                    "....k...",
                    "........",
                    "........",
                    "P.......",
                    "........",
                    "........",
                    "........",
                    "....K...");

            assertTrue(game.powerTargets(at("a5")).stream().noneMatch(target -> target.y() == 0 || target.y() == 7));
            assertTrue(game.powerTargets(at("a5")).contains(at("h6")));
        }

        @Test
        void aPinnedPieceCannotFlyAwayFromThePin() {
            IGameService game = withBoxes("e2=f", Color.WHITE,
                    "k...r...",
                    "........",
                    "........",
                    "........",
                    "........",
                    "........",
                    "....B...",
                    "....K...");

            assertEquals(squares("e3", "e4", "e5", "e6", "e7"), Set.copyOf(game.powerTargets(at("e2"))));
        }

        @Test
        void theKingCannotFlyIntoCheck() {
            IGameService game = withBoxes("e1=f", Color.WHITE,
                    "k......r",
                    "........",
                    "........",
                    "........",
                    "........",
                    "........",
                    "........",
                    "....K...");

            assertFalse(game.powerTargets(at("e1")).contains(at("h4")));
            assertFalse(game.powerTargets(at("e1")).contains(at("c8")));
            assertTrue(game.powerTargets(at("e1")).contains(at("d4")));
        }

        @Test
        void aKingThatFliesLosesCastling() {
            IGameService game = withBoxes("e1=f", Color.WHITE,
                    "....k...",
                    "........",
                    "........",
                    "........",
                    "........",
                    "........",
                    "........",
                    "R...K..R");
            game.usePower(at("e1"), at("e2"));
            play(game, "e8d8", "e2e1", "d8e8");

            assertFalse(game.legalMoves(at("e1")).contains(at("g1")));
        }
    }

    @Nested
    final class Star {

        private IGameService knightUnderAttack() {
            return withBoxes("d4=s", Color.WHITE,
                    "...rk...",
                    "........",
                    "........",
                    "........",
                    "...N....",
                    "........",
                    "........",
                    "K.......");
        }

        @Test
        void itMakesThePieceImmuneDuringTheRivalsTurn() {
            IGameService game = knightUnderAttack();

            game.usePower(at("d4"), at("d4"));

            assertTrue(game.isImmune(at("d4")));
            assertFalse(game.legalMoves(at("d8")).contains(at("d4")));
        }

        @Test
        void theImmunityEndsWhenTheRivalsTurnIsOver() {
            IGameService game = knightUnderAttack();
            game.usePower(at("d4"), at("d4"));

            play(game, "e8f8");

            assertFalse(game.isImmune(at("d4")));
            play(game, "a1b1");
            assertTrue(game.legalMoves(at("d8")).contains(at("d4")));
        }

        @Test
        void anImmunePieceCannotBeFrozenNorHitByAShell() {
            IGameService game = withBoxes("d4=s,d8=t,h8=i", Color.WHITE,
                    "...rk..b",
                    "........",
                    "........",
                    "........",
                    "...N...P",
                    "........",
                    "........",
                    "K.......");
            game.usePower(at("d4"), at("d4"));

            assertTrue(game.powerTargets(at("d8")).isEmpty());
            assertFalse(game.powerTargets(at("h8")).contains(at("d4")));
        }

        @Test
        void theKingsStarRemovesTheEnemiesAroundIt() {
            IGameService game = withBoxes("d4=s", Color.WHITE,
                    ".......k",
                    "........",
                    "........",
                    "........",
                    "...K....",
                    "..n.r...",
                    "...P....",
                    "........");

            game.usePower(at("d4"), at("d4"));

            assertNull(game.pieceAt(at("c3")));
            assertNull(game.pieceAt(at("e3")));
            assertNotNull(game.pieceAt(at("d2")));
            assertEquals(2, game.capturedPieces().size());
            assertFalse(game.isImmune(at("d4")));
            assertEquals(List.of("Estrella d4 (x2)"), game.moveNotation());
        }

        @Test
        void theBlastIsUndoneCompletely() {
            IGameService game = withBoxes("d4=s", Color.WHITE,
                    ".......k",
                    "........",
                    "........",
                    "........",
                    "...K....",
                    "..n.r...",
                    "...P....",
                    "........");
            game.usePower(at("d4"), at("d4"));

            game.undoLastMove();

            assertEquals(PieceType.KNIGHT, game.pieceAt(at("c3")).type());
            assertEquals(PieceType.ROOK, game.pieceAt(at("e3")).type());
            assertTrue(game.capturedPieces().isEmpty());
        }
    }

    @Nested
    final class Shell {

        @Test
        void itHitsTheFirstEnemyInEachDirection() {
            IGameService game = withBoxes("d4=t", Color.WHITE,
                    "...pk...",
                    "........",
                    "...p....",
                    "........",
                    "n..R..b.",
                    "........",
                    "........",
                    "K.......");

            assertEquals(squares("d6", "a4", "g4"), Set.copyOf(game.powerTargets(at("d4"))));
        }

        @Test
        void itCannotGoThroughATeammate() {
            IGameService game = withBoxes("d4=t", Color.WHITE,
                    "....k...",
                    "...p....",
                    "........",
                    "...P....",
                    "...R....",
                    "........",
                    "........",
                    "K.......");

            assertTrue(game.powerTargets(at("d4")).isEmpty());
        }

        @Test
        void theKingIsTooBigForTheShell() {
            IGameService game = withBoxes("e4=t", Color.WHITE,
                    "....k...",
                    "........",
                    "........",
                    "........",
                    "....R...",
                    "........",
                    "........",
                    "K.......");

            assertTrue(game.powerTargets(at("e4")).isEmpty());
        }

        @Test
        void theThrowerStaysInPlace() {
            IGameService game = withBoxes("d4=t", Color.WHITE,
                    "....k...",
                    "........",
                    "...p....",
                    "........",
                    "...R....",
                    "........",
                    "........",
                    "K.......");

            game.usePower(at("d4"), at("d6"));

            assertEquals(PieceType.ROOK, game.pieceAt(at("d4")).type());
            assertNull(game.pieceAt(at("d6")));
            assertEquals(List.of("@d4d6"), game.moveCodes());
        }
    }
}
