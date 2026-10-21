package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.play;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.domain.Color;
import com.chess.domain.GameResult;
import com.chess.domain.IGameService;

final class DrawRulesTest {

    private static final String EMPTY = "........";
    private static final String[] KNIGHT_CYCLE = {"g1f3", "g8f6", "f3g1", "f6g8"};
    private static final String[] ROOK_CYCLE = {"h1h2", "h8h7", "h2h1", "h7h8"};

    @Test
    void kingVsKingIsDrawnAtCreation() {
        assertDrawnAtCreation("....k...", "....K...");
    }

    @Test
    void kingAndBishopVsKingIsDrawnAtCreation() {
        assertDrawnAtCreation("....k...", "..B.K...");
    }

    @Test
    void kingAndKnightVsKingIsDrawnAtCreation() {
        assertDrawnAtCreation("....k...", "..N.K...");
    }

    @Test
    void bishopsOnTheSameColorAreDrawnAtCreation() {
        assertDrawnAtCreation("....kb..", "..B.K...");
    }

    @Test
    void bishopsOnOppositeColorsAreNotDrawn() {
        assertNotFinishedAtCreation("..b.k...", "..B.K...");
    }

    @Test
    void kingAndRookVsKingIsNotDrawn() {
        assertNotFinishedAtCreation("....k...", "R...K...");
    }

    @Test
    void kingAndPawnVsKingIsNotDrawn() {
        IGameService game = gameWithPawn();

        assertFalse(game.isFinished());
    }

    @Test
    void twoKnightsVsKingIsNotDrawn() {
        assertNotFinishedAtCreation("....k...", "N.N.K...");
    }

