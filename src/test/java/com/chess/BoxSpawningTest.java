package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.play;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.chess.configuration.BoxSpawning;
import com.chess.configuration.DefaultMoveCommandFactory;
import com.chess.configuration.GameSetup;
import com.chess.configuration.StandardGameFactory;
import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.Position;
import com.chess.domain.model.PowerType;
import com.chess.domain.port.IGameService;
import com.chess.domain.power.MirroredBoxSpawner;
import com.chess.domain.power.PowerState;

/** Las cajas que aparecen durante la partida: cuándo, dónde y que se puedan reproducir. */
final class BoxSpawningTest {

    /** Las cuatro cajas iniciales: el tablero ya tiene el máximo. */
    private static final String START = "a5=i,c5=s,f4=f,h4=t|s=42";
    /** Sin cajas iniciales, para ver una aparición completa. */
    private static final String EMPTY = "|s=42";
    private static final List<String> FIVE_TURNS = List.of("e2e3", "e7e6", "d2d3", "d7d6", "b1c3");

    private final StandardGameFactory factory =
            new StandardGameFactory(new DefaultMoveCommandFactory(), GameSetup.mysteryBoxEdition());

    @Test
    void noBoxesAppearBeforeTheFifthTurn() {
        IGameService game = factory.createWithSetup(EMPTY);

        play(game, "e2e3", "e7e6", "d2d3", "d7d6");

        assertTrue(game.mysteryBoxes().isEmpty());
        assertEquals(1, game.turnsUntilNewBoxes());
    }

    @Test
    void afterFiveTurnsTwoBoxesAppearOnEachSide() {
        IGameService game = factory.createWithSetup(EMPTY);

        playAll(game, FIVE_TURNS);

        List<Position> fresh = game.mysteryBoxes();
        assertEquals(4, fresh.size());
        assertEquals(2, fresh.stream().filter(box -> box.y() < 4).count());
        assertEquals(2, fresh.stream().filter(box -> box.y() >= 4).count());
        assertEquals(5, game.turnsUntilNewBoxes());
    }

    @Test
    void theNewBoxesAreMirroredInPosition() {
        IGameService game = factory.createWithSetup(EMPTY);

        playAll(game, FIVE_TURNS);

        Set<Position> fresh = new HashSet<>(game.mysteryBoxes());
        for (Position box : fresh) {
            assertTrue(fresh.contains(new Position(7 - box.x(), 7 - box.y())), "missing mirror of " + box);
        }
    }

    @Test
    void theNewBoxesOnlyAppearOnFreeSquares() {
        for (long seed = 0; seed < 40; seed++) {
            IGameService game = factory.createWithSetup("|s=" + seed);
            playAll(game, FIVE_TURNS);
            assertEquals(4, game.mysteryBoxes().size());
            for (Position box : game.mysteryBoxes()) {
                assertNull(game.pieceAt(box), "seed " + seed + " put a box under a piece at " + box);
            }
        }
    }

    @Test
    void noBoxesAppearWhileTheBoardHasFour() {
        IGameService game = factory.createWithSetup(START);

        playAll(game, FIVE_TURNS);

        assertEquals(List.of(at("a5"), at("c5"), at("f4"), at("h4")), game.mysteryBoxes());
    }

    @Test
    void openingTwoBoxesMakesRoomForANewMirroredPair() {
        IGameService game = factory.createWithSetup(START);

        play(game, "h2h4", "a7a5", "g2g3", "b7b6", "g1f3");

        List<Position> fresh = newBoxes(game);
        assertEquals(4, game.mysteryBoxes().size());
        assertEquals(2, fresh.size());
        Position first = fresh.get(0);
        assertTrue(fresh.contains(new Position(7 - first.x(), 7 - first.y())));
    }

    @Test
    void aSingleFreeSlotIsNotFilledSoTheBoardStaysSymmetric() {
        IGameService game = factory.createWithSetup(START);

        play(game, "h2h4", "b7b6", "g2g3", "c8b7", "g1f3");

        assertEquals(3, game.mysteryBoxes().size());
    }

    @Test
    void thereAreNeverMoreThanFourBoxes() {
        for (long seed = 0; seed < 20; seed++) {
            IGameService game = factory.createWithSetup("|s=" + seed);
            List<String> moves = List.of(
                    "e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "g8f6", "d2d3", "f8c5", "b1c3", "d7d6",
                    "c1g5", "h7h6", "g5h4", "c8g4", "h2h3", "g4h5", "a2a3", "a7a6", "b2b4", "c5a7");
            for (String move : moves) {
                if (game.isFinished()) {
                    break;
                }
                try {
                    game.playMoveCode(move);
                } catch (IllegalArgumentException blockedByABox) {
                    break;
                }
                assertTrue(game.mysteryBoxes().size() <= 4, "seed " + seed + " after " + move);
            }
        }
    }

