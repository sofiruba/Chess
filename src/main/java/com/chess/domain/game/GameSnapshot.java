package com.chess.domain.game;

import com.chess.domain.model.*;

import com.chess.domain.command.*;

import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;


import java.util.Objects;

/**
 * Todo lo que cambia con cada jugada y debe recuperarse al deshacer: el estado de la partida, los derechos de
 * enroque y los contadores de tablas. Es inmutable, por lo que el historial guarda una por jugada y deshacer solo
 * tiene que restaurarla.
 */
public record GameSnapshot(IGameState state, CastlingRights castlingRights, DrawCounters drawCounters) {

    public GameSnapshot {
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(castlingRights, "castlingRights must not be null");
        Objects.requireNonNull(drawCounters, "drawCounters must not be null");
    }

    public static GameSnapshot initial(Board board, IGameState state) {
        return initial(board, state, PieceRoles.standard());
    }

    public static GameSnapshot initial(Board board, IGameState state, PieceRoles roles) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(roles, "roles must not be null");
        CastlingRights rights = CastlingRights.initial(board.size(), roles);
        PositionKey start = PositionKey.initial(board, state.activePlayerColor(), rights);
        return new GameSnapshot(state, rights, DrawCounters.initial(start, roles));
    }

    /**
     * @param move la jugada ya ejecutada en el tablero
     * @param board el tablero después de esa jugada
     * @param nextState el estado que gestionó la jugada
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