    @Test
    void capturingTheLastPieceDrawsTheGame() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "k.......",
                ".R......",
                ".......K");
        assertFalse(game.isFinished());

        play(game, "h1g1", "a3b2");

        assertTrue(game.isFinished());
        assertEquals(GameResult.DRAW, game.result());
    }

    @Test
    void stalemateIsDetectedAtCreation() {
        IGameService game = TestGames.fromDiagram(Color.BLACK,
                "k.......",
                EMPTY,
                ".Q......",
                EMPTY, EMPTY, EMPTY, EMPTY,
                ".......K");

        assertTrue(game.isFinished());
        assertEquals(GameResult.DRAW, game.result());
    }

    @Test
    void checkmateIsDetectedAtCreation() {
        IGameService game = TestGames.fromDiagram(Color.BLACK,
                "R.....k.",
                ".....ppp",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "....K...");

        assertTrue(game.isFinished());
        assertEquals(GameResult.WHITE_WINS, game.result());
    }

    @Test
    void oneCycleOfRepetitionIsNotADraw() {
        IGameService game = TestGames.standard();

        play(game, KNIGHT_CYCLE);

        assertFalse(game.isFinished());
    }

    @Test
    void threefoldRepetitionIsADraw() {
        IGameService game = TestGames.standard();

        play(game, KNIGHT_CYCLE);
        play(game, KNIGHT_CYCLE);

        assertTrue(game.isFinished());
        assertEquals(GameResult.DRAW, game.result());
    }

    @Test
    void undoRestoresTheCountersSoReplayingDoesNotDoubleCount() {
        IGameService game = TestGames.standard();
        play(game, KNIGHT_CYCLE);

        for (int i = 0; i < KNIGHT_CYCLE.length; i++) {
            game.undoLastMove();
        }
        play(game, KNIGHT_CYCLE);

        assertFalse(game.isFinished());
    }

    @Test
    void redoCountsPositionsAgain() {
        IGameService game = TestGames.standard();
        play(game, KNIGHT_CYCLE);
        for (int i = 0; i < KNIGHT_CYCLE.length; i++) {
            game.undoLastMove();
        }
        for (int i = 0; i < KNIGHT_CYCLE.length; i++) {
            game.redoLastMove();
        }

        play(game, KNIGHT_CYCLE);

        assertTrue(game.isFinished());
        assertEquals(GameResult.DRAW, game.result());
    }

    @Test
    void undoingTheDrawingMoveReopensTheGameAndReplayingDrawsAgain() {
        IGameService game = TestGames.standard();
        play(game, KNIGHT_CYCLE);
        play(game, KNIGHT_CYCLE);

        game.undoLastMove();
        assertFalse(game.isFinished());
        game.redoLastMove();

        assertTrue(game.isFinished());
    }

    @Test
    void castlingRightsArePartOfThePosition() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "....k..r",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "....K..R");

        play(game, ROOK_CYCLE);
        play(game, ROOK_CYCLE);

        assertFalse(game.isFinished(), "The starting position had castling rights, so it was not seen three times");
    }

    @Test
    void fiftyMovesWithoutPawnMoveOrCaptureIsADraw() {
        IGameService game = rookEndgame();
        List<String> plies = nonRepeatingPlies(100);

        play(game, plies.subList(0, 99).toArray(new String[0]));
        assertFalse(game.isFinished());
        play(game, plies.get(99));

        assertTrue(game.isFinished());
        assertEquals(GameResult.DRAW, game.result());
    }

    @Test
    void undoAndRedoKeepTheFiftyMoveClockConsistent() {
        IGameService game = rookEndgame();
        play(game, nonRepeatingPlies(100).toArray(new String[0]));
        assertTrue(game.isFinished());

        game.undoLastMove();
        assertFalse(game.isFinished());
        game.redoLastMove();

        assertTrue(game.isFinished());
    }

    @Test
    void aPawnMoveResetsTheFiftyMoveClock() {
        IGameService game = TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY, EMPTY, EMPTY, EMPTY,
                "P.......",
                "..R.....",
                "K.......");
        List<String> rookMoves = rookMoves(60);
        for (int i = 0; i < 45; i++) {
            play(game, rookMoves.get(i), kingMove(i));
        }

        play(game, "a3a4", kingMove(45));
        for (int i = 45; i < 49; i++) {
            play(game, rookMoves.get(i), kingMove(i + 1));
        }

        assertFalse(game.isFinished(), "100 plies were played but the clock restarted at ply 91");
    }

    private void assertDrawnAtCreation(String topRank, String bottomRank) {
        IGameService game = TestGames.fromDiagram(Color.WHITE, boardRows(topRank, bottomRank));

        assertTrue(game.isFinished());
        assertEquals(GameResult.DRAW, game.result());
    }

    private void assertNotFinishedAtCreation(String topRank, String bottomRank) {
        IGameService game = TestGames.fromDiagram(Color.WHITE, boardRows(topRank, bottomRank));

        assertFalse(game.isFinished());
    }

    private IGameService gameWithPawn() {
        return TestGames.fromDiagram(Color.WHITE,
                "....k...",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "P.......",
                "....K...");
    }

    private String[] boardRows(String topRank, String bottomRank) {
        String[] rows = new String[8];
        rows[0] = topRank;
        for (int i = 1; i < 7; i++) {
            rows[i] = EMPTY;
        }
        rows[7] = bottomRank;
        return rows;
    }

    private IGameService rookEndgame() {
        return TestGames.fromDiagram(Color.WHITE,
                "k.......",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "..R.....",
                "K.......");
    }

    /**
     * White walks a rook one square at a time over files c-h and ranks 2-7 (forward, then back) while the black king
     * swings between a8 and b8. No position can occur three times and nothing is a pawn move or a capture.
     */
    private List<String> nonRepeatingPlies(int count) {
        List<String> rookMoves = rookMoves(count / 2);
        List<String> plies = new ArrayList<>();
        for (int i = 0; i < count / 2; i++) {
            plies.add(rookMoves.get(i));
            plies.add(kingMove(i));
        }
        return plies;
    }

    private String kingMove(int index) {
        return index % 2 == 0 ? "a8b8" : "b8a8";
    }

    private List<String> rookMoves(int count) {
        List<String> path = rookPath();
        int last = path.size() - 1;
        List<String> moves = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (i < last) {
                moves.add(path.get(i) + path.get(i + 1));
            } else {
                int back = i - last;
                moves.add(path.get(last - back) + path.get(last - back - 1));
            }
        }
        return moves;
    }

    private List<String> rookPath() {
        List<String> path = new ArrayList<>();
        for (int rank = 2; rank <= 7; rank++) {
            boolean leftToRight = rank % 2 == 0;
            for (int i = 0; i < 6; i++) {
                char file = (char) ('c' + (leftToRight ? i : 5 - i));
                path.add("" + file + rank);
            }
        }
        return path;
    }
}
