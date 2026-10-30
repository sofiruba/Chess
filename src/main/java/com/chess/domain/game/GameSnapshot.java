package com.chess.domain.game;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.Objects;

/**
 * Everything that changes with each move and has to come back on undo: the game state, the castling rights and the
 * draw counters. It is immutable, so the history keeps one per move and undo only has to restore it.
 */
public record GameSnapshot(IGameState state, CastlingRights castlingRights, DrawCounters drawCounters) {

    public GameSnapshot {
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(castlingRights, "castlingRights must not be null");
        Objects.requireNonNull(drawCounters, "drawCounters must not be null");
    }

    public static GameSnapshot initial(Board board, IGameState state) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(state, "state must not be null");
        CastlingRights rights = CastlingRights.initial(board.size());
        PositionKey start = PositionKey.initial(board, state.activePlayerColor(), rights);
        return new GameSnapshot(state, rights, DrawCounters.initial(start));
    }

    /**
     * @param move the move already executed on the board
     * @param board the board after that move
     * @param nextState the state that handled the move
     */
    public GameSnapshot after(IMoveInfo move, Board board, IGameState nextState) {
        Objects.requireNonNull(nextState, "nextState must not be null");
        CastlingRights rights = castlingRights.after(move);
        PositionKey position = PositionKey.of(board, nextState.activePlayerColor(), rights, move);
        return new GameSnapshot(nextState, rights, drawCounters.after(move, position));
    }

    public GameSnapshot withState(IGameState newState) {
        return new GameSnapshot(newState, castlingRights, drawCounters);
    }
}