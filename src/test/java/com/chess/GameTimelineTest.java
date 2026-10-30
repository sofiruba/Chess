package com.chess;

import static com.chess.TestGames.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chess.domain.command.ICommand;
import com.chess.domain.command.MoveCommand;
import com.chess.domain.game.GameSnapshot;
import com.chess.domain.game.GameTimeline;
import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.GameResult;
import com.chess.domain.state.ITurnStateFactory;
import com.chess.domain.state.TurnStateFactory;

final class GameTimelineTest {

    private final ITurnStateFactory states = new TurnStateFactory(new StubMoveGenerator(List.of()));
    private Board board;
    private GameSnapshot start;
    private GameTimeline timeline;

    @BeforeEach
    void setUp() {
        board = TestGames.board(
                "....k...",
                "........",
                "........",
                "........",
                "........",
                "........",
                "P.......",
                "....K...");
        start = GameSnapshot.initial(board, states.forColor(Color.WHITE));
        timeline = new GameTimeline(start);
    }

    @Test
    void startsAtTheInitialSnapshotWithoutMoves() {
        assertSame(start, timeline.current());
        assertEquals(Optional.empty(), timeline.lastMove());
        assertTrue(timeline.moves().isEmpty());
    }

    @Test
    void advancingExecutesTheCommandAndMakesTheNewSnapshotCurrent() {
        ICommand pawnMove = new MoveCommand(board, at("a2"), at("a3"));

        timeline.advance(pawnMove, board);

        assertNotNull(board.pieceAt(at("a3")));
        assertEquals(Color.BLACK, timeline.current().state().activePlayerColor());
        assertSame(pawnMove, timeline.lastMove().orElseThrow());
        assertEquals(1, timeline.moves().size());
    }

    @Test
    void undoingRevertsTheBoardAndRestoresThePreviousSnapshot() {
        timeline.advance(new MoveCommand(board, at("a2"), at("a3")), board);

        timeline.undo();

        assertSame(start, timeline.current());
        assertNotNull(board.pieceAt(at("a2")));
        assertNull(board.pieceAt(at("a3")));
        assertEquals(Optional.empty(), timeline.lastMove());
    }

    @Test
    void redoingPlaysTheUndoneMoveAgain() {
        timeline.advance(new MoveCommand(board, at("a2"), at("a3")), board);
        timeline.undo();

        timeline.redo(board);

        assertNotNull(board.pieceAt(at("a3")));
        assertEquals(Color.BLACK, timeline.current().state().activePlayerColor());
        assertEquals(1, timeline.moves().size());
    }

    @Test
    void aNewMoveForgetsTheUndoneOnes() {
        timeline.advance(new MoveCommand(board, at("a2"), at("a3")), board);
        timeline.undo();
        timeline.advance(new MoveCommand(board, at("e1"), at("e2")), board);

        assertThrows(IllegalStateException.class, () -> timeline.redo(board));
    }

    @Test
    void undoingOrRedoingWithNothingToDoFails() {
        assertThrows(IllegalStateException.class, () -> timeline.undo());
        assertThrows(IllegalStateException.class, () -> timeline.redo(board));
    }

    @Test
    void replacingTheStateKeepsTheRightsAndTheCounters() {
        timeline.replaceState(states.draw(Color.WHITE));

        assertEquals(GameResult.DRAW, timeline.current().state().result());
        assertSame(start.castlingRights(), timeline.current().castlingRights());
        assertSame(start.drawCounters(), timeline.current().drawCounters());
    }

    @Test
    void aMoveThatTheStateRejectsLeavesTheTimelineUntouched() {
        timeline.replaceState(states.draw(Color.WHITE));

        assertThrows(IllegalStateException.class,
                () -> timeline.advance(new MoveCommand(board, at("a2"), at("a3")), board));

        assertTrue(timeline.moves().isEmpty());
        assertNotNull(board.pieceAt(at("a2")));
    }
}
