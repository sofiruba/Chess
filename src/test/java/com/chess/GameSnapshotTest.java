package com.chess;

import static com.chess.TestGames.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chess.domain.Board;
import com.chess.domain.Color;
import com.chess.domain.GameSnapshot;
import com.chess.domain.ICommand;
import com.chess.domain.IGameState;
import com.chess.domain.ITurnStateFactory;
import com.chess.domain.MoveCommand;
import com.chess.domain.TurnStateFactory;

final class GameSnapshotTest {

    private final ITurnStateFactory states = new TurnStateFactory();
    private Board board;
    private GameSnapshot start;

    @BeforeEach
    void setUp() {
        board = TestGames.board(
                "....k..n",
                "........",
                "........",
                "........",
                "........",
                "........",
                "P.......",
                "....K..R");
        start = GameSnapshot.initial(board, states.forColor(Color.WHITE));
    }

    @Test
    void theInitialSnapshotHasAllCastlingRightsAndTheStartingPositionSeenOnce() {
        assertTrue(start.castlingRights().canCastle(Color.WHITE, 1));
        assertTrue(start.castlingRights().canCastle(Color.BLACK, -1));
        assertEquals(0, start.drawCounters().halfMoveClock());
        assertEquals(1, start.drawCounters().timesCurrentPositionSeen());
        assertEquals(Color.WHITE, start.state().activePlayerColor());
    }

    @Test
    void afterAMoveItGivesTheNextStateRightsAndCountersWithoutChangingTheOriginal() {
        ICommand kingMove = new MoveCommand(board, at("e1"), at("e2"));
        IGameState next = start.state().handleMove(kingMove);

        GameSnapshot after = start.after(kingMove, board, next);

        assertEquals(Color.BLACK, after.state().activePlayerColor());
        assertFalse(after.castlingRights().canCastle(Color.WHITE, 1));
        assertTrue(after.castlingRights().canCastle(Color.BLACK, 1));
        assertEquals(1, after.drawCounters().halfMoveClock());
        assertTrue(start.castlingRights().canCastle(Color.WHITE, 1));
        assertEquals(0, start.drawCounters().halfMoveClock());
    }

    @Test
    void withStateChangesOnlyTheState() {
        IGameState finished = states.draw(Color.WHITE);

        GameSnapshot drawn = start.withState(finished);

        assertSame(finished, drawn.state());
        assertSame(start.castlingRights(), drawn.castlingRights());
        assertSame(start.drawCounters(), drawn.drawCounters());
    }

    @Test
    void itRejectsMissingParts() {
        assertThrows(NullPointerException.class,
                () -> new GameSnapshot(null, start.castlingRights(), start.drawCounters()));
        assertThrows(NullPointerException.class, () -> GameSnapshot.initial(null, states.forColor(Color.WHITE)));
    }
}
