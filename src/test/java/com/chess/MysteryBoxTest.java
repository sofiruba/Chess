package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.play;
import static com.chess.TestGames.squares;
import static com.chess.TestGames.withBoxes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.chess.configuration.DefaultMoveCommandFactory;
import com.chess.configuration.GameSetup;
import com.chess.configuration.StandardGameFactory;
import com.chess.domain.model.Color;
import com.chess.domain.model.GameResult;
import com.chess.domain.model.PowerType;
import com.chess.domain.port.IGameListener;
import com.chess.domain.port.IGameService;

/** Las cajas misteriosas: dónde aparecen, cómo se abren y qué pasa con el poder de cada pieza. */
final class MysteryBoxTest {

    @Test
    void aNewGameHasFourBoxesInTheMiddle() {
        IGameService game = new StandardGameFactory(
                new DefaultMoveCommandFactory(), GameSetup.mysteryBoxEdition()).create();

        assertEquals(List.of(at("a5"), at("c5"), at("f4"), at("h4")), game.mysteryBoxes());
        assertEquals(4, game.setupCode().split(",").length);
    }

    @Test
    void theClassicSetupHasNoBoxes() {
        IGameService game = TestGames.standard();

        assertTrue(game.mysteryBoxes().isEmpty());
        assertEquals("", game.setupCode());
    }

    @Test
    void theSameSeedDealsTheSameBoxes() {
        String first = seededGame(7).setupCode();
        String second = seededGame(7).setupCode();

        assertEquals(first, second);
        assertTrue(first.startsWith("a5="));
    }

    @Test
    void aPieceThatLandsOnABoxGetsItsPowerAndTheBoxDisappears() {
        IGameService game = withBoxes("c5=i", Color.WHITE,
                "....k...",
                ".......p",
                "........",
                "........",
                "........",
                ".N......",
                "........",
                "....K...");

        play(game, "b3c5");

        assertEquals(PowerType.ICE_FLOWER, game.powerAt(at("c5")));
        assertTrue(game.mysteryBoxes().isEmpty());
        assertEquals(1, game.heldPowers(Color.WHITE).size());
        assertTrue(game.heldPowers(Color.BLACK).isEmpty());
    }

    @Test
    void aPieceThatAlreadyHasAPowerLeavesTheBoxWhereItIs() {
        IGameService game = withBoxes("b3=f,c5=i", Color.WHITE,
                "....k...",
                ".......p",
                "........",
                "........",
                "........",
                ".N......",
                "........",
                "....K...");

        play(game, "b3c5");

        assertEquals(PowerType.FEATHER, game.powerAt(at("c5")));
        assertEquals(List.of(at("c5")), game.mysteryBoxes());
    }

    @Test
    void usingAPowerEndsTheTurnAndSpendsThePower() {
        IGameService game = shellOnTheRook();

        game.usePower(at("a1"), at("a6"));

        assertEquals(Color.BLACK, game.turn());
        assertNull(game.powerAt(at("a1")));
        assertTrue(game.heldPowers(Color.WHITE).isEmpty());
    }

    @Test
    void aCapturedPieceLosesItsPower() {
        IGameService game = withBoxes("d4=s", Color.BLACK,
                "...rk...",
                "........",
                "........",
                "........",
                "...N....",
                "........",
                "........",
                "....K...");

        play(game, "d8d4");

        assertTrue(game.heldPowers(Color.WHITE).isEmpty());
    }

    @Test
    void aPowerCannotBeUsedOnAnInvalidTarget() {
        IGameService game = shellOnTheRook();

        assertThrows(IllegalArgumentException.class, () -> game.usePower(at("a1"), at("e8")));
        assertEquals(Color.WHITE, game.turn());
    }

    @Test
    void aPieceWithoutPowerHasNoTargets() {
        IGameService game = shellOnTheRook();

        assertTrue(game.powerTargets(at("e1")).isEmpty());
    }

    @Test
    void usingAPowerNotifiesTheListeners() {
        IGameService game = shellOnTheRook();
        int[] calls = new int[1];
        game.addListener(new IGameListener() {
            @Override
            public void powerUsed() {
                calls[0]++;
            }
        });

        game.usePower(at("a1"), at("a6"));

        assertEquals(1, calls[0]);
    }

    @Test
    void aPowerUseCanBeUndoneAndRedone() {
        IGameService game = shellOnTheRook();
        game.usePower(at("a1"), at("a6"));

        game.undoLastMove();

        assertEquals(Color.WHITE, game.turn());
        assertEquals(PowerType.SHELL, game.powerAt(at("a1")));
        assertTrue(game.pieceAt(at("a6")) != null);

        game.redoLastMove();

        assertNull(game.pieceAt(at("a6")));
        assertNull(game.powerAt(at("a1")));
    }

    @Test
    void aSavedGameWithPowersIsReplayedExactly() {
        StandardGameFactory factory =
                new StandardGameFactory(new DefaultMoveCommandFactory(), GameSetup.mysteryBoxEdition());
        IGameService game = factory.createWithSetup("a5=i,c5=f,f4=s,h4=t");
        play(game, "h2h4", "a7a5");
        game.usePower(at("h4"), at("h7"));

        IGameService replayed = factory.createWithSetup(game.setupCode(), game.moveCodes());

        assertEquals(List.of("h2h4", "a7a5", "@h4h7"), game.moveCodes());
        assertNull(replayed.pieceAt(at("h7")));
        assertEquals(PowerType.ICE_FLOWER, replayed.powerAt(at("a5")));
        assertEquals(Color.BLACK, replayed.turn());
    }

    @Test
    void aPowerThatSavesTheKingPreventsCheckmate() {
        String[] backRankMate = {
                ".......k",
                "........",
                "........",
                "........",
                "N.......",
                "........",
                "......PP",
                "r......K"};

        IGameService classic = TestGames.fromDiagram(Color.WHITE, backRankMate);
        IGameService withShell = withBoxes("a4=t", Color.WHITE, backRankMate);

        assertEquals(GameResult.BLACK_WINS, classic.result());
        assertFalse(withShell.isFinished());
        assertEquals(Set.of(at("a1")), Set.copyOf(withShell.powerTargets(at("a4"))));
    }

    @Test
    void theRemovedPiecesCountAsCaptured() {
        IGameService game = shellOnTheRook();

        game.usePower(at("a1"), at("a6"));

        assertEquals(1, game.capturedPieces().size());
    }

    @Test
    void theShellTargetIsTheSquareOfThePieceItHits() {
        assertEquals(squares("a6"), Set.copyOf(shellOnTheRook().powerTargets(at("a1"))));
    }

    private static IGameService shellOnTheRook() {
        return withBoxes("a1=t", Color.WHITE,
                "....k...",
                "........",
                "p.......",
                "........",
                "........",
                "........",
                "........",
                "R...K...");
    }

    private static IGameService seededGame(long seed) {
        return new StandardGameFactory(
                new DefaultMoveCommandFactory(), GameSetup.mysteryBoxEdition(), new Random(seed)).create();
    }
}
