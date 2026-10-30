package com.chess;

import static com.chess.TestGames.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chess.domain.command.ICommand;
import com.chess.domain.command.MoveCommand;
import com.chess.domain.game.GameSnapshot;
import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.PieceRoles;
import com.chess.domain.state.IGameState;
import com.chess.domain.state.ITurnStateFactory;
import com.chess.domain.state.TurnStateFactory;

final class GameSnapshotTest {

    private final ITurnStateFactory states = new TurnStateFactory(new StubMoveGenerator(List.of()));
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
        start = GameSnapshot.initial(board, states.forColor(Color.WHITE), PieceRoles.standard());
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
        IGameState turn = states.forColor(Color.WHITE);
        PieceRoles roles = PieceRoles.standard();

        assertThrows(NullPointerException.class, () -> GameSnapshot.initial(null, turn, roles));
    }
}