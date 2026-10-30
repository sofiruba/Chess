package com.chess;

import static com.chess.TestGames.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chess.domain.model.Board;
import com.chess.domain.rule.CastlingRights;
import com.chess.domain.model.Color;
import com.chess.domain.draw.DrawCounters;
import com.chess.domain.command.ICommand;
import com.chess.domain.command.MoveCommand;
import com.chess.domain.draw.PositionKey;

final class DrawCountersTest {

    private final CastlingRights rights = CastlingRights.initial(8);
    private Board board;
    private DrawCounters counters;

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
        counters = DrawCounters.initial(PositionKey.of(board, Color.WHITE, rights, null));
    }

    @Test
    void startsWithTheInitialPositionSeenOnce() {
        assertEquals(0, counters.halfMoveClock());
        assertEquals(1, counters.timesCurrentPositionSeen());
    }

    @Test
    void aQuietMoveIncrementsTheClock() {
        counters = advance(counters, "h1", "h2");
        counters = advance(counters, "h2", "h3");

        assertEquals(2, counters.halfMoveClock());
    }

    @Test
    void aPawnMoveResetsTheClock() {
        counters = advance(counters, "h1", "h2");

        counters = advance(counters, "a2", "a3");

        assertEquals(0, counters.halfMoveClock());
    }

    @Test
    void aCaptureResetsTheClock() {
        counters = advance(counters, "e1", "e2");
        assertEquals(1, counters.halfMoveClock());

        counters = advance(counters, "h1", "h8");

        assertEquals(0, counters.halfMoveClock());
    }

    @Test
    void theSamePositionReachedAgainIsCounted() {
        ICommand there = new MoveCommand(board, at("h1"), at("h2"));
        there.execute();
        PositionKey away = PositionKey.of(board, Color.BLACK, rights, there);
        DrawCounters first = counters.after(there, away);

        DrawCounters second = first.after(there, away);

        assertEquals(1, first.timesCurrentPositionSeen());
        assertEquals(2, second.timesCurrentPositionSeen());
    }

    @Test
    void enPassantSquareIsPartOfThePosition() {
        ICommand doubleStep = new MoveCommand(board, at("a2"), at("a4"));
        doubleStep.execute();

        PositionKey afterDoubleStep = PositionKey.of(board, Color.BLACK, rights, doubleStep);
        PositionKey withoutHistory = PositionKey.of(board, Color.BLACK, rights, null);

        assertEquals(at("a3"), afterDoubleStep.enPassantSquare());
        assertNull(withoutHistory.enPassantSquare());
        assertNotEquals(afterDoubleStep, withoutHistory);
    }

    @Test
    void aSingleStepLeavesNoEnPassantSquare() {
        ICommand singleStep = new MoveCommand(board, at("a2"), at("a3"));
        singleStep.execute();

        assertNull(PositionKey.of(board, Color.BLACK, rights, singleStep).enPassantSquare());
    }

    @Test
    void turnAndCastlingRightsDistinguishPositions() {
        PositionKey whiteToMove = PositionKey.of(board, Color.WHITE, rights, null);
        PositionKey blackToMove = PositionKey.of(board, Color.BLACK, rights, null);
        ICommand kingMove = new MoveCommand(board, at("e1"), at("e2"));
        PositionKey withoutRights = PositionKey.of(board, Color.WHITE, rights.after(kingMove), null);

        assertNotEquals(whiteToMove, blackToMove);
        assertNotEquals(whiteToMove, withoutRights);
    }

    private DrawCounters advance(DrawCounters current, String from, String to) {
        ICommand command = new MoveCommand(board, at(from), at(to));
        command.execute();
        PositionKey key = PositionKey.of(board, Color.BLACK, rights, command);
        return current.after(command, key);
    }
}