    @Test
    void undoingTheFifthTurnRemovesThemAndRedoingBringsBackTheSameOnes() {
        IGameService game = factory.createWithSetup(EMPTY);
        playAll(game, FIVE_TURNS);
        List<Position> appeared = game.mysteryBoxes();

        game.undoLastMove();
        assertTrue(game.mysteryBoxes().isEmpty());

        game.redoLastMove();
        assertEquals(appeared, game.mysteryBoxes());
    }

    @Test
    void aSavedGameGetsTheSameNewBoxesWhenLoaded() {
        IGameService game = factory.createWithSetup("|s=7");
        playAll(game, FIVE_TURNS);

        IGameService loaded = factory.createWithSetup(game.setupCode(), game.moveCodes());

        assertEquals(4, game.mysteryBoxes().size());
        assertEquals(game.mysteryBoxes(), loaded.mysteryBoxes());
        assertEquals("|s=7", game.setupCode());
    }

    @Test
    void aPieceCanOpenANewBox() {
        for (long seed = 0; seed < 40; seed++) {
            IGameService game = factory.createWithSetup("|s=" + seed);
            playAll(game, FIVE_TURNS);
            for (Position box : game.mysteryBoxes()) {
                Position from = reachableByActivePlayer(game, box);
                if (from != null) {
                    game.playMove(from, box);

                    assertTrue(game.powerAt(box) != null);
                    assertTrue(!game.mysteryBoxes().contains(box));
                    return;
                }
            }
        }
        throw new AssertionError("No seed put a new box within reach");
    }

    @Test
    void theClassicSetupNeverAddsBoxes() {
        IGameService game = TestGames.standard();

        playAll(game, FIVE_TURNS);

        assertTrue(game.mysteryBoxes().isEmpty());
        assertEquals(-1, game.turnsUntilNewBoxes());
    }

    @Test
    void theIntervalCanBeConfigured() {
        GameSetup everyTwo = GameSetup.mysteryBoxEdition().withBoxSpawning(new BoxSpawning(2, 1));
        IGameService game = new StandardGameFactory(new DefaultMoveCommandFactory(), everyTwo)
                .createWithSetup(START);

        play(game, "e2e3", "e7e6");

        assertEquals(6, game.mysteryBoxes().size());
    }

    @Test
    void mirroredBoxesCanHoldDifferentPowers() {
        boolean differentContents = false;
        for (long seed = 0; seed < 50 && !differentContents; seed++) {
            String code = new StandardGameFactory(new DefaultMoveCommandFactory(),
                    GameSetup.mysteryBoxEdition(), new Random(seed)).create().setupCode();
            differentContents = !powerIn(code, "a5").equals(powerIn(code, "h4"));
        }
        assertTrue(differentContents, "the mirror is only about position, not content");
    }

    @Test
    void whenNoPairOfSquaresIsFreeNoBoxAppears() {
        Board board = TestGames.board("kK", "..");
        MirroredBoxSpawner spawner = new MirroredBoxSpawner(5, 2, List.of(PowerType.STAR), 1);
        PowerState state = PowerState.initial(Map.of(), board, spawner);

        assertTrue(spawner.spawn(state, board, 5).isEmpty());
    }

    @Test
    void theSpawnerRespectsTheMaximum() {
        Board board = TestGames.board(
                "....k...",
                "........",
                "........",
                "........",
                "........",
                "........",
                "........",
                "....K...");
        MirroredBoxSpawner spawner = new MirroredBoxSpawner(5, 2, 4, List.of(PowerType.STAR), 1);
        PowerState twoBoxes = PowerState.initial(
                Map.of(at("a5"), PowerType.STAR, at("h4"), PowerType.STAR), board, spawner);

        assertEquals(2, spawner.spawn(twoBoxes, board, 5).size());
    }

    private static void playAll(IGameService game, List<String> moves) {
        play(game, moves.toArray(String[]::new));
    }

    private static List<Position> newBoxes(IGameService game) {
        List<Position> fresh = new ArrayList<>(game.mysteryBoxes());
        fresh.removeAll(List.of(at("a5"), at("c5"), at("f4"), at("h4")));
        return fresh;
    }

    private static Position reachableByActivePlayer(IGameService game, Position box) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Position from = new Position(x, y);
                if (game.belongsToActivePlayer(from) && game.legalMoves(from).contains(box)) {
                    return from;
                }
            }
        }
        return null;
    }

    private static String powerIn(String code, String square) {
        for (String entry : code.split("\\|")[0].split(",")) {
            if (entry.startsWith(square + "=")) {
                return entry.substring(square.length() + 1);
            }
        }
        throw new IllegalArgumentException(square + " is not in " + code);
    }
}
